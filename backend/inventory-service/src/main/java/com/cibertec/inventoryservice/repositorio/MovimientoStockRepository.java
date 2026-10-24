package com.cibertec.inventoryservice.repositorio;

import com.cibertec.common.dto.TipoMovimiento;
import com.cibertec.inventoryservice.entidades.MovimientoStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MovimientoStockRepository extends JpaRepository<MovimientoStock, Long> {

	List<MovimientoStock> findAllByOrderByFechaDesc();

	boolean existsByProductoId(Long productoId);

	@Query("""
			SELECT m FROM MovimientoStock m
			WHERE (:desde IS NULL OR m.fecha >= :desde)
			  AND (:hasta IS NULL OR m.fecha < :hasta)
			  AND (:tipo IS NULL OR m.tipo = :tipo)
			ORDER BY m.fecha DESC
			""")
	List<MovimientoStock> findAllParaReporte(
			@Param("desde") LocalDateTime desde,
			@Param("hasta") LocalDateTime hasta,
			@Param("tipo") TipoMovimiento tipo
	);
}
