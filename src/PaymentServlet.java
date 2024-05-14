import javax.naming.InitialContext;
import javax.naming.NamingException;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.*;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "PaymentServlet", urlPatterns = "/api/payment")
public class PaymentServlet extends HttpServlet {

    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        JsonObject responseJsonObject = new JsonObject();

        String credit = request.getParameter("creditcard");
        String firstname = request.getParameter("Firstname");
        String lastname = request.getParameter("Lastname");
        String expdate = request.getParameter("expdate");

        User userinfo = (User) session.getAttribute("user");
        if (userinfo == null || !userinfo.getfName().equals(firstname) || !userinfo.getlName().equals(lastname)) {
            responseJsonObject.addProperty("status", "fail");
            responseJsonObject.addProperty("message", "Please check your info and reenter");
        } else {
            try (Connection conn = dataSource.getConnection()) {
                String query = "SELECT expiration FROM creditcards WHERE id = ?";
                try (PreparedStatement statement = conn.prepareStatement(query)) {
                    statement.setString(1, credit);
                    try (ResultSet rs = statement.executeQuery()) {
                        if (rs.next() && rs.getString("expiration").equals(expdate)) {
                            responseJsonObject.addProperty("status", "success");
                            responseJsonObject.addProperty("message", "Payment successful.");
                        } else {
                            responseJsonObject.addProperty("status", "fail");
                            responseJsonObject.addProperty("message", "Invalid credit card information.");
                        }
                    }
                }
            } catch (SQLException e) {
                responseJsonObject.addProperty("status", "error");
                responseJsonObject.addProperty("message", "An error occurred while processing your request.");
                e.printStackTrace();
            }
        }

        response.getWriter().write(responseJsonObject.toString());
    }
}