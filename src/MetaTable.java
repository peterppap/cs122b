import com.google.gson.Gson;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "MetaTable", urlPatterns = "/api/meta")
public class MetaTable extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }


    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        Map<String, List<String>> metadataMap = new HashMap<>();

        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet tables = metaData.getTables(null, null, "%", new String[] { "TABLE" });

            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                ResultSet columns = metaData.getColumns(null, null, tableName, null);

                List<String> columnData = new ArrayList<>();
                while (columns.next()) {
                    String columnName = columns.getString("COLUMN_NAME");
                    String columnType = columns.getString("TYPE_NAME");
                    int columnSize = columns.getInt("COLUMN_SIZE");
                    columnData.add(columnName + ":" + columnType + "(" + columnSize + ")");
                }
                metadataMap.put(tableName, columnData);
            }

            Gson gson = new Gson();
            String json = gson.toJson(metadataMap);

            out.write(json);
            response.setStatus(200);
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(500);
        }
    }
}