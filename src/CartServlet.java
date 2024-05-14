import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.google.gson.JsonArray;
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
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

@WebServlet(name = "CartServlet", urlPatterns = "/api/cart")
public class CartServlet extends HttpServlet{
    private DataSource dataSource;
    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String movieId = request.getParameter("movieId");
        String amount = request.getParameter("amount");
        HttpSession session = request.getSession();

        // Retrieve or initialize the cart
        Map<String, ArrayList<String>> cart = getOrCreateCart(session);

        // Attempt to add or update the movie in the cart
        if (!updateCartWithMovie(cart, movieId, amount)) {
            // If the movie doesn't exist in the database, handle error
            handleError(response, "Movie not found or database error.");
            return;
        }

        // Write the cart as JSON to the response
        writeCartAsJson(response, cart);
    }

    private Map<String, ArrayList<String>> getOrCreateCart(HttpSession session) {
        Map<String, ArrayList<String>> cart =
                (Map<String, ArrayList<String>>) session.getAttribute("previousItems");
        if (cart == null) {
            cart = new HashMap<>();
            session.setAttribute("previousItems", cart);
        }
        return cart;
    }

    private boolean updateCartWithMovie(Map<String, ArrayList<String>> cart,
                                        String movieId, String amountStr) {
        try (Connection conn = dataSource.getConnection()) {
            if (cart.containsKey(movieId)) {
                // Update existing entry
                ArrayList<String> movieDetails = cart.get(movieId);
                int newAmount = Integer.parseInt(movieDetails.get(1)) + Integer.parseInt(amountStr);
                movieDetails.set(1, String.valueOf(Math.max(newAmount, 0))); // Prevent negative quantity
                if (newAmount <= 0) {
                    cart.remove(movieId); // Remove the item if quantity is zero or less
                }
            } else {
                // Add new movie to the cart
                String query = "SELECT id, title, price FROM movies WHERE id = ?";
                try (PreparedStatement statement = conn.prepareStatement(query)) {
                    statement.setString(1, movieId);
                    ResultSet rs = statement.executeQuery();

                    if (!rs.next()) return false; // Movie not found

                    ArrayList<String> movieDetails = new ArrayList<>();
                    movieDetails.add(rs.getString("title")); // title
                    movieDetails.add(amountStr); // amount
                    movieDetails.add(rs.getString("id")); // movieId
                    movieDetails.add(rs.getString("price")); // price
                    cart.put(movieId, movieDetails);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // Database error
        }
        return true;
    }

    private void writeCartAsJson(HttpServletResponse response, Map<String, ArrayList<String>> cart) throws IOException {
        JsonArray cartJsonArray = new JsonArray();
        for (ArrayList<String> movieDetails : cart.values()) {
            JsonObject movieJson = new JsonObject();
            movieJson.addProperty("title", movieDetails.get(0));
            movieJson.addProperty("quantity", movieDetails.get(1));
            movieJson.addProperty("movieId", movieDetails.get(2));
            movieJson.addProperty("price", movieDetails.get(3));
            cartJsonArray.add(movieJson);
        }
        response.setContentType("application/json");
        response.getWriter().write(cartJsonArray.toString());
    }

    private void handleError(HttpServletResponse response, String errorMessage) throws IOException {
        JsonObject errorJson = new JsonObject();
        errorJson.addProperty("status", "fail");
        errorJson.addProperty("message", errorMessage);
        response.setContentType("application/json");
        response.getWriter().write(errorJson.toString());
    }


    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        HashMap<String, ArrayList<String>> previousItems = (HashMap<String, ArrayList<String>>) session.getAttribute("previousItems");
        if (previousItems == null) {
            previousItems = new HashMap<String, ArrayList<String>>();
        }
        // Log to localhost log
        request.getServletContext().log("getting " + previousItems.size() + " items");
        JsonArray previousItemsJsonArray = new JsonArray();
        for (ArrayList<String> tp : previousItems.values()){
            JsonObject responseJsonObject = new JsonObject();
            responseJsonObject.addProperty("title", tp.get(0));
            responseJsonObject.addProperty("quantity", tp.get(1));
            responseJsonObject.addProperty("id", tp.get(2));
            responseJsonObject.addProperty("price", tp.get(3));
            previousItemsJsonArray.add(responseJsonObject);
        }
        response.getWriter().write(previousItemsJsonArray.toString());
    }
}