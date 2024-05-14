import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;


/**
 * A servlet that takes input from a html <form> and talks to MySQL moviedb,
 * generates output as a html <table>
 */

// Declaring a WebServlet called SearchServlet, which maps to url "/search"
@WebServlet(name = "AddStar", urlPatterns = "/api/addStar")
public class AddStar extends HttpServlet {

    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession();
        response.setContentType("application/json");    // Response mime type

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Retrieve data named "accessCount" from session, which count how many times the user requested before

        try {

            // Create a new connection to database
            Connection dbCon = dataSource.getConnection();

            // Retrieve parameter "name" from the http request, which refers to the value of <input name="name"> in index.html
            String starName = request.getParameter("starName");
            String birthYear = request.getParameter("birthYear");

            // Generate a SQL query
            String lastIdQuery = "SELECT id FROM stars ORDER BY id DESC LIMIT 1";
            String lastId = null;
            try (Statement statement = dbCon.createStatement();
                 ResultSet resultSet = statement.executeQuery(lastIdQuery)) {
                if (resultSet.next()) {
                    lastId = resultSet.getString("id");
                }
            }

            String newId = null;
            if (lastId != null) {
                String prefix = lastId.replaceAll("[0-9]", "");
                String numberPart = lastId.substring(prefix.length());
                long newNumber = Long.parseLong(numberPart) + 1;
                newId = prefix + newNumber;
            }

            String insertQuery = "INSERT INTO stars (id, name, birthYear) VALUES (?, ?, ?)";

            PreparedStatement statement = dbCon.prepareStatement(insertQuery);
            statement.setString(1, newId);
            statement.setString(2, starName);
            if (birthYear.isEmpty()) {
                statement.setNull(3, Types.INTEGER);
            } else {
                statement.setInt(3, Integer.parseInt(birthYear));
            }
            statement.executeUpdate();

            JsonObject responseJsonObject = new JsonObject();
            // responseJsonObject.addProperty("status", "success");
            responseJsonObject.addProperty("message", "Success! Star ID:" + newId);
            response.getWriter().write(responseJsonObject.toString());
            statement.close();

            // Set response status to 200 (OK)
            response.setStatus(200);
            dbCon.close();
        }
        catch (Exception e) {

            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }
    }
}