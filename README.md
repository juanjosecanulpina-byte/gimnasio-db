# Sistema de administración de un gimnasio

Proyecto Integrador - Diseño de Base de Datos
Facultad de Ingeniería en Software, Universidad Autónoma de Yucatán

**Autor:** Juan Jose Canul Piña
**Profesora:** Erika Rossana Llanes Castro

## Descripción

Aplicación de consola en Java (JDBC) para administrar un gimnasio sobre una base de datos MySQL normalizada hasta Tercera Forma Normal (3FN).

Operaciones disponibles (CRUD sobre las tablas relacionadas `CLIENTE` y `MEMBRESIA`):

| Opción | Operación |
|--------|-----------|
| 1 | Registrar cliente con su membresía (Create, en una sola transacción) |
| 2 | Consultar clientes y membresías (Read, JOIN de 2 tablas) |
| 3 | Actualizar datos del cliente (Update) |
| 4 | Actualizar datos de la membresía (Update) |
| 5 | Eliminar cliente (Delete; sus membresías y pagos se eliminan por `ON DELETE CASCADE`) |
| 6 | Registrar un pago de una membresía |
| 7 | Consultar pagos por cliente (JOIN de 3 tablas: `CLIENTE`, `MEMBRESIA`, `PAGO`) |

## Estructura del repositorio

```
.
├── gimnasio_db.sql                  Script DDL completo de la base de datos
├── pom.xml                          Configuración de Maven (Java 17, mysql-connector-j)
├── src/main/java/com/gimnasio/
│   └── App.java                     Código fuente de la aplicación
├── .gitignore
└── README.md
```

## Requisitos

- JDK 17 o superior
- Maven 3.8 o superior
- MySQL Server 8.x (y, opcionalmente, MySQL Workbench)

## Cómo configurar y ejecutar

### 1. Crear la base de datos

Con el servicio de MySQL iniciado, ejecuta el script `gimnasio_db.sql`.

- **Desde MySQL Workbench:** `File → Open SQL Script…`, selecciona `gimnasio_db.sql` y ejecútalo completo con el rayo (⚡).
- **Desde la terminal:**
  ```
  mysql -u root -p < gimnasio_db.sql
  ```

El script elimina y vuelve a crear la base `gimnasio_db` con sus 8 tablas: `CLIENTE`, `ENTRENADOR`, `MAQUINA`, `MEMBRESIA`, `PAGO`, `CLASE`, `INSCRIPCION_CLASE` y `ASISTENCIA`.

### 2. Configurar la conexión

La aplicación lee la conexión de variables de entorno (no se guardan credenciales en el código):

| Variable | Descripción | Valor por defecto |
|----------|-------------|-------------------|
| `DB_PASSWORD` | Contraseña de MySQL | (vacía) |
| `DB_USER` | Usuario de MySQL | `root` |
| `DB_URL` | URL JDBC | `jdbc:mysql://localhost:3306/gimnasio_db` |

Define al menos `DB_PASSWORD` antes de ejecutar:

- **Windows (cmd):** `set DB_PASSWORD=tu_contraseña`
- **Windows (PowerShell):** `$env:DB_PASSWORD="tu_contraseña"`
- **Linux / macOS:** `export DB_PASSWORD=tu_contraseña`

### 3. Ejecutar

Desde la carpeta raíz del proyecto (donde está el `pom.xml`), en la misma terminal donde definiste la variable:

```
mvn compile exec:java -Dexec.mainClass=com.gimnasio.App
```

En PowerShell, escribe el argumento entre comillas: `"-Dexec.mainClass=com.gimnasio.App"`.

Aparecerá el menú del gimnasio en consola.

## Diseño de la base de datos

El esquema fue normalizado hasta 3FN. Las tablas `CLASE` e `INSCRIPCION_CLASE` se descompusieron para eliminar dependencias transitivas (se quitaron los atributos `nombre_entrenador`, `especialidad_entrenador` y `nombre_clase`, que ya existen en `ENTRENADOR` y `CLASE`).

Reglas de integridad referencial:

- `MEMBRESIA`, `PAGO`, `INSCRIPCION_CLASE` y `ASISTENCIA`: `ON DELETE CASCADE` (al eliminar un cliente se eliminan sus registros asociados).
- `CLASE` (entrenador) y `ASISTENCIA` (máquina): `ON DELETE SET NULL` (se conserva el registro).
