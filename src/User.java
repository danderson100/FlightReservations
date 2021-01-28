import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Author: David Anderson
 * File: User.java
 *
 * Purpose: This class will store a User's information and add it to a database. It will use an encryption
 * class to hash the password for safe storage. Once a user is added to the database, they will be
 * able to log in every time they start the program.
 */

public class User {


    private static final String DB_NAME = "flightres.db";
    private static final String CONNECTION_STRING = "jdbc:sqlite:" + DB_NAME;


    private final String name;
    private String email;
    private String phone;
    private final String password;

    public User(String name, String password) {
        this.name = name;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public boolean generateLogin() {
        try {
            Connection conn = DriverManager.getConnection(CONNECTION_STRING);
            Statement statement = conn.createStatement();
            String hashedPW = hashPassword(password);
            String query = "INSERT INTO users (username, pass, email, phone) " +
                    "VALUES('" + name + "', '" + hashedPW + "', '" + email + "', '" + phone +"')";
            statement.executeUpdate(query);
            return true;
        } catch (SQLException e) {
            System.out.println("Error: " + e);
            return false;
        }
    }

    public boolean validateLogin(String user, String pass) {
        boolean checkPass = false;

        try {
            Connection conn = DriverManager.getConnection(CONNECTION_STRING);
            Statement statement = conn.createStatement();

            String checkPassQuery = "SELECT pass FROM users WHERE username='" + user + "'";

            ResultSet results = statement.executeQuery(checkPassQuery);
            String hashedPass = results.getString("pass");
            checkPass = checkPassword(pass, hashedPass);

        } catch (SQLException e) {
            System.out.println("Error: " + e);

        }
        return checkPass;
    }

    public List<String> getCredentials(String user) {
        List<String> credentials = new ArrayList<>();
        try {
            Connection conn = DriverManager.getConnection(CONNECTION_STRING);
            Statement statement = conn.createStatement();

            String getCreds = "SELECT * FROM users WHERE username='" + user + "'";

            ResultSet results = statement.executeQuery(getCreds);
            String email = results.getString("email");
            String phone = results.getString("phone");
            credentials.add(email);
            credentials.add(phone);
        } catch (SQLException e) {
            System.out.println("Error " + e);
        }

        return credentials;
    }

    private boolean checkPassword(String pass, String hashedPass) {
        return BCrypt.checkpw(pass, hashedPass);
    }

    private String hashPassword(String password) {
        //hash this first
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phone=" + phone +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return phone.equals(user.phone) && Objects.equals(name, user.name) && Objects.equals(email, user.email)
                && Objects.equals(password, user.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, password);
    }
}
