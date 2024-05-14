import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.io.PrintWriter;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.jasypt.util.password.StrongPasswordEncryptor;

@WebServlet(name = "LoginServlet", urlPatterns = "/api/login")
public class LoginServlet extends HttpServlet {

    private DataSource dataSource;
    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }
    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        PrintWriter out = response.getWriter();

        try {
            Connection dbCon = dataSource.getConnection();
            String query = "SELECT * FROM customers WHERE email = ?";
            PreparedStatement statement = dbCon.prepareStatement(query);
            statement.setString(1, username);
            ResultSet rs = statement.executeQuery();

            String employ = "SELECT * FROM employees WHERE email = ?";
            PreparedStatement stateEmploy = dbCon.prepareStatement(employ);
            stateEmploy.setString(1, username);
            ResultSet rs2 = stateEmploy.executeQuery();

            boolean noUsername = false;
            boolean success = false;
            boolean accessDashboard = false;
            if (rs2.next()) {
                accessDashboard = new StrongPasswordEncryptor().checkPassword(password, rs2.getString("password"));
            }
            else if (rs.next()) {
                success = new StrongPasswordEncryptor().checkPassword(password, rs.getString("password"));
            } else {
                noUsername = true;
            }

            String fname = "", lname = "", address = "", ccid = "";
            int userId = 0;

            JsonObject responseJsonObject = new JsonObject();
            if (accessDashboard) {
                responseJsonObject.addProperty("status", "employee");
                responseJsonObject.addProperty("message", "employee success");
            }
            else if (username.equals("")) {
                responseJsonObject.addProperty("status", "fail");
                responseJsonObject.addProperty("message", "Missing username");
            }
            else if (noUsername) {
                responseJsonObject.addProperty("status", "fail");
                responseJsonObject.addProperty("message", "Invalid username");
            }
            else if (success) { // Assuming password comparison for demonstration.
                fname = rs.getString("firstname");
                lname = rs.getString("lastname");
                userId = rs.getInt("id");
                ccid = rs.getString("ccId");
                address = rs.getString("address");
                request.getSession().setAttribute("user", new User(fname, lname, userId, username, address, ccid, password));
                responseJsonObject.addProperty("status", "success");
                responseJsonObject.addProperty("message", "success");
            }
            else {
                responseJsonObject.addProperty("status", "fail");
                responseJsonObject.addProperty("message", "Invalid password");
                request.getServletContext().log("Login failed for user: " + username);
            }


            response.getWriter().write(responseJsonObject.toString());

        } catch (Exception e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());
            response.setStatus(500);
        } finally {
            out.close();
        }

    }
}
