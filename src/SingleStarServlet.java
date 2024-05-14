import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

// Declaring a WebServlet called SingleMovieServlet, which maps to url "/api/single-star"
@WebServlet(name = "SingleStarServlet", urlPatterns = "/api/single-star")
public class SingleStarServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type

        // Retrieve parameter id from url request.
        String id = request.getParameter("id");

        // The log message can be found in localhost log
        request.getServletContext().log("getting id: " + id);

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            // Get a connection from dataSource

            String query = "SELECT s.id, \n" +
                    "       s.name, \n" +
                    "       s.birthYear, \n" +
                    "       m.title, \n" +
                    "       m.year, \n" +
                    "       m.director, \n" +
                    "       m.id AS movieId\n" +
                    "FROM stars s\n" +
                    "JOIN stars_in_movies sim ON s.id = sim.starId\n" +
                    "JOIN movies m ON sim.movieId = m.id\n" +
                    "WHERE s.id = ?\n" +
                    "ORDER BY m.year DESC, m.title ASC\n";

            // Declare our statement
            PreparedStatement statement = conn.prepareStatement(query);

            statement.setString(1, id);

            // Perform the query
            ResultSet rs = statement.executeQuery();

            JsonArray jsonArray = new JsonArray();

            // Iterate through each row of rs
            while (rs.next()) {

                String starId = rs.getString("id");
                String starName = rs.getString("name");
                String birthYear = rs.getString("birthYear");
                if (birthYear == null) {
                    birthYear = "N/A";
                }
                String title = rs.getString("title");
                String movie_year = rs.getString("year");
                String movie_director = rs.getString("director");
                String movieId = rs.getString("movieId");


                // Create a JsonObject based on the data we retrieve from rs

                JsonObject jsonObject = new JsonObject();

                jsonObject.addProperty("title", title);
                jsonObject.addProperty("birthYear", birthYear);
                jsonObject.addProperty("starId", starId);
                jsonObject.addProperty("starName", starName);
                jsonObject.addProperty("movieId", movieId);
                jsonObject.addProperty("movie_year", movie_year);
                jsonObject.addProperty("movie_director", movie_director);

                jsonArray.add(jsonObject);
            }
            rs.close();
            statement.close();

            // Write JSON string to output
            out.write(jsonArray.toString());
            // Set response status to 200 (OK)
            response.setStatus(200);

        } catch (Exception e) {
            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Log error to localhost log
            request.getServletContext().log("Error:", e);
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }

        // Always remember to close db connection after usage. Here it's done by try-with-resources

    }

}