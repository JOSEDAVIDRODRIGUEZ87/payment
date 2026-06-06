# Payment Authorization Service 💳

Servicio backend de alta disponibilidad para la autorización y consulta de transacciones de pago, diseñado bajo los principios de **Arquitectura Hexagonal (Ports & Adapters)**, DDD (Domain-Driven Design) táctico y patrones de resiliencia empresarial.

---

## 🚀 Descripción de la Solución

El sistema implementa un flujo desacoplado para procesar pagos de manera segura y eficiente. La solución aísla completamente las reglas del negocio de los componentes de infraestructura (como las bases de datos relacionales o los proveedores HTTP externos), garantizando que el núcleo de la aplicación sea altamente testeable, mantenible y agnóstico a los frameworks.

### 🏛️ Diseño Arquitectónico (Ports & Adapters)

La estructura del código sigue una distribución estricta de capas orientada al dominio, garantizando que la dependencia fluya siempre desde el exterior hacia el centro:

* **`domain` (Núcleo de Negocio):** Contiene los modelos de negocio puros (`Payment`, `AuthorizationResult`) y los servicios de dominio que orquestan las reglas operacionales. No tiene dependencias de Spring ni de ninguna librería externa.
* **`ports` (Interfaces de Aislamiento):**
    * *Inbound Ports (Puertos de Entrada):* Interfaces que definen los casos de uso disponibles para el exterior (`AuthorizePaymentUseCase`, `GetPaymentUseCase`).
    * *Outbound Ports (Puertos de Salida):* Interfaces que definen las necesidades de comunicación externa del negocio (`PaymentRepositoryPort`, `AntiFraudPort`).
* **`infrastructure` (Detalles Tecnológicos):**
    * *Inbound Adapters:* Controladores REST (`PaymentController`) que exponen los endpoints y un manejador de excepciones global (`GlobalExceptionHandler`) para traducir errores en respuestas estructuradas.
    * *Outbound Adapters:* Clientes de persistencia con Spring Data JPA (PostgreSQL) y adaptadores HTTP con OpenFeign para la comunicación con el servicio externo antifraude.

---

## 🛠️ Decisiones Técnicas y Patrones Usados

* **Java 17 & Spring Boot 3.4.x:** Uso intensivo de capacidades modernas del lenguaje, como **Records** para la inmutabilidad de los DTOs y la API de Streams para un procesamiento funcional limpio.
* **Estándar REST Semántico:** Control estricto de los códigos de estado HTTP. Los errores de formato o validación en los payloads de entrada interceptan la excepción `MethodArgumentNotValidException` a través del controlador de consejo global, respondiendo inmediatamente con un código **`400 Bad Request`** en lugar de enmascarar fallos de datos con estados `200 OK`.
* **Inversión de Dependencias (IoD):** La capa de aplicación/controladores se acopla exclusivamente a los puertos (interfaces). Esto permite cambiar el proveedor de base de datos o el motor de análisis de fraude sin alterar una sola línea de código del dominio.
* **Aislamiento de Contratos (DTOs & Mappers):** Se definen `PaymentRequest` y `PaymentResponse` independientes para proteger la integridad del dominio. La transformación de datos se centraliza en mappers dedicados (`PaymentRestMapper`), evitando la exposición de entidades JPA o modelos de dominio en la red.

---

## 🛡️ Configuración de Caché y Resiliencia

El servicio está blindado contra caídas en cascada y latencias de red mediante estrategias avanzadas de tolerancia a fallos:

### ⚡ Resiliencia (Circuit Breaker & Retries)
Utilizando **Resilience4j**, el adaptador de infraestructura que consume el servicio externo de fraude (`AntiFraudAdapter`) cuenta con las siguientes políticas:
* **Retries (Reintentos):** Ante fallos de red efímeros o intermitencias del proveedor externo, el sistema ejecuta reintentos automáticos configurados con un intervalo de espera antes de propagar un error.
* **Circuit Breaker (Disyuntor):** Monitorea la tasa de éxito de las peticiones HTTP. Si el umbral de error supera el porcentaje configurado, el circuito pasa a estado `OPEN`, desviando el tráfico de inmediato hacia un método de respaldo (**Fallback**) seguro. Esto evita el estancamiento de hilos (thread starvation) y mantiene el microservicio operativo.

### 💾 Almacenamiento en Caché
Se implementó un mecanismo de caché en memoria (`@Cacheable`) enfocado en las consultas repetitivas de análisis de fraude para el mismo identificador de transacción, configurando una política de **TTL (Time-To-Live) de 5 minutos**.
* **Impacto:** Optimiza los tiempos de respuesta de la API, disminuye la carga transaccional hacia el tercero y mitiga penalizaciones por Rate Limiting en el proveedor externo.

---

## ⚙️ Cómo Ejecutar el Proyecto

### Prerrequisitos
* Java 17 instalado.
* Maven 3.8 o superior.
* Instancia de PostgreSQL activa.

### 1. Configuración de Base de Datos
Ajusta las credenciales de conexión en el archivo `src/main/resources/application.properties` (o mediante variables de entorno):

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/payment_db
spring.datasource.username=tu_usuario
spring.datasource.password=tu_contrasena
spring.jpa.hibernate.ddl-auto=update