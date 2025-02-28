 // Asegúrate de que la estructura del paquete sea correcta

 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 
 public class DatabaseConnection {
     private static final String URL = "jdbc:mysql://localhost:3306/AlquilerCochesDB";
     private static final String USER = "root"; // Cambia esto si tu usuario es diferente
     private static final String PASSWORD = ""; // Agrega tu contraseña si la tienes
 
     public static Connection getConnection() {
         try {
             return DriverManager.getConnection(URL, USER, PASSWORD);
         } catch (SQLException e) {
             e.printStackTrace();
             return null;
         }
     }
 }
 