import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class Main {
    public static void main(String[] args) {

        Connection con = Connexion.getConnexion();

        try {
            String sql = "SELECT * FROM Patient";
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {
                System.out.println(
                    rs.getInt("Num_Patient") + " | " +
                    rs.getString("Nom")      + " | " +
                    rs.getString("Prenom")   + " | " +
                    rs.getString("Telephone")
                );
            }

        } catch (Exception e) {
            System.out.println("Erreur : " + e.getMessage());
        }
    }
}