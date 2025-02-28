import java.awt.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;

public class VentanaClientes extends JFrame {
    private JComboBox<String> comboCoches;
    private JLabel lblPrecio;
    private JTextField txtDias, txtNombre, txtCorreo, txtTelefono;
    private JButton btnRegistrar;

    private final Map<String, Integer> cochesDisponibles = new HashMap<>();
    private final Map<String, Double> preciosCoches = new HashMap<>();

    public VentanaClientes() {
        setTitle("Registro de Clientes");
        setSize(400, 350);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(8, 2, 10, 10));

        // Obtener coches de la base de datos
        cargarCochesDesdeBD();

        add(new JLabel("Modelo del coche:"));
        comboCoches = new JComboBox<>(cochesDisponibles.keySet().toArray(new String[0]));
        comboCoches.addActionListener(e -> actualizarPrecio());
        add(comboCoches);

        add(new JLabel("Precio por día (€):"));
        lblPrecio = new JLabel(preciosCoches.get(comboCoches.getSelectedItem()) + " €");
        add(lblPrecio);

        add(new JLabel("Días de uso:"));
        txtDias = new JTextField();
        add(txtDias);

        add(new JLabel("Nombre:"));
        txtNombre = new JTextField();
        add(txtNombre);

        add(new JLabel("Correo:"));
        txtCorreo = new JTextField();
        add(txtCorreo);

        add(new JLabel("Teléfono:"));
        txtTelefono = new JTextField();
        add(txtTelefono);

        btnRegistrar = new JButton("Registrar Alquiler");
        btnRegistrar.addActionListener(e -> registrarAlquiler());
        add(btnRegistrar);

        setVisible(true);
    }

    private void actualizarPrecio() {
        String cocheSeleccionado = (String) comboCoches.getSelectedItem();
        lblPrecio.setText(preciosCoches.get(cocheSeleccionado) + " €");
    }

    private void cargarCochesDesdeBD() {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            JOptionPane.showMessageDialog(this, "Error de conexión con la base de datos.");
            return;
        }

        try {
            String sql = "SELECT id, modelo, precio_dia FROM Coche";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                cochesDisponibles.put(rs.getString("modelo"), rs.getInt("id"));
                preciosCoches.put(rs.getString("modelo"), rs.getDouble("precio_dia"));
            }

            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al cargar los coches.");
        }
    }

    private void registrarAlquiler() {
        String nombre = txtNombre.getText();
        String correo = txtCorreo.getText();
        String telefono = txtTelefono.getText();
        String cocheSeleccionado = (String) comboCoches.getSelectedItem();
        int dias = Integer.parseInt(txtDias.getText());
        double precioPorDia = preciosCoches.get(cocheSeleccionado);
        double precioTotal = dias * precioPorDia;
        int cocheId = cochesDisponibles.get(cocheSeleccionado);

        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            JOptionPane.showMessageDialog(this, "Error de conexión.");
            return;
        }

        try {
            // Insertar cliente si no existe
            int clienteId = -1;
            String checkCliente = "SELECT id FROM Cliente WHERE correo = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkCliente);
            checkStmt.setString(1, correo);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                clienteId = rs.getInt("id"); // Cliente ya existe
            } else {
                String sqlCliente = "INSERT INTO Cliente (nombre, correo, telefono) VALUES (?, ?, ?)";
                PreparedStatement stmtCliente = conn.prepareStatement(sqlCliente, Statement.RETURN_GENERATED_KEYS);
                stmtCliente.setString(1, nombre);
                stmtCliente.setString(2, correo);
                stmtCliente.setString(3, telefono);
                stmtCliente.executeUpdate();
                ResultSet generatedKeys = stmtCliente.getGeneratedKeys();
                if (generatedKeys.next()) {
                    clienteId = generatedKeys.getInt(1);
                }
            }

            // Insertar alquiler
            String sqlAlquiler = "INSERT INTO Alquiler (coche_id, cliente_id, dias, precio_total) VALUES (?, ?, ?, ?)";
            PreparedStatement stmtAlquiler = conn.prepareStatement(sqlAlquiler);
            stmtAlquiler.setInt(1, cocheId);
            stmtAlquiler.setInt(2, clienteId);
            stmtAlquiler.setInt(3, dias);
            stmtAlquiler.setDouble(4, precioTotal);
            stmtAlquiler.executeUpdate();

            JOptionPane.showMessageDialog(this, "Alquiler registrado correctamente.");
            conn.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al registrar el alquiler.");
        }
    }
}

