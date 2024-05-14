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
@WebServlet(name = "AddMovie", urlPatterns = "/api/addMovie")
public class AddMovie extends HttpServlet {

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
            String movieName = request.getParameter("movieName");
            String director = request.getParameter("director");
            String year = request.getParameter("year");
            String genre = request.getParameter("genre");
            String star = request.getParameter("star");
            String birthYear = request.getParameter("birthYear");
            System.out.println("Start statement add_movie");

            CallableStatement statement = dbCon.prepareCall("{CALL add_movie(?, ?, ?, ?, ?, ?, ?, ?, ?)}");
            System.out.println("success create function");
            statement.setString(1, movieName);
            statement.setInt(2, Integer.parseInt(year));
            statement.setString(3, director);
            statement.setString(4, star);
            System.out.println("success create function2");
            if (birthYear.isEmpty()){
                statement.setNull(5, Types.INTEGER);
            } else {
                statement.setInt(5, Integer.parseInt(birthYear));
            }
            statement.setString(6, genre);
            statement.registerOutParameter(7, Types.VARCHAR);
            statement.registerOutParameter(8, Types.VARCHAR);
            statement.registerOutParameter(9, Types.INTEGER);
            statement.executeUpdate();
            System.out.println("success update");
            String movieId = statement.getString(7);
            String starId = statement.getString(8);
            String genreId = statement.getString(9);
            boolean success = false;
            if (!movieId.isEmpty() && !starId.isEmpty() && !genreId.isEmpty()) {
                success = true;
            }
            System.out.println("success getId");

            JsonObject responseJsonObject = new JsonObject();
            if (success) {
                responseJsonObject.addProperty("message", "Success! Movie ID:" + movieId + " Star Id:" + starId + " Genre Id:" + genreId);
            } else {
                responseJsonObject.addProperty("message", "Failed to add movie: Duplicated movie");
            }
            response.getWriter().write(responseJsonObject.toString());

            statement.close();

            // Set response status to 200 (OK)
            response.setStatus(200);
            dbCon.close();
        }
        catch (SQLException e) {
            System.err.println("SQLException: " + e.getMessage());
            JsonObject responseJsonObject = new JsonObject();
            responseJsonObject.addProperty("message", "Fail to add movie: The movie already exists.");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);  // 400 for client-side error
            response.getWriter().write(responseJsonObject.toString());
        }
        catch (Exception e) {
            System.err.println("Exception: " + e.getMessage());
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", "Internal server error.");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);  // 500 for server-side error
            response.getWriter().write(jsonObject.toString());
        } finally {
            out.close();
        }
    }
}