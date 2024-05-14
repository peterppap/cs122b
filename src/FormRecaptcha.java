import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "FormReCaptcha", urlPatterns = "/form-recaptcha")
public class FormRecaptcha extends HttpServlet {
//    private static final long serialVersionUID = 1L;

    public String getServletInfo() {
        return "Servlet connects to MySQL database and displays result of a SELECT";
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();

        String gRecaptchaResponse = request.getParameter("g-recaptcha-response");
        System.out.println("gRecaptchaResponse=" + gRecaptchaResponse);

        // Verify reCAPTCHA
        try {
            System.out.println("verifying response");
            RecaptchaVerifyUtils.verify(gRecaptchaResponse);

            response.setContentType("text/plain");
            out.print(1);
            out.flush();
            out.close();

        } catch (Exception e) {
            response.setContentType("text/plain");
            out.print(0);
            out.flush();
            out.close();
        }
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doGet(request, response);
    }
}