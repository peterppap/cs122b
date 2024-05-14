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
import java.sql.Statement;


// Declaring a WebServlet called StarsServlet, which maps to url "/api/stars"
@WebServlet(name = "MovieServlet", urlPatterns = "/api/movies")
public class MoviesServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Create a dataSource which registered in web.
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
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {

            // Declare our statement
            // Assume conn is an already created JDBC connection
            String sql = "SELECT m.id AS movieId, m.title, m.year, m.director, " +
                    "SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT g.name ORDER BY g.name ASC SEPARATOR ','), ',', 3) AS genres, " +
                    "SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT s.name ORDER BY mdb.num_movies DESC, s.name ASC SEPARATOR ','), ',', 3) AS stars, " +
                    "SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT s.id ORDER BY mdb.num_movies DESC, s.name ASC SEPARATOR ','), ',', 3) AS stars_id, " +
                    "ROUND(AVG(r.rating), 2) AS rating " +
                    "FROM movies AS m " +
                    "JOIN genres_in_movies AS gim ON m.id = gim.movieId " +
                    "JOIN genres AS g ON g.id = gim.genreId " +
                    "JOIN stars_in_movies AS sim ON sim.movieId = m.id " +
                    "JOIN stars AS s ON s.id = sim.starId " +
                    "LEFT JOIN ratings AS r ON r.movieId = m.id " +
                    "INNER JOIN ( " +
                    "    SELECT sim.starId, COUNT(DISTINCT sim.movieId) AS num_movies " +
                    "    FROM stars_in_movies AS sim " +
                    "    GROUP BY sim.starId) AS mdb ON s.id = mdb.starId " +
                    "JOIN ( " +
                    "    SELECT movieId, AVG(rating) AS avg_rating " +
                    "    FROM ratings " +
                    "    GROUP BY movieId " +
                    "    ORDER BY avg_rating DESC " +
                    "    LIMIT ?) AS top_movies ON m.id = top_movies.movieId " +
                    "GROUP BY m.id, m.title, m.year, m.director " +
                    "ORDER BY rating DESC;";

            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setInt(1, 20); // Dynamic limit for the number of top movies
            System.out.println(statement);
            ResultSet rs = statement.executeQuery();

            JsonArray jsonArray = new JsonArray();

            // Iterate through each row of rs
            while (rs.next()) {
                String movie_id = rs.getString("movieId");
                String movie_title = rs.getString("title");
                String movie_year = rs.getString("year");
                String movie_director = rs.getString("director");
                String genres = rs.getString("genres");
                String stars = rs.getString("stars");
                String rating = rs.getString("rating");
                String starsId = rs.getString("stars_id");
                // Create a JsonObject based on the data we retrieve from rs
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_id", movie_id);
                jsonObject.addProperty("movie_title", movie_title);
                jsonObject.addProperty("movie_year", movie_year);
                jsonObject.addProperty("movie_director", movie_director);
                jsonObject.addProperty("genres", genres);
                jsonObject.addProperty("stars", stars);
                jsonObject.addProperty("starsId", starsId);
                jsonObject.addProperty("rating", rating);

                jsonArray.add(jsonObject);
            }
            rs.close();
            statement.close();

            // Log to localhost log
            request.getServletContext().log("getting " + jsonArray.size() + " results");

            // Write JSON string to output
            out.write(jsonArray.toString());
            // Set response status to 200 (OK)
            response.setStatus(200);

        } catch (Exception e) {

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