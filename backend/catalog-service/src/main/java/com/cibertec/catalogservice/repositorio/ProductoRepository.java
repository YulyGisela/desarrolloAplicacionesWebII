package com.cibertec.catalogservice.repositorio;

import com.cibertec.catalogservice.entidades.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

	boolean existsByCodigo(String codigo);

	boolean existsByCodigoAndIdNot(String codigo, Long id);

	boolean existsByCategoriaId(Long categoriaId);

	boolean existsByProveedorId(Long proveedorId);
}

