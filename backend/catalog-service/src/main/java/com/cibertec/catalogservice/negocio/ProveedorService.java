package com.cibertec.catalogservice.negocio;

import com.cibertec.common.dto.ProveedorRequest;
import com.cibertec.common.dto.ProveedorResponse;
import com.cibertec.catalogservice.entidades.Proveedor;
import com.cibertec.catalogservice.repositorio.ProductoRepository;
import com.cibertec.catalogservice.repositorio.ProveedorRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ProveedorService {

	private final ProveedorRepository proveedorRepository;
	private final ProductoRepository productoRepository;

	public ProveedorService(ProveedorRepository proveedorRepository, ProductoRepository productoRepository) {
		this.proveedorRepository = proveedorRepository;
		this.productoRepository = productoRepository;
	}

	public List<ProveedorResponse> getAllProveedores() {
		return proveedorRepository.findAll().stream()
				.map(this::mapToResponse)
				.toList();
	}

	public ProveedorResponse getProveedorById(Long id) {
		return mapToResponse(findOrFail(id));
	}

	public ProveedorResponse createProveedor(ProveedorRequest request) {
		if (proveedorRepository.existsByRuc(request.ruc())) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un proveedor con ese RUC");
		}

		Proveedor proveedor = Proveedor.builder()
				.razonSocial(request.razonSocial().trim())
				.ruc(request.ruc())
				.telefono(clean(request.telefono()))
				.email(clean(request.email()))
				.build();
		return mapToResponse(proveedorRepository.save(proveedor));
	}

	public ProveedorResponse updateProveedor(Long id, ProveedorRequest request) {
		Proveedor proveedor = findOrFail(id);
		if (proveedorRepository.existsByRucAndIdNot(request.ruc(), id)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un proveedor con ese RUC");
		}

		proveedor.setRazonSocial(request.razonSocial().trim());
		proveedor.setRuc(request.ruc());
		proveedor.setTelefono(clean(request.telefono()));
		proveedor.setEmail(clean(request.email()));
		return mapToResponse(proveedorRepository.save(proveedor));
	}

	public void deleteProveedor(Long id) {
		findOrFail(id);
		if (productoRepository.existsByProveedorId(id)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "No se puede eliminar: el proveedor tiene productos asociados");
		}
		proveedorRepository.deleteById(id);
	}

	private Proveedor findOrFail(Long id) {
		return proveedorRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proveedor no encontrado"));
	}

	private String clean(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}

	private ProveedorResponse mapToResponse(Proveedor proveedor) {
		return new ProveedorResponse(
				proveedor.getId(),
				proveedor.getRazonSocial(),
				proveedor.getRuc(),
				proveedor.getTelefono(),
				proveedor.getEmail()
		);
	}
}

