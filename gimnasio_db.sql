-- =====================================================================
-- Proyecto Integrador - Sistema de administración de un gimnasio
-- Script DDL final (esquema normalizado en 3FN) - MySQL
-- Autor: Juan Jose Canul Piña
-- =====================================================================

DROP DATABASE IF EXISTS gimnasio_db;
CREATE DATABASE gimnasio_db;
USE gimnasio_db;

-- ---------------------------------------------------------------------
-- CLIENTE: personas inscritas al gimnasio.
-- PK: id_cliente. El correo es único.
-- ---------------------------------------------------------------------
CREATE TABLE CLIENTE (
    id_cliente       INT AUTO_INCREMENT PRIMARY KEY,
    nombre           VARCHAR(50)  NOT NULL,
    apellido         VARCHAR(50)  NOT NULL,
    telefono         VARCHAR(15),
    correo           VARCHAR(100) UNIQUE,
    fecha_nacimiento DATE
);

-- ---------------------------------------------------------------------
-- ENTRENADOR: personal que imparte las clases.
-- PK: id_entrenador.
-- ---------------------------------------------------------------------
CREATE TABLE ENTRENADOR (
    id_entrenador INT AUTO_INCREMENT PRIMARY KEY,
    nombre        VARCHAR(50) NOT NULL,
    apellido      VARCHAR(50) NOT NULL,
    especialidad  VARCHAR(50),
    telefono      VARCHAR(15)
);

-- ---------------------------------------------------------------------
-- MAQUINA: inventario de equipo del gimnasio.
-- PK: id_maquina.
-- ---------------------------------------------------------------------
CREATE TABLE MAQUINA (
    id_maquina        INT AUTO_INCREMENT PRIMARY KEY,
    nombre            VARCHAR(50) NOT NULL,
    tipo              VARCHAR(30),
    fecha_adquisicion DATE,
    estado            VARCHAR(20) DEFAULT 'activa'
);

-- ---------------------------------------------------------------------
-- MEMBRESIA: plan contratado por un cliente.
-- FK: id_cliente -> CLIENTE. Si se elimina el cliente, se eliminan
-- también sus membresías (ON DELETE CASCADE).
-- ---------------------------------------------------------------------
CREATE TABLE MEMBRESIA (
    id_membresia   INT AUTO_INCREMENT PRIMARY KEY,
    id_cliente     INT           NOT NULL,
    tipo_membresia VARCHAR(30)   NOT NULL,
    costo_mensual  DECIMAL(8,2)  NOT NULL,
    fecha_inicio   DATE          NOT NULL,
    fecha_fin      DATE,
    FOREIGN KEY (id_cliente) REFERENCES CLIENTE(id_cliente)
        ON DELETE CASCADE
);

-- ---------------------------------------------------------------------
-- PAGO: historial de pagos de una membresía.
-- FK: id_membresia -> MEMBRESIA (ON DELETE CASCADE).
-- ---------------------------------------------------------------------
CREATE TABLE PAGO (
    id_membresia INT          NOT NULL,
    id_pago      INT AUTO_INCREMENT PRIMARY KEY,
    fecha_pago   DATE         NOT NULL,
    monto        DECIMAL(8,2) NOT NULL,
    metodo_pago  VARCHAR(20),
    FOREIGN KEY (id_membresia) REFERENCES MEMBRESIA(id_membresia)
        ON DELETE CASCADE
);

-- ---------------------------------------------------------------------
-- CLASE: actividad dirigida que imparte un entrenador (3FN: ya no
-- guarda nombre ni especialidad del entrenador).
-- FK: id_entrenador -> ENTRENADOR. Si se elimina el entrenador, la
-- clase se conserva con id_entrenador en NULL (ON DELETE SET NULL).
-- ---------------------------------------------------------------------
CREATE TABLE CLASE (
    id_clase      INT AUTO_INCREMENT PRIMARY KEY,
    nombre_clase  VARCHAR(50) NOT NULL,
    horario       VARCHAR(30),
    cupo_max      INT,
    id_entrenador INT,
    FOREIGN KEY (id_entrenador) REFERENCES ENTRENADOR(id_entrenador)
        ON DELETE SET NULL
);

-- ---------------------------------------------------------------------
-- INSCRIPCION_CLASE: relación entre clientes y clases (3FN: ya no
-- guarda nombre_clase).
-- FK: id_cliente -> CLIENTE, id_clase -> CLASE (ON DELETE CASCADE).
-- ---------------------------------------------------------------------
CREATE TABLE INSCRIPCION_CLASE (
    id_inscripcion    INT AUTO_INCREMENT PRIMARY KEY,
    id_cliente        INT  NOT NULL,
    id_clase          INT  NOT NULL,
    fecha_inscripcion DATE NOT NULL,
    FOREIGN KEY (id_cliente) REFERENCES CLIENTE(id_cliente)
        ON DELETE CASCADE,
    FOREIGN KEY (id_clase) REFERENCES CLASE(id_clase)
        ON DELETE CASCADE
);

-- ---------------------------------------------------------------------
-- ASISTENCIA: registro de cada visita de un cliente.
-- FK: id_cliente -> CLIENTE (ON DELETE CASCADE),
--     id_maquina -> MAQUINA (ON DELETE SET NULL).
-- ---------------------------------------------------------------------
CREATE TABLE ASISTENCIA (
    id_asistencia INT AUTO_INCREMENT PRIMARY KEY,
    id_cliente    INT  NOT NULL,
    id_maquina    INT,
    fecha         DATE NOT NULL,
    hora_entrada  TIME NOT NULL,
    hora_salida   TIME,
    FOREIGN KEY (id_cliente) REFERENCES CLIENTE(id_cliente)
        ON DELETE CASCADE,
    FOREIGN KEY (id_maquina) REFERENCES MAQUINA(id_maquina)
        ON DELETE SET NULL
);
