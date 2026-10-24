package com.cibertec.catalogservice.repositorio;

import com.cibertec.catalogservice.entidades.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {

	boolean existsByRuc(String ruc);

	boolean existsByRucAndIdNot(String ruc, Long id);
}

