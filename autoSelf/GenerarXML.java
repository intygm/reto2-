import java.io.File;
import java.sql.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.*;

public class GenerarXML {
    public static void main(String[] args) {
        try {
            // Conectar a la base de datos
            Connection conn = DatabaseConnection.getConnection();
            if (conn == null) {
                System.out.println("Error de conexión con la base de datos.");
                return;
            }

            // Crear el documento XML
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();

            // Crear el elemento raíz <Inscripciones>
            Element rootElement = doc.createElement("Inscripciones");
            doc.appendChild(rootElement);

            // Consulta SQL para obtener los datos de inscripción
            String sql = "SELECT c.id AS ClienteID, c.nombre, c.correo, c.telefono, " +
                         "co.marca, co.modelo, co.anio, co.precio_dia, " +
                         "a.dias, a.precio_total, a.fecha_alquiler " +
                         "FROM Cliente c " +
                         "JOIN Alquiler a ON c.id = a.cliente_id " +
                         "JOIN Coche co ON a.coche_id = co.id " +
                         "ORDER BY a.fecha_alquiler DESC";

            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            // Recorrer los resultados y construir el XML
            while (rs.next()) {
                // Crear elemento <Inscripcion>
                Element inscripcion = doc.createElement("Inscripcion");
                rootElement.appendChild(inscripcion);

                // Crear elemento Cliente
                Element cliente = doc.createElement("Cliente");
                cliente.setAttribute("id", String.valueOf(rs.getInt("ClienteID")));
                inscripcion.appendChild(cliente);

                Element nombre = doc.createElement("Nombre");
                nombre.appendChild(doc.createTextNode(rs.getString("nombre")));
                cliente.appendChild(nombre);

                Element correo = doc.createElement("Correo");
                correo.appendChild(doc.createTextNode(rs.getString("correo")));
                cliente.appendChild(correo);

                Element telefono = doc.createElement("Telefono");
                telefono.appendChild(doc.createTextNode(rs.getString("telefono")));
                cliente.appendChild(telefono);

                // Crear elemento Coche
                Element coche = doc.createElement("Coche");
                inscripcion.appendChild(coche);

                Element modelo = doc.createElement("Modelo");
                modelo.appendChild(doc.createTextNode(rs.getString("marca") + " " + rs.getString("modelo")));
                coche.appendChild(modelo);

                Element anio = doc.createElement("Año");
                anio.appendChild(doc.createTextNode(String.valueOf(rs.getInt("anio"))));
                coche.appendChild(anio);

                Element precioDia = doc.createElement("PrecioDia");
                precioDia.appendChild(doc.createTextNode(String.valueOf(rs.getDouble("precio_dia"))));
                coche.appendChild(precioDia);

                // Crear elemento Alquiler
                Element alquiler = doc.createElement("Alquiler");
                inscripcion.appendChild(alquiler);

                Element dias = doc.createElement("Dias");
                dias.appendChild(doc.createTextNode(String.valueOf(rs.getInt("dias"))));
                alquiler.appendChild(dias);

                Element precioTotal = doc.createElement("PrecioTotal");
                precioTotal.appendChild(doc.createTextNode(String.valueOf(rs.getDouble("precio_total"))));
                alquiler.appendChild(precioTotal);

                Element fecha = doc.createElement("Fecha");
                fecha.appendChild(doc.createTextNode(rs.getTimestamp("fecha_alquiler").toString()));
                alquiler.appendChild(fecha);
            }

            conn.close();

            // Guardar el archivo XML
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File("inscripciones.xml"));

            transformer.transform(source, result);

            System.out.println("Archivo XML 'inscripciones.xml' generado correctamente.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
