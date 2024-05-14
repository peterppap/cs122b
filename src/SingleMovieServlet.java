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

// Declaring a WebServlet called SingleMovieServlet, which maps to url "/api/single-movie"
@WebServlet(name = "SingleMovieServlet", urlPatterns = "/api/single-movie")
public class SingleMovieServlet extends HttpServlet {
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

        String id = request.getParameter("id");

        request.getServletContext().log("getting id: " + id);

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {

            String query = "SELECT m.id, m.title, m.year, m.director,\n" +
                    "GROUP_CONCAT(DISTINCT g.name ORDER BY g.name ASC SEPARATOR ',') AS genres,\n" +
                    "GROUP_CONCAT(DISTINCT g.id ORDER BY g.name ASC SEPARATOR ',') AS genres_id,\n" +
                    "GROUP_CONCAT(DISTINCT s.name ORDER BY num_movies DESC, s.name ASC SEPARATOR ',') AS stars,\n" +
                    "GROUP_CONCAT(DISTINCT s.id ORDER BY num_movies DESC, s.name ASC SEPARATOR ',') AS stars_id,\n" +
                    "ROUND(AVG(r.rating),2) AS rating\n" +
                    "FROM ratings AS r\n" +
                    "RIGHT JOIN movies AS m ON r.movieId = m.id\n" +
                    "LEFT JOIN stars_in_movies AS sim ON m.id = sim.movieId\n" +
                    "LEFT JOIN stars AS s ON sim.starId = s.id\n" +
                    "LEFT JOIN (\n" +
                    "    SELECT sim.starId, COUNT(DISTINCT sim.movieId) AS num_movies\n" +
                    "    FROM stars_in_movies AS sim\n" +
                    "    GROUP BY sim.starId\n" +
                    ") AS mdb ON s.id = mdb.starId\n" +
                    "LEFT JOIN genres_in_movies AS gim ON m.id = gim.movieId\n" +
                    "LEFT JOIN genres AS g ON gim.genreId = g.id\n" +
                    "WHERE m.id = ?\n" +
                    "GROUP BY m.id, m.title, m.year, m.director;";

            // Declare our statement
            PreparedStatement statement = conn.prepareStatement(query);

            statement.setString(1, id);

            // Perform the query
            ResultSet rs = statement.executeQuery();
            JsonArray jsonArray = new JsonArray();

            // Iterate through each row of rs
            while (rs.next()) {
                System.out.println("in-loop");

                String movieId = rs.getString("id");
                String title = rs.getString("title");
                String year = rs.getString("year");
                String director = rs.getString("director");
                String rating = rs.getString("rating");
                String genres = rs.getString("genres");
                String genres_id = rs.getString("genres_id");
                String starsId = rs.getString("stars_id");
                String stars = rs.getString("stars");
                // Create a JsonObject based on the data we retrieve from rs

                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movieId", movieId);
                jsonObject.addProperty("title", title);
                jsonObject.addProperty("year", year);
                jsonObject.addProperty("director", director);
                jsonObject.addProperty("genres", genres);
                jsonObject.addProperty("genres_id", genres_id);
                jsonObject.addProperty("starsId", starsId);
                jsonObject.addProperty("stars", stars);
                jsonObject.addProperty("rating", rating);
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

    }

}