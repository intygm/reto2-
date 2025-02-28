import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class VentanaAdmin extends JFrame {
    private static final String PASSWORD = "admin"; // Cambia la contraseña aquí
    private JTable tablaClientes;
    private DefaultTableModel modeloTabla;
    private JTextField campoBusqueda;

    public VentanaAdmin() {
        String input = JOptionPane.showInputDialog("Ingrese la contraseña de administrador:");

        if (input == null || !input.equals(PASSWORD)) {
            JOptionPane.showMessageDialog(null, "Acceso denegado.");
            return;
        }

        setTitle("Lista de Clientes y Alquileres");
        setSize(800, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Crear campo de búsqueda y botón
        JPanel panelSuperior = new JPanel();
        panelSuperior.setLayout(new FlowLayout(FlowLayout.LEFT));
        
        JLabel etiquetaBusqueda = new JLabel("Buscar:");
        campoBusqueda = new JTextField(20);
        JButton botonBuscar = new JButton("Buscar");

        // Acción del botón de búsqueda
        botonBuscar.addActionListener(e -> buscarDatos());

        // Crear botón de eliminar
        JButton botonEliminar = new JButton("Eliminar");
        
        // Acción del botón de eliminar
        botonEliminar.addActionListener(e -> eliminarSeleccionado());

        // Añadir componentes al panel superior
        panelSuperior.add(etiquetaBusqueda);
        panelSuperior.add(campoBusqueda);
        panelSuperior.add(botonBuscar);
        panelSuperior.add(botonEliminar);

        // Crear modelo de tabla
        String[] columnas = {"ID Cliente", "Nombre", "Correo", "Teléfono", 
                             "Coche", "Año", "Precio/día", "Días", "Total (€)", "Fecha"};
        modeloTabla = new DefaultTableModel(columnas, 0);
        tablaClientes = new JTable(modeloTabla);
        add(new JScrollPane(tablaClientes), BorderLayout.CENTER);
        add(panelSuperior, BorderLayout.NORTH);

        // Cargar datos desde la base de datos
        cargarDatos();

        setVisible(true);
    }

    private void cargarDatos() {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            JOptionPane.showMessageDialog(this, "Error de conexión.");
            return;
        }

        try {
            String sql = "SELECT c.id AS ClienteID, c.nombre, c.correo, c.telefono, " +
                         "co.marca, co.modelo, co.anio, co.precio_dia, " +
                         "a.dias, a.precio_total, a.fecha_alquiler " +
                         "FROM Cliente c " +
                         "JOIN Alquiler a ON c.id = a.cliente_id " +
                         "JOIN Coche co ON a.coche_id = co.id " +
                         "ORDER BY a.fecha_alquiler DESC";

            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            // Limpiar los datos de la tabla antes de agregar nuevos
            modeloTabla.setRowCount(0);

            while (rs.next()) {
                modeloTabla.addRow(new Object[]{
                        rs.getInt("ClienteID"),
                        rs.getString("nombre"),
                        rs.getString("correo"),
                        rs.getString("telefono"),
                        rs.getString("marca") + " " + rs.getString("modelo"),
                        rs.getInt("anio"),
                        rs.getDouble("precio_dia"),
                        rs.getInt("dias"),
                        rs.getDouble("precio_total"),
                        rs.getTimestamp("fecha_alquiler")
                });
            }

            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al cargar los datos.");
        }
    }

    private void buscarDatos() {
        String busqueda = campoBusqueda.getText().trim().toLowerCase();
        if (busqueda.isEmpty()) {
            cargarDatos();  // Si no hay texto, cargar todos los datos
            return;
        }

        // Filtrar datos según la búsqueda
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            JOptionPane.showMessageDialog(this, "Error de conexión.");
            return;
        }

        try {
            String sql = "SELECT c.id AS ClienteID, c.nombre, c.correo, c.telefono, " +
                         "co.marca, co.modelo, co.anio, co.precio_dia, " +
                         "a.dias, a.precio_total, a.fecha_alquiler " +
                         "FROM Cliente c " +
                         "JOIN Alquiler a ON c.id = a.cliente_id " +
                         "JOIN Coche co ON a.coche_id = co.id " +
                         "WHERE LOWER(c.nombre) LIKE ? " +
                         "OR LOWER(c.correo) LIKE ? " +
                         "ORDER BY a.fecha_alquiler DESC";

            PreparedStatement stmt = conn.prepareStatement(sql);
            String query = "%" + busqueda + "%";
            stmt.setString(1, query);
            stmt.setString(2, query);

            ResultSet rs = stmt.executeQuery();

            // Limpiar los datos de la tabla antes de agregar nuevos
            modeloTabla.setRowCount(0);

            while (rs.next()) {
                modeloTabla.addRow(new Object[]{
                        rs.getInt("ClienteID"),
                        rs.getString("nombre"),
                        rs.getString("correo"),
                        rs.getString("telefono"),
                        rs.getString("marca") + " " + rs.getString("modelo"),
                        rs.getInt("anio"),
                        rs.getDouble("precio_dia"),
                        rs.getInt("dias"),
                        rs.getDouble("precio_total"),
                        rs.getTimestamp("fecha_alquiler")
                });
            }

            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al realizar la búsqueda.");
        }
    }

    private void eliminarSeleccionado() {
        int rowIndex = tablaClientes.getSelectedRow();
        if (rowIndex == -1) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione un registro para eliminar.");
            return;
        }

        int clienteID = (Integer) modeloTabla.getValueAt(rowIndex, 0);

        int confirm = JOptionPane.showConfirmDialog(this, 
            "¿Está seguro de que desea eliminar el alquiler del cliente con ID " + clienteID + "?",
            "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            Connection conn = DatabaseConnection.getConnection();
            if (conn == null) {
                JOptionPane.showMessageDialog(this, "Error de conexión.");
                return;
            }

            try {
                String sql = "DELETE FROM Alquiler WHERE cliente_id = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, clienteID);
                int rowsAffected = stmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "El alquiler ha sido eliminado.");
                    // Actualizar la tabla
                    cargarDatos();
                } else {
                    JOptionPane.showMessageDialog(this, "No se pudo eliminar el alquiler.");
                }

                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al eliminar el alquiler.");
            }
        }
    }
}



