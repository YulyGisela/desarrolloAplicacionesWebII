# inventario-web (frontend Angular)

Interfaz de **BodegaPro**. Angular 21 · componentes standalone · signals · formularios reactivos · interceptor JWT · guards.

## Ejecutar

Requisitos: Node.js 20 o superior.

```bash
npm install
npm start          # http://localhost:4200
```

El backend debe estar encendido en `http://localhost:8080` (se configura en `src/app/core/config.ts`).

## Estructura

```
src/app
├── core/          servicios HTTP (GET/POST/PUT/DELETE), auth, interceptor, guards, modelos
├── auth/          login y registro
├── layout/        menú lateral (shell)
├── dashboard/     panel con indicadores
├── productos/     CRUD de productos
├── categorias/    CRUD de categorías
├── proveedores/   CRUD de proveedores
└── movimientos/   entradas / salidas de stock (kardex)
```

## Compilar para producción

```bash
npm run build
```
