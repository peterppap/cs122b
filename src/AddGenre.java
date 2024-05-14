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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;


/**
 * A servlet that takes input from a html <form> and talks to MySQL moviedb,
 * generates output as a html <table>
 */

// Declaring a WebServlet called SearchServlet, which maps to url "/search"
@WebServlet(name = "AddGenre", urlPatterns = "/api/addGenre")
public class AddGenre extends HttpServlet {

    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
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
            String titles = request.getParameter("title");
            String years = request.getParameter("year");
            String directors = request.getParameter("director");
            String stars = request.getParameter("star");

            // Generate a SQL query
            String query = "";

            PreparedStatement statement = dbCon.prepareStatement(query);

            statement.setString(1, "%"+titles+"%");
            statement.setString(2, "%"+directors+"%");

            JsonArray jsonArray = new JsonArray();
            // Perform the query
            ResultSet rs = statement.executeQuery();
            // Create a html <table>
            while (rs.next()) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_id", rs.getString("id"));

                jsonArray.add(jsonObject);
            }
            session.setAttribute("currentResult", statement.toString());
            rs.close();
            statement.close();

            // Log to localhost log
            request.getServletContext().log("getting " + jsonArray.size() + " results");

            // Write JSON string to output
            out.write(jsonArray.toString());
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