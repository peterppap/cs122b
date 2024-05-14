import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "ResultServlet", urlPatterns = "/api/result")
public class ResultServlet extends HttpServlet {
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json"); // Response mime type
        HttpSession session = request.getSession();

        // Retrieve parameter id from url request.
        String genre = request.getParameter("genre");
        String prefix = request.getParameter("prefix");
        String sorting = request.getParameter("sorting");

        String paging = request.getParameter("paging");
        String offset = (String) session.getAttribute("offset");
        if (offset == null){
            offset = "0";
        }
        // Output stream to STDOUT
        PrintWriter out = response.getWriter();
        if (sorting != null) {
            String currentRecord = (String) session.getAttribute("currentResult");
            if (currentRecord != null) {
                currentRecord = currentRecord.substring(42) + " ORDER BY ";
                switch (sorting) {
                    case "1":
                        currentRecord += "m.title DESC, r.rating DESC";
                        break;
                    case "2":
                        currentRecord += "m.title DESC, r.rating ASC";
                        break;
                    case "3":
                        currentRecord += "m.title ASC, r.rating DESC";
                        break;
                    case "4":
                        currentRecord += "m.title ASC, r.rating ASC";
                        break;
                    case "5":
                        currentRecord += "r.rating DESC, m.title DESC";
                        break;
                    case "6":
                        currentRecord += "r.rating DESC, m.title ASC";
                        break;
                    case "7":
                        currentRecord += "r.rating ASC, m.title DESC";
                        break;
                    case "8":
                        currentRecord += "r.rating ASC, m.title ASC";
                        break;
                }
                currentRecord += ";";

                try (Connection conn = dataSource.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(currentRecord);
                     ResultSet rs = stmt.executeQuery()) {

                    JsonArray jsonArray = new JsonArray();
                    while (rs.next()) {
                        JsonObject jsonObject = new JsonObject();
                        jsonObject.addProperty("movie_id", rs.getString("id"));
                        jsonObject.addProperty("movie_title", rs.getString("title"));
                        jsonObject.addProperty("movie_year", rs.getString("year"));
                        jsonObject.addProperty("movie_director", rs.getString("director"));
                        jsonObject.addProperty("genres", rs.getString("genres"));
                        jsonObject.addProperty("genres_id", rs.getString("genresId"));
                        jsonObject.addProperty("stars", rs.getString("stars"));
                        jsonObject.addProperty("starsId", rs.getString("starsId"));
                        jsonObject.addProperty("rating", rs.getDouble("rating"));
                        jsonArray.add(jsonObject);
                    }
                    out.write(jsonArray.toString());
                    response.setStatus(HttpServletResponse.SC_OK);
                } catch (SQLException e) {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "SQL error occurred.");
                    e.printStackTrace();
                } finally {
                    out.close();
                }
            }
        }
        else{
            try (Connection conn = dataSource.getConnection()) {
                PreparedStatement statement;
                String query = "SELECT m.id, m.title, m.year, m.director, "
                        + "GROUP_CONCAT(DISTINCT g.id ORDER BY g.name ASC) AS genresId, "
                        + "GROUP_CONCAT(DISTINCT g.name ORDER BY g.name ASC) AS genres, "
                        + "GROUP_CONCAT(DISTINCT s.name ORDER BY star_count DESC, s.name ASC) AS stars, "
                        + "GROUP_CONCAT(DISTINCT s.id ORDER BY star_count DESC, s.name ASC) AS starsId, "
                        + "r.rating "
                        + "FROM movies m "
                        + "JOIN genres_in_movies gm ON m.id = gm.movieId "
                        + "JOIN genres g ON gm.genreId = g.id "
                        + "JOIN ratings r ON m.id = r.movieId "
                        + "JOIN stars_in_movies sm ON m.id = sm.movieId "
                        + "JOIN stars s ON sm.starId = s.id "
                        + "JOIN (SELECT sm.starId, COUNT(*) AS star_count FROM stars_in_movies sm GROUP BY sm.starId) sc ON sc.starId = s.id ";
                if (genre != null) {
                    query += "WHERE m.id IN (SELECT m.id FROM movies m JOIN genres_in_movies gm ON m.id = gm.movieId WHERE gm.genreId = ?) ";
                } else if (prefix.equals("*")) {
                    query += "WHERE m.title REGEXP ? ";
                    prefix = "^[^a-zA-Z0-9]+"; // Regex for non-alphanumeric starting characters
                } else {
                    query += "WHERE m.title LIKE ? ";
                    prefix += "%"; // Wildcard for SQL LIKE
                }
                query += "GROUP BY m.id";

                statement = conn.prepareStatement(query);
                if (genre != null || prefix != null) {
                    statement.setString(1, genre != null ? genre : prefix);
                }

                session.setAttribute("currentResult", statement.toString());

                // Perform the query
                ResultSet rs = statement.executeQuery();
                JsonArray jsonArray = new JsonArray();
                while (rs.next()) {
                    String movie_id = rs.getString("id");
                    String movie_title = rs.getString("title");
                    String movie_year = rs.getString("year");
                    String movie_director = rs.getString("director");
                    String genres = rs.getString("genres");
                    String genres_id = rs.getString("genresId");
                    String star_s = rs.getString("stars");
                    String star_id = rs.getString("starsId");
                    String rating = rs.getString("rating");

                    // Create a JsonObject based on the data we retrieve from rs
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("movie_id", movie_id);
                    jsonObject.addProperty("movie_title", movie_title);
                    jsonObject.addProperty("movie_year", movie_year);
                    jsonObject.addProperty("movie_director", movie_director);
                    jsonObject.addProperty("genres", genres);
                    jsonObject.addProperty("genres_id", genres_id);
                    jsonObject.addProperty("stars", star_s);
                    jsonObject.addProperty("starsId", star_id);
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
            } catch (SQLException e) {
                throw new RuntimeException(e);
            } finally {
                out.close();
            }
        }

    }
}

