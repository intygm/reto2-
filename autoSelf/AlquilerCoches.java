import javax.swing.*;

public class AlquilerCoches extends JFrame {

    public AlquilerCoches() {
        setTitle("Alquiler de Coches");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new java.awt.GridLayout(2, 1, 10, 10));

        JButton btnClientes = new JButton("Clientes");
        JButton btnAdmin = new JButton("Administrador");

        btnClientes.addActionListener(e -> new VentanaClientes());
        btnAdmin.addActionListener(e -> new VentanaAdmin());

        add(btnClientes);
        add(btnAdmin);

        setVisible(true);
    }

    public static void main(String[] args) {
        new AlquilerCoches();
    }
}
