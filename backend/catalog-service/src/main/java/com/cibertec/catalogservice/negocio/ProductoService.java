package com.cibertec.catalogservice.negocio;

import com.cibertec.catalogservice.entidades.Categoria;
import com.cibertec.catalogservice.entidades.Producto;
import com.cibertec.catalogservice.entidades.Proveedor;
import com.cibertec.catalogservice.repositorio.CategoriaRepository;
import com.cibertec.catalogservice.repositorio.ProductoRepository;
import com.cibertec.catalogservice.repositorio.ProveedorRepository;
import com.cibertec.common.dto.ProductoRequest;
import com.cibertec.common.dto.ProductoResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ProductoService {

	private final ProductoRepository productoRepository;
	private final CategoriaRepository categoriaRepository;
	private final ProveedorRepository proveedorRepository;
	private final AlertProducerService alertProducerService;

	@org.springframework.beans.factory.annotation.Autowired
	public ProductoService(
			ProductoRepository productoRepository,
			CategoriaRepository categoriaRepository,
			ProveedorRepository proveedorRepository,
			@org.springframework.beans.factory.annotation.Autowired(required = false) AlertProducerService alertProducerService
	) {
		this.productoRepository = productoRepository;
		this.categoriaRepository = categoriaRepository;
		this.proveedorRepository = proveedorRepository;
		this.alertProducerService = alertProducerService;
	}

	public ProductoService(
			ProductoRepository productoRepository,
			CategoriaRepository categoriaRepository,
			ProveedorRepository proveedorRepository
	) {
		this(productoRepository, categoriaRepository, proveedorRepository, null);
	}

	public List<ProductoResponse> getAllProductos() {
		return productoRepository.findAll().stream()
				.map(ProductoService::mapToResponse)
				.toList();
	}

	public ProductoResponse getProductoById(Long id) {
		return mapToResponse(findOrFail(id));
	}

	public ProductoResponse createProducto(ProductoRequest request) {
		validarPrecios(request);
		String codigo = request.codigo().trim();
		if (productoRepository.existsByCodigo(codigo)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un producto con ese código");
		}

		Producto producto = Producto.builder()
				.codigo(codigo)
				.nombre(request.nombre().trim())
				.descripcion(clean(request.descripcion()))
				.precioCompra(request.precioCompra())
				.precioVenta(request.precioVenta())
				.stockActual(request.stockInicial() == null ? 0 : request.stockInicial())
				.stockMinimo(request.stockMinimo())
				.fechaVencimiento(request.fechaVencimiento())
				.categoria(findCategoria(request.categoriaId()))
				.proveedor(findProveedor(request.proveedorId()))
				.build();

		return mapToResponse(productoRepository.save(producto));
	}

	public ProductoResponse updateProducto(Long id, ProductoRequest request) {
		Producto producto = findOrFail(id);
		validarPrecios(request);
		String codigo = request.codigo().trim();
		if (productoRepository.existsByCodigoAndIdNot(codigo, id)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un producto con ese código");
		}

		producto.setCodigo(codigo);
		producto.setNombre(request.nombre().trim());
		producto.setDescripcion(clean(request.descripcion()));
		producto.setPrecioCompra(request.precioCompra());
		producto.setPrecioVenta(request.precioVenta());
		producto.setStockMinimo(request.stockMinimo());
		producto.setFechaVencimiento(request.fechaVencimiento());
		producto.setCategoria(findCategoria(request.categoriaId()));
		producto.setProveedor(findProveedor(request.proveedorId()));

		return mapToResponse(productoRepository.save(producto));
	}

	public ProductoResponse updateStock(Long id, Integer nuevoStock) {
		Producto producto = findOrFail(id);
		producto.setStockActual(nuevoStock);
		Producto guardado = productoRepository.save(producto);

		if (alertProducerService != null && guardado.getStockActual() <= guardado.getStockMinimo()) {
			alertProducerService.emitStockAlert(com.cibertec.common.event.StockAlertEvent.builder()
					.tipoAlerta("STOCK_BAJO")
					.productoId(guardado.getId())
					.productoCodigo(guardado.getCodigo())
					.productoNombre(guardado.getNombre())
					.stockActual(guardado.getStockActual())
					.stockMinimo(guardado.getStockMinimo())
					.mensaje("ALERTA: El producto " + guardado.getNombre() + " alcanzó un nivel crítico de stock (" + guardado.getStockActual() + ")")
					.fechaEvento(java.time.LocalDateTime.now())
					.build());
		}

		return mapToResponse(guardado);
	}

	public void deleteProducto(Long id) {
		findOrFail(id);
		productoRepository.deleteById(id);
	}

	private void validarPrecios(ProductoRequest request) {
		if (request.precioVenta().compareTo(request.precioCompra()) < 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El precio de venta no puede ser menor al precio de compra");
		}
	}

	public Producto findOrFail(Long id) {
		return productoRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
	}

	private Categoria findCategoria(Long id) {
		return categoriaRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoría no encontrada"));
	}

	private Proveedor findProveedor(Long id) {
		if (id == null) {
			return null;
		}
		return proveedorRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proveedor no encontrado"));
	}

	private String clean(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}

	public static ProductoResponse mapToResponse(Producto producto) {
		Proveedor proveedor = producto.getProveedor();
		return new ProductoResponse(
				producto.getId(),
				producto.getCodigo(),
				producto.getNombre(),
				producto.getDescripcion(),
				producto.getPrecioCompra(),
				producto.getPrecioVenta(),
				producto.getStockActual(),
				producto.getStockMinimo(),
				producto.getFechaVencimiento(),
				producto.getCategoria().getId(),
				producto.getCategoria().getNombre(),
				proveedor == null ? null : proveedor.getId(),
				proveedor == null ? null : proveedor.getRazonSocial(),
				producto.getStockActual() <= producto.getStockMinimo()
		);
	}
}
