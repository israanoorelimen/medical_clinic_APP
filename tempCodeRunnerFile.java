import java.sql.Connection;
import java.sql.DriverManager;

public class Connexion {

    static String url  = "jdbc:oracle:thin:@localhost:1521:XE";
    static String user = "system";  // mon username Oracle
    static String pass = "oracle";    // le mot de passe Oracle

    public static Connection getConnexion() {
        Connection con = null;
        try {
            con = DriverManager.getConnection(url, user, pass);
            System.out.println("Connexion reussie !");
        } catch (Exception e) {
            System.out.println("Erreur : " + e.getMessage());
        }
        return con;
    }
}