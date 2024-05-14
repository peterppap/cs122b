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
import java.util.HashMap;

@WebServlet(name = "ConfirmServlet", urlPatterns = "/api/confirmation")
public class ConfirmServlet extends HttpServlet{
    private DataSource dataSource;
    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        User userinfo = (User) session.getAttribute("user");
        int customerId = userinfo.getUserId();
        String purchaseDate = userinfo.getPurchaseDate();
        HashMap<String, ArrayList<String>> previousItems =
                (HashMap<String, ArrayList<String>>) session.getAttribute("previousItems");

        if (previousItems == null || previousItems.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No items to process.");
            return;
        }

        try (Connection conn = dataSource.getConnection()) {
            int nextSaleId = fetchNextSaleId(conn);
            JsonArray salesData = processSales(conn, customerId, purchaseDate, previousItems, nextSaleId);
            writeResponse(response, salesData);
            session.removeAttribute("previousItems"); // Clear session data
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database access error.");
        }
    }

    private int fetchNextSaleId(Connection conn) throws SQLException {
        String query = "SELECT id FROM sales ORDER BY id DESC LIMIT 1";
        try (PreparedStatement stmt = conn.prepareStatement(query); ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getInt("id") + 1 : 1;
        }
    }

    private JsonArray processSales(Connection conn, int customerId, String purchaseDate,
                                   HashMap<String, ArrayList<String>> items, int saleId) throws SQLException {
        JsonArray jsonArray = new JsonArray();
        String insertQuery = "INSERT INTO sales (id, customerId, movieId, saleDate) VALUES (?, ?, ?, ?)";
        for (ArrayList<String> item : items.values()) {
            try (PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
                stmt.setInt(1, saleId);
                stmt.setInt(2, customerId);
                stmt.setString(3, item.get(2)); // Assume movieId is at index 2
                stmt.setString(4, purchaseDate);
                stmt.executeUpdate();
                jsonArray.add(createSaleJsonObject(saleId++, item));
            }
        }
        return jsonArray;
    }

    private JsonObject createSaleJsonObject(int saleId, ArrayList<String> itemDetails) {
        JsonObject saleInfo = new JsonObject();
        saleInfo.addProperty("saleid", saleId);
        saleInfo.addProperty("movie_id", itemDetails.get(2)); // Assume movieId is at index 2
        saleInfo.addProperty("movie_title", itemDetails.get(0)); // Assume title is at index 0
        saleInfo.addProperty("quantity", itemDetails.get(1)); // Assume quantity is at index 1
        saleInfo.addProperty("price", itemDetails.get(3)); // Assume price is at index 3
        int total = Integer.parseInt(itemDetails.get(1)) * Integer.parseInt(itemDetails.get(3));
        saleInfo.addProperty("total", total);
        return saleInfo;
    }

    private void writeResponse(HttpServletResponse response, JsonArray data) throws IOException {
        response.setContentType("application/json");
        response.getWriter().write(data.toString());
        response.setStatus(HttpServletResponse.SC_OK);
    }


}