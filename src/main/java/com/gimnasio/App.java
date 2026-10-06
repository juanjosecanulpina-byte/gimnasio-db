package com.gimnasio;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

/**
 * Sistema de administración de un gimnasio (Proyecto Integrador).
 * CRUD sobre las tablas relacionadas CLIENTE y MEMBRESIA, registro de pagos
 * y una consulta que combina CLIENTE, MEMBRESIA y PAGO.
 *
 * La contraseña de MySQL se lee de la variable de entorno DB_PASSWORD.
 */
public class App {

    private static final Scanner SC = new Scanner(System.in);

    // ------------------------------------------------------------------
    // Conexión
    // ------------------------------------------------------------------
    static Connection conectar() throws SQLException {
        String url = System.getenv().getOrDefault("DB_URL", "jdbc:mysql://localhost:3306/gimnasio_db");
        String user = System.getenv().getOrDefault("DB_USER", "root");
        String password = System.getenv().getOrDefault("DB_PASSWORD", "");
        return DriverManager.getConnection(url, user, password);
    }

    // ------------------------------------------------------------------
    // Utilidades de lectura
    // ------------------------------------------------------------------
    static String leerTexto(String mensaje) {
        System.out.print(mensaje);
        return SC.nextLine().trim();
    }

    static int leerEntero(String mensaje) {
        while (true) {
            try {
                return Integer.parseInt(leerTexto(mensaje));
            } catch (NumberFormatException e) {
                System.out.println("Escribe un número entero válido.");
            }
        }
    }

    static double leerDecimal(String mensaje) {
        while (true) {
            try {
                return Double.parseDouble(leerTexto(mensaje));
            } catch (NumberFormatException e) {
                System.out.println("Escribe un número válido (ejemplo: 450.00).");
            }
        }
    }

    /** Pide una fecha con formato AAAA-MM-DD y la valida. */
    static Date leerFecha(String mensaje) {
        while (true) {
            try {
                return Date.valueOf(leerTexto(mensaje));
            } catch (IllegalArgumentException e) {
                System.out.println("Formato inválido. Usa AAAA-MM-DD (ejemplo: 2026-10-06).");
            }
        }
    }

    // ------------------------------------------------------------------
    // CREATE: cliente + membresía en una sola transacción
    // ------------------------------------------------------------------
    static void registrarClienteConMembresia() {
        String nombre = leerTexto("Nombre: ");
        String apellido = leerTexto("Apellido: ");
        String telefono = leerTexto("Teléfono: ");
        String correo = leerTexto("Correo: ");
        Date fechaNacimiento = leerFecha("Fecha de nacimiento (AAAA-MM-DD): ");
        String tipo = leerTexto("Tipo de membresía (Mensual, Anual...): ");
        double costo = leerDecimal("Costo mensual: ");
        Date inicio = leerFecha("Fecha de inicio (AAAA-MM-DD): ");
        Date fin = leerFecha("Fecha de fin (AAAA-MM-DD): ");

        String sqlCliente = "INSERT INTO CLIENTE (nombre, apellido, telefono, correo, fecha_nacimiento) "
                + "VALUES (?, ?, ?, ?, ?)";
        String sqlMembresia = "INSERT INTO MEMBRESIA (id_cliente, tipo_membresia, costo_mensual, "
                + "fecha_inicio, fecha_fin) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = conectar()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psCliente = conn.prepareStatement(sqlCliente, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement psMembresia = conn.prepareStatement(sqlMembresia)) {

                psCliente.setString(1, nombre);
                psCliente.setString(2, apellido);
                psCliente.setString(3, telefono);
                psCliente.setString(4, correo);
                psCliente.setDate(5, fechaNacimiento);
                psCliente.executeUpdate();

                int idCliente;
                try (ResultSet rs = psCliente.getGeneratedKeys()) {
                    rs.next();
                    idCliente = rs.getInt(1);
                }

                psMembresia.setInt(1, idCliente);
                psMembresia.setString(2, tipo);
                psMembresia.setDouble(3, costo);
                psMembresia.setDate(4, inicio);
                psMembresia.setDate(5, fin);
                psMembresia.executeUpdate();

                conn.commit();
                System.out.println("Cliente " + idCliente + " registrado con su membresía.");
            } catch (SQLException e) {
                conn.rollback();
                System.out.println("Error al registrar (se deshizo la operación): " + e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println("Error de conexión: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // READ: clientes con su membresía (JOIN de 2 tablas)
    // ------------------------------------------------------------------
    static void consultarClientesConMembresia() {
        String sql = "SELECT c.id_cliente, c.nombre, c.apellido, c.telefono, c.correo, "
                + "m.id_membresia, m.tipo_membresia, m.costo_mensual, m.fecha_inicio, m.fecha_fin "
                + "FROM CLIENTE c JOIN MEMBRESIA m ON c.id_cliente = m.id_cliente "
                + "ORDER BY c.id_cliente";

        try (Connection conn = conectar();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            boolean hayDatos = false;
            System.out.println("\n--- Clientes y membresías ---");
            while (rs.next()) {
                hayDatos = true;
                System.out.printf("Cliente %d | %s %s | Tel: %s | %s%n"
                                + "   Membresía %d | %s | $%.2f | %s a %s%n",
                        rs.getInt("id_cliente"), rs.getString("nombre"), rs.getString("apellido"),
                        rs.getString("telefono"), rs.getString("correo"),
                        rs.getInt("id_membresia"), rs.getString("tipo_membresia"),
                        rs.getDouble("costo_mensual"), rs.getDate("fecha_inicio"), rs.getDate("fecha_fin"));
            }
            if (!hayDatos) {
                System.out.println("No hay clientes registrados.");
            }
        } catch (SQLException e) {
            System.out.println("Error al consultar: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // UPDATE: datos del cliente
    // ------------------------------------------------------------------
    static void actualizarCliente() {
        int id = leerEntero("ID del cliente a actualizar: ");
        String telefono = leerTexto("Nuevo teléfono: ");
        String correo = leerTexto("Nuevo correo: ");

        String sql = "UPDATE CLIENTE SET telefono = ?, correo = ? WHERE id_cliente = ?";
        try (Connection conn = conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, telefono);
            ps.setString(2, correo);
            ps.setInt(3, id);
            int filas = ps.executeUpdate();
            System.out.println(filas > 0 ? "Cliente actualizado." : "No existe un cliente con ese ID.");
        } catch (SQLException e) {
            System.out.println("Error al actualizar cliente: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // UPDATE: datos de la membresía
    // ------------------------------------------------------------------
    static void actualizarMembresia() {
        int id = leerEntero("ID de la membresía a actualizar: ");
        String tipo = leerTexto("Nuevo tipo de membresía: ");
        double costo = leerDecimal("Nuevo costo mensual: ");

        String sql = "UPDATE MEMBRESIA SET tipo_membresia = ?, costo_mensual = ? WHERE id_membresia = ?";
        try (Connection conn = conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tipo);
            ps.setDouble(2, costo);
            ps.setInt(3, id);
            int filas = ps.executeUpdate();
            System.out.println(filas > 0 ? "Membresía actualizada." : "No existe una membresía con ese ID.");
        } catch (SQLException e) {
            System.out.println("Error al actualizar membresía: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // DELETE: cliente (sus membresías y pagos se eliminan por ON DELETE CASCADE)
    // ------------------------------------------------------------------
    static void eliminarCliente() {
        int id = leerEntero("ID del cliente a eliminar: ");
        String confirmar = leerTexto("Se eliminará también su membresía y pagos. ¿Confirmas? (s/n): ");
        if (!confirmar.equalsIgnoreCase("s")) {
            System.out.println("Operación cancelada.");
            return;
        }

        String sql = "DELETE FROM CLIENTE WHERE id_cliente = ?";
        try (Connection conn = conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            int filas = ps.executeUpdate();
            System.out.println(filas > 0 ? "Cliente eliminado." : "No existe un cliente con ese ID.");
        } catch (SQLException e) {
            System.out.println("Error al eliminar: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // CREATE: pago de una membresía (necesario para la consulta de 3 tablas)
    // ------------------------------------------------------------------
    static void registrarPago() {
        int idMembresia = leerEntero("ID de la membresía: ");
        Date fecha = leerFecha("Fecha de pago (AAAA-MM-DD): ");
        double monto = leerDecimal("Monto: ");
        String metodo = leerTexto("Método de pago (Efectivo, Tarjeta...): ");

        String sql = "INSERT INTO PAGO (id_membresia, fecha_pago, monto, metodo_pago) VALUES (?, ?, ?, ?)";
        try (Connection conn = conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idMembresia);
            ps.setDate(2, fecha);
            ps.setDouble(3, monto);
            ps.setString(4, metodo);
            ps.executeUpdate();
            System.out.println("Pago registrado.");
        } catch (SQLException e) {
            System.out.println("Error al registrar pago: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // READ: consulta que combina 3 tablas (CLIENTE + MEMBRESIA + PAGO)
    // ------------------------------------------------------------------
    static void consultarPagosPorCliente() {
        String sql = "SELECT c.id_cliente, c.nombre, c.apellido, m.tipo_membresia, "
                + "p.fecha_pago, p.monto, p.metodo_pago "
                + "FROM CLIENTE c "
                + "JOIN MEMBRESIA m ON c.id_cliente = m.id_cliente "
                + "JOIN PAGO p ON m.id_membresia = p.id_membresia "
                + "ORDER BY c.id_cliente, p.fecha_pago";

        try (Connection conn = conectar();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            boolean hayDatos = false;
            System.out.println("\n--- Pagos por cliente (CLIENTE + MEMBRESIA + PAGO) ---");
            while (rs.next()) {
                hayDatos = true;
                System.out.printf("%d | %s %s | %s | %s | $%.2f | %s%n",
                        rs.getInt("id_cliente"), rs.getString("nombre"), rs.getString("apellido"),
                        rs.getString("tipo_membresia"), rs.getDate("fecha_pago"),
                        rs.getDouble("monto"), rs.getString("metodo_pago"));
            }
            if (!hayDatos) {
                System.out.println("No hay pagos registrados todavía (usa la opción 6).");
            }
        } catch (SQLException e) {
            System.out.println("Error al consultar: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // Menú principal
    // ------------------------------------------------------------------
    public static void main(String[] args) {
        int opcion;
        do {
            System.out.println("""

                    ===== GIMNASIO =====
                    1. Registrar cliente con membresía   (Create)
                    2. Consultar clientes y membresías   (Read)
                    3. Actualizar cliente                (Update)
                    4. Actualizar membresía              (Update)
                    5. Eliminar cliente                  (Delete)
                    6. Registrar pago
                    7. Consultar pagos (3 tablas)
                    0. Salir""");
            opcion = leerEntero("Elige una opción: ");

            switch (opcion) {
                case 1 -> registrarClienteConMembresia();
                case 2 -> consultarClientesConMembresia();
                case 3 -> actualizarCliente();
                case 4 -> actualizarMembresia();
                case 5 -> eliminarCliente();
                case 6 -> registrarPago();
                case 7 -> consultarPagosPorCliente();
                case 0 -> System.out.println("Hasta luego.");
                default -> System.out.println("Opción no válida.");
            }
        } while (opcion != 0);
    }
}
