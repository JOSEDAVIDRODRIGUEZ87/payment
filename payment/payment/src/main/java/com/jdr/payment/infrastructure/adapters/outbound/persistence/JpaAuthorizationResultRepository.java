package com.jdr.payment.infrastructure.adapters.outbound.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jdr.payment.infrastructure.adapters.outbound.persistence.entities.AuthorizationResultEntity;

@Repository
public interface JpaAuthorizationResultRepository extends JpaRepository<AuthorizationResultEntity, String> {
    // Hereda automáticamente todos los métodos CRUD (save, findById, delete, etc.)
}