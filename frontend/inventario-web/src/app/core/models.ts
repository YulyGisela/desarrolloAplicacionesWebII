export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegistroRequest {
  nombre: string;
  username: string;
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  tokenType: string;
  expiresInMinutes: number;
  username: string;
  nombre: string;
  role: string;
}

export interface Categoria {
  id?: number;
  nombre: string;
  descripcion?: string | null;
}

export interface Proveedor {
  id?: number;
  razonSocial: string;
  ruc: string;
  telefono?: string | null;
  email?: string | null;
}

export interface Producto {
  id: number;
  codigo: string;
  nombre: string;
  descripcion: string | null;
  precioCompra: number;
  precioVenta: number;
  stockActual: number;
  stockMinimo: number;
  fechaVencimiento: string | null;
  categoriaId: number;
  categoriaNombre: string;
  proveedorId: number | null;
  proveedorNombre: string | null;
  stockBajo: boolean;
}

export interface ProductoRequest {
  codigo: string;
  nombre: string;
  descripcion: string | null;
  precioCompra: number;
  precioVenta: number;
  stockInicial: number | null;
  stockMinimo: number;
  fechaVencimiento: string | null;
  categoriaId: number;
  proveedorId: number | null;
}

export type TipoMovimiento = 'ENTRADA' | 'SALIDA';

export interface Movimiento {
  id: number;
  productoId: number;
  productoCodigo: string;
  productoNombre: string;
  tipo: TipoMovimiento;
  cantidad: number;
  motivo: string | null;
  fecha: string;
  usuario: string;
}

export interface MovimientoRequest {
  productoId: number;
  tipo: TipoMovimiento;
  cantidad: number;
  motivo: string | null;
}

export interface Dashboard {
  totalProductos: number;
  totalCategorias: number;
  totalProveedores: number;
  productosStockBajo: number;
  productosPorVencer: number;
  valorInventario: number;
  stockBajo: Producto[];
  porVencer: Producto[];
}
