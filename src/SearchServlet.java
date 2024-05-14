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
import java.sql.SQLException;


/**
 * A servlet that takes input from a html <form> and talks to MySQL moviedb,
 * generates output as a html <table>
 */

// Declaring a WebServlet called SearchServlet, which maps to url "/search"
@WebServlet(name = "SearchServlet", urlPatterns = "/api/search")
public class SearchServlet extends HttpServlet {

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
        try {

            // Create a new connection to database
            Connection dbCon = dataSource.getConnection();

            // Retrieve parameter "name" from the http request, which refers to the value of <input name="name"> in index.html
            String titles = request.getParameter("title");
            String years = request.getParameter("year");
            String directors = request.getParameter("director");
            String stars = request.getParameter("star");

            String sorting = request.getParameter("sorting");
            // String paging = request.getParameter("paging");

            if (sorting != null) {
                String currentRecord = (String) session.getAttribute("currentResult");
                if (currentRecord != null) {
                    currentRecord = currentRecord.substring(40) + " ORDER BY ";
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
                    try {
                        Connection conn = dataSource.getConnection();
                        if (currentRecord.startsWith("t:")) {
                            currentRecord = currentRecord.substring(2).trim();  // Remove the prefix and any leading whitespace
                        }
                         PreparedStatement stmt = conn.prepareStatement(currentRecord);
                         ResultSet rs = stmt.executeQuery();

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
            } else {
                String query;
                if(years.isEmpty()){
                    query = "SELECT m.id, m.title, m.year, m.director," +
                            "    SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT g.name ORDER BY g.name ASC), ',', 3) AS genres,\n" +
                            "    SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT g.id ORDER BY g.name ASC), ',', 3) AS genresId,\n" +
                            "    SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT s.name ORDER BY star_count DESC), ',', 3) AS stars,\n" +
                            "    SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT s.id ORDER BY star_count DESC), ',', 3) AS starsId,\n" +
                            "    r.rating\n" +
                            "FROM movies m\n" +
                            "JOIN genres_in_movies gm ON m.id = gm.movieId\n" +
                            "JOIN genres g ON gm.genreId = g.id\n" +
                            "JOIN ratings r ON m.id = r.movieId\n" +
                            "JOIN stars_in_movies sm ON m.id = sm.movieId\n" +
                            "JOIN stars s ON sm.starId = s.id\n" +
                            "JOIN ( SELECT sm.starId, COUNT(*) AS star_count\n" +
                            "       FROM stars_in_movies sm\n" +
                            "       GROUP BY sm.starId ) sc ON sc.starId = s.id\n" +
                            "WHERE m.title LIKE ? AND m.director LIKE ? AND m.id IN (\n" +
                            "    SELECT DISTINCT m.id\n" +
                            "    FROM movies m\n" +
                            "    JOIN stars_in_movies sm ON m.id = sm.movieId\n" +
                            "    JOIN stars s ON sm.starId = s.id\n" +
                            "    WHERE m.title LIKE ? AND s.name LIKE ?)" +
                            "GROUP BY m.id";
                }else{
                    query = "SELECT m.id, m.title, m.year, m.director,\n" +
                            "SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT g.name ORDER BY g.name ASC), ',', 3) AS genres,\n" +
                            "SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT g.id ORDER BY g.name ASC), ',', 3) AS genresId,\n" +
                            "SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT s.name ORDER BY star_count DESC, s.name ASC), ',', 3) AS stars,\n" +
                            "SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT s.id ORDER BY star_count DESC, s.name ASC), ',', 3) AS starsId,\n" +
                            "r.rating\n" +
                            "FROM movies m\n" +
                            "JOIN genres_in_movies gm ON m.id = gm.movieId\n" +
                            "JOIN genres g ON gm.genreId = g.id\n" +
                            "JOIN ratings r ON m.id = r.movieId\n" +
                            "JOIN stars_in_movies sm ON m.id = sm.movieId\n" +
                            "JOIN stars s ON sm.starId = s.id\n" +
                            "JOIN (SELECT sm.starId, COUNT(*) AS star_count" +
                            "     FROM stars_in_movies sm \n" +
                            "     GROUP BY sm.starId) sc ON sc.starId = s.id\n" +
                            "WHERE m.title LIKE ? AND m.director LIKE ? AND m.year = ? AND m.id IN " +
                            "        (SELECT DISTINCT m.id FROM movies m\n" +
                            "        JOIN stars_in_movies sm ON m.id = sm.movieId\n" +
                            "        JOIN stars s ON sm.starId = s.id\n" +
                            "        WHERE m.title LIKE ? AND s.name LIKE ?)" +
                            "GROUP BY m.id";
                }
                PreparedStatement statement = dbCon.prepareStatement(query);

                statement.setString(1, "%"+titles+"%");
                statement.setString(2, "%"+directors+"%");

                if (!years.equals("")){
                    statement.setString(3, years);
                    statement.setString(4, "%"+titles+"%");
                    statement.setString(5, "%"+stars+"%");
                }else{
                    statement.setString(3, "%"+titles+"%");
                    statement.setString(4, "%"+stars+"%");
                }

                JsonArray jsonArray = new JsonArray();
                // Perform the query
                ResultSet rs = statement.executeQuery();
                // Create a html <table>
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
                session.setAttribute("currentResult", statement.toString());
                rs.close();
                statement.close();

                // Log to localhost log
                request.getServletContext().log("getting " + jsonArray.size() + " results");
                out.write(jsonArray.toString());
            }
            // Write JSON string to output
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