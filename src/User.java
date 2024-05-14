import java.util.ArrayList;

/**
 * This User class only has the username field in this example.
 * You can add more attributes such as the user's shopping cart items.
 */
public class User {

    private final String username;
    private ArrayList<String>  cartItems;

    private final String firstname;

    private final String lastname;

    private final int userId;

    private String ccid;

    private String address;

    private String email;

    private String password;
    private String purchaseDate;


    public User(String fName, String lName, int id, String email, String address, String ccid, String password) {
        this.firstname = fName;
        this.lastname = lName;
        this.userId = id;
        this.email = email;
        this.username = email;
        this.cartItems = null;
        this.address = address;
        this.ccid = ccid;
        this.password = password;
    }

    public String getfName()
    {
        return this.firstname;
    }

    public String getlName()
    {
        return this.lastname;
    }

    public int getUserId(){ return this.userId; }

    public String getPurchaseDate() {
        this.purchaseDate = String.valueOf(java.time.LocalDate.now());
        return this.purchaseDate;
    }
}
