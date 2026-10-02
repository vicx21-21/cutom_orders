# Sistema de Gestión de Inventario y Ventas

Aplicación para la gestión de inventario, registro de productos, precios, control de ventas (con detalle de artículos), clientes y proveedores.

## 🚀 Características

* **Control de Inventario**: Registro y gestión de productos, categorías y precios.
* **Gestión de Ventas**: Registro de ventas detalladas y clientes.
* **Proveedores**: Administración de información de proveedores (*suppliers*).
* **Control de Acceso**: Autenticación de usuarios/empleados para el ingreso al sistema.

## 🛠️ Requisitos Previos

* **PostgreSQL** y **pgAdmin 4** instalados.
* Entorno de ejecución de **Java (JDK)** instalado.

## ⚙️ Configuración de la Base de Datos

1. Abre **pgAdmin 4** y conéctate a tu servidor de PostgreSQL.
2. Crea una base de datos con el nombre exacto: `data_model_with_cutom_orders`
3. Ejecuta los scripts SQL incluidos en la carpeta en el **siguiente orden estricto**:
   1. `crear_BD`
   2. `crear_Empleado` *(Contraseña por defecto: `empleado2025`)*
   3. `crear_Prod_Tipos`
   4. `agregar_Suppliers`
   5. `agregar_Prod`
   6. `agregar_Client`

## 🏃‍♂️ Ejecución y Login

1. Ejecuta o corre la clase principal **`Launcher`** en tu entorno de desarrollo (IDE) o desde terminal.
2. Inicia sesión con las siguientes credenciales de prueba:
   * **Usuario:** `empleado2`
   * **Contraseña:** `empleado2025`