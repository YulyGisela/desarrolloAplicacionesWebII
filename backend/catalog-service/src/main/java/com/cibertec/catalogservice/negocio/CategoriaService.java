package com.cibertec.catalogservice.negocio;

import com.cibertec.common.dto.CategoriaRequest;
import com.cibertec.common.dto.CategoriaResponse;
import com.cibertec.catalogservice.entidades.Categoria;
import com.cibertec.catalogservice.repositorio.CategoriaRepository;
import com.cibertec.catalogservice.repositorio.ProductoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CategoriaService {

	private final CategoriaRepository categoriaRepository;
	private final ProductoRepository productoRepository;

	public CategoriaService(CategoriaRepository categoriaRepository, ProductoRepository productoRepository) {
		this.categoriaRepository = categoriaRepository;
		this.productoRepository = productoRepository;
	}

	public List<CategoriaResponse> getAllCategorias() {
		return categoriaRepository.findAll().stream()
				.map(this::mapToResponse)
				.toList();
	}

	public CategoriaResponse getCategoriaById(Long id) {
		return mapToResponse(findOrFail(id));
	}

	public CategoriaResponse createCategoria(CategoriaRequest request) {
		String nombre = request.nombre().trim();
		if (categoriaRepository.existsByNombreIgnoreCase(nombre)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una categorÃ­a con ese nombre");
		}

		Categoria categoria = Categoria.builder()
				.nombre(nombre)
				.descripcion(clean(request.descripcion()))
				.build();
		return mapToResponse(categoriaRepository.save(categoria));
	}

	public CategoriaResponse updateCategoria(Long id, CategoriaRequest request) {
		Categoria categoria = findOrFail(id);
		String nombre = request.nombre().trim();
		if (categoriaRepository.existsByNombreIgnoreCaseAndIdNot(nombre, id)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una categorÃ­a con ese nombre");
		}

		categoria.setNombre(nombre);
		categoria.setDescripcion(clean(request.descripcion()));
		return mapToResponse(categoriaRepository.save(categoria));
	}

	public void deleteCategoria(Long id) {
		findOrFail(id);
		if (productoRepository.existsByCategoriaId(id)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "No se puede eliminar: la categorÃ­a tiene productos asociados");
		}
		categoriaRepository.deleteById(id);
	}

	private Categoria findOrFail(Long id) {
		return categoriaRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "CategorÃ­a no encontrada"));
	}

	private String clean(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}

	private CategoriaResponse mapToResponse(Categoria categoria) {
		return new CategoriaResponse(categoria.getId(), categoria.getNombre(), categoria.getDescripcion());
	}
}

