import java.sql.*;
import java.util.*;
import java.io.*;

public class App {
    static boolean loggedIn = false;

    public static void main(String[] args) throws Exception {

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/sonoo", "techbrain", "admin");
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("select * from admins");

            Scanner sc = new Scanner(System.in);
            System.out.println("Please use the following account to log in...");
            printResultSet(rs);
            user_interface(sc, con);
            con.close();

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static void user_interface(Scanner sc, Connection con) {
        while (true) {
            if (!loggedIn) {
                System.out.println("Please log in to continue: ...");
                logIn(sc, con);
                continue;
            } else {
                System.out.println("\nWhat do you want to do?");
                System.out.println("    1. Add customer");
                System.out.println("    2. Edit customer information");
                System.out.println("    3. Delete customer");
                System.out.println("    4. Upload a file contains customers");
                System.out.println("    5. Generate customer profile by ID");
                System.out.println("    6. List existing customers");
                System.out.println("    7. LogOut and Exit");

                System.out.print("Your selection: ");
            }
            int input;
            try {
                input = Integer.parseInt(sc.nextLine());
                System.out.println();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                continue;
            }

            switch (input) {
                case 1:
                    addCustomer(sc, con);
                    break;
                case 2:
                    editCustomer(sc, con);
                    break;
                case 3:
                    deleteCustomer(sc, con);
                    break;
                case 4:
                    uploadBulkCustomer(sc, con);
                    break;
                case 5:
                    generateProfile(sc, con);
                    break;
                case 6:
                    listCustomer(sc, con);
                    break;
                case 7:
                    System.out.println("See you next time!");
                    return;
            }
        }
    }

    public static void listCustomer(Scanner sc, Connection con) {
        System.out.println("How do you want to filter the customer (empty to skip):");
        System.out.print("  username: ");
        String username = sc.nextLine();
        System.out.print("  firstname: ");
        String firstname = sc.nextLine();
        System.out.print("  lastname: ");
        String lastname = sc.nextLine();
        int age_low = 0, age_high = 130;
        while (true) {
            age_low = 0;
            System.out.print("  age greater or equal to: ");
            try {
                String input = sc.nextLine();
                if (input.length() > 0) {
                    age_low = Integer.parseInt(input);
                }
                break;
            } catch (Exception e) {
                System.out.println("Press enter to skip");
            }
        }
        while (true) {
            age_high = 130;
            System.out.print("  age less or equal to: ");
            try {
                String input = sc.nextLine();
                if (input.length() > 0) {
                    age_high = Integer.parseInt(input);
                }
                break;
            } catch (Exception e) {
                System.out.println("Press enter to skip");
            }
        }
        System.out.print("  email: ");
        String email = sc.nextLine();

        try {
            String sql = "select * from customers where (age >= ? and age <= ? or age is null)";
            if (username.length() > 0) {
                sql += " and username = ?";
            }
            if (firstname.length() > 0) {
                sql += " and firstname = ?";
            }
            if (lastname.length() > 0) {
                sql += " and lastname = ?";
            }
            if (email.length() > 0) {
                sql += " and email = ?";
            }
            PreparedStatement ps = con.prepareStatement(sql);
            // ps.setInt(1, age_low);
            // ps.setInt(2, age_high);
            // ps.setString(3, username.length() > 0 ? username : "username");
            // ps.setString(4, firstname.length() > 0 ? firstname : "firstname");
            // ps.setString(5, lastname.length() > 0 ? lastname : "lastname");
            // ps.setString(6, email.length() > 0 ? email : "email");
            ps.setInt(1, age_low);
            ps.setInt(2, age_high);
            int i = 3;
            if (username.length() > 0) {
                ps.setString(i++, username);
            }
            if (firstname.length() > 0) {
                ps.setString(i++, firstname);
            }
            if (lastname.length() > 0) {
                ps.setString(i++, lastname);
            }
            if (email.length() > 0) {
                ps.setString(i++, email);
            }
            ResultSet rs = ps.executeQuery();
            printResultSet(rs);
        } catch (SQLException ex) {
            while (ex != null) {
                System.out.println("Message = " + ex.getMessage());
                System.out.println("SQLState = " + ex.getSQLState());
                System.out.println("Error code = " + ex.getErrorCode());
                ex = ex.getNextException();
            }
        }

    }

    private static void printResultSet(ResultSet rs) throws SQLException {

        // Prepare metadata object and get the number of columns.
        ResultSetMetaData rsmd = rs.getMetaData();
        int columnsNumber = rsmd.getColumnCount();
        System.out.println();
        // Print column names (a header).
        for (int i = 1; i <= columnsNumber; i++) {

            if (i > 1)
                System.out.print(" | ");
            System.out.printf("%-10s", rsmd.getColumnName(i));
        }
        System.out.println("");

        for (int i = 0; i < 13 * columnsNumber; i++) {
            System.out.print("-");
        }
        System.out.println("");

        while (rs.next()) {
            for (int i = 1; i <= columnsNumber; i++) {
                if (i > 1)
                    System.out.print(" | ");
                System.out.printf("%-10s", rs.getString(i));
            }
            System.out.println("");
        }
        System.out.println();
    }

    public static void addCustomer(Scanner sc, Connection con) {
        String username = "";
        while (username.length() == 0) {
            System.out.print("    Please enter username (Cannot be empty): ");
            username = sc.nextLine();
        }
        System.out.print("    First name: ");
        String first = sc.nextLine();
        String last = "";
        while (last.length() == 0) {
            System.out.print("    Last name (cannot be empty): ");
            last = sc.nextLine();
        }
        int age;
        while (true) {
            try {
                System.out.print("    age: ");
                String input = sc.nextLine();
                if (input.length() > 0)
                    age = Integer.parseInt(sc.nextLine());
                else
                    age = Types.NULL;
                break;
            } catch (Exception e) {
                System.out.println("    Please enter an integer...");
            }
        }
        System.out.print("    email: ");
        String email = sc.nextLine();

        sql_add_customer(username, first, last, age, email, con);
    }

    private static void sql_add_customer(String username, String first, String last, int age, String email,
            Connection con) {
        try {
            String sql = "INSERT INTO CUSTOMERS (username, firstname, lastname, age, email) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, first);
            ps.setString(3, last);
            ps.setInt(4, age);
            ps.setString(5, email);

            ps.executeUpdate();
            System.out.println("Successfully add customer " + last);
            return;
        } catch (SQLException ex) {
            while (ex != null) {
                System.out.println("Message = " + ex.getMessage());
                System.out.println("SQLState = " + ex.getSQLState());
                System.out.println("Error code = " + ex.getErrorCode());
                ex = ex.getNextException();
            }
        }
    }

    public static void editCustomer(Scanner sc, Connection con) {
        System.out.print("    Please enter customer username (q for quit): ");
        String username = sc.nextLine();

        try {
            String sql = "select * from CUSTOMERS where username=?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                updateInfo(sc, con, rs);
            } else {
                System.out.println("Cannot find the customer...");
                return;
            }
        } catch (SQLException ex) {
            while (ex != null) {
                System.out.println("Message = " + ex.getMessage());
                System.out.println("SQLState = " + ex.getSQLState());
                System.out.println("Error code = " + ex.getErrorCode());
                ex = ex.getNextException();
            }
        }
    }

    private static void updateInfo(Scanner sc, Connection con, ResultSet rs) {
        String user, firstname, lastname, email, sql;
        int age;
        try {
            user = rs.getString("username");
            firstname = rs.getString("firstname");
            lastname = rs.getString("lastname");
            age = rs.getInt("age");
            email = rs.getString("email");
        } catch (SQLException ex) {
            System.out.println("Server error...");
            return;
        }
        while (true) {
            System.out.println("Please select the information you want to edit: ");
            System.out.println("    1. first name");
            System.out.println("    2. last name");
            System.out.println("    3. age");
            System.out.println("    4. email address");
            System.out.println("    5. finish editing");
            System.out.print("Your selection: ");
            int input;
            try {
                input = Integer.parseInt(sc.nextLine());
                System.out.println();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                continue;
            }
            try {
                sql = "update customers set firstname = ? where username = " + user;
                PreparedStatement ps = con.prepareStatement(sql);
                switch (input) {
                    case 1:
                        System.out.print("Please enter your new first name: ");
                        firstname = sc.nextLine();
                        sql = "update customers set firstname = ? where username = ?";
                        ps = con.prepareStatement(sql);
                        ps.setString(1, firstname);
                        ps.setString(2, user);
                        break;
                    case 2:
                        System.out.print("Please enter your new last name: ");
                        lastname = sc.nextLine();
                        sql = "update customers set lastname = ? where username = ?";
                        ps = con.prepareStatement(sql);
                        ps.setString(1, lastname);
                        ps.setString(2, user);
                        break;
                    case 3:
                        System.out.println("Please enter the age: ");
                        try {
                            age = Integer.parseInt(sc.nextLine());
                        } catch (Exception e) {
                            System.out.println("Unvalid input");
                            continue;
                        }
                        sql = "update customers set age = ? where username = ?";
                        ps = con.prepareStatement(sql);
                        ps.setInt(1, age);
                        ps.setString(2, user);
                        break;
                    case 4:
                        System.out.print("Please enter your new email: ");
                        email = sc.nextLine();
                        sql = "update customers set email = ? where username = ?";
                        ps = con.prepareStatement(sql);
                        ps.setString(1, email);
                        ps.setString(2, user);
                        break;
                    case 5:
                        return;
                }
                ps.executeUpdate();
                System.out.println("Updated!\n");
            } catch (SQLException ex) {
                while (ex != null) {
                    System.out.println("Message = " + ex.getMessage());
                    System.out.println("SQLState = " + ex.getSQLState());
                    System.out.println("Error code = " + ex.getErrorCode());
                    ex = ex.getNextException();
                }
                System.out.println("error...");
            }

        }

    }

    public static void deleteCustomer(Scanner sc, Connection con) {
        System.out.print("    Please enter customer username (q for quit): ");
        String username = sc.nextLine();
        if (username.toLowerCase().equals("q"))
            return;

        try {
            String sql = "select * from CUSTOMERS where username=?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                sql = "delete from customers where username = ?";
                ps = con.prepareStatement(sql);
                ps.setString(1, username);
                ps.executeUpdate();
                System.out.println("Deleted...\n");
            } else {
                System.out.println("Cannot find the customer...");
                return;
            }
        } catch (SQLException ex) {
            while (ex != null) {
                System.out.println("Message = " + ex.getMessage());
                System.out.println("SQLState = " + ex.getSQLState());
                System.out.println("Error code = " + ex.getErrorCode());
                ex = ex.getNextException();
            }
        }
    }

    public static void uploadBulkCustomer(Scanner sc, Connection con) {
        System.out.println("Please enter the path to the uploaded file:");
        String path = sc.nextLine();
        try {
            Scanner file_scanner = new Scanner(new File(path));

            file_scanner.useDelimiter("\n");
            String[] title = file_scanner.next().split(",");
            while (file_scanner.hasNext()) {
                String[] new_info = file_scanner.next().split(",");
                int age;
                try {
                    age = Integer.parseInt(new_info[3]);
                } catch (Exception e) {
                    System.out.println("Info error for customer: " + new_info[0]);
                    System.out.println("    not added, continue...");
                    continue;
                }
                sql_add_customer(new_info[0], new_info[1], new_info[2], age, new_info[4], con);
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found...\n");
        }

    }

    public static void generateProfile(Scanner sc, Connection con) {
        System.out.print("    Please enter customer username (q for quit): ");
        String username = sc.nextLine();

        try {
            String sql = "select * from CUSTOMERS where username=?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String filename = generateCSV(rs);
                if (filename.length() > 0) {
                    System.out.println("file generated: " + filename + "\n");
                }

            } else {
                System.out.println("Cannot find the customer...");
                return;
            }
        } catch (SQLException ex) {
            while (ex != null) {
                System.out.println("Message = " + ex.getMessage());
                System.out.println("SQLState = " + ex.getSQLState());
                System.out.println("Error code = " + ex.getErrorCode());
                ex = ex.getNextException();
            }
        }
    }

    private static String generateCSV(ResultSet rs) {
        try {
            String filename = "user_" + rs.getString("username") + ".csv";
            FileWriter csvWriter = new FileWriter(filename);
            csvWriter.append("Username,firstname,lastname,age,email\n");
            csvWriter.append(rs.getString("username") + ",");
            csvWriter.append(rs.getString("firstname") + ",");
            csvWriter.append(rs.getString("lastname") + ",");
            csvWriter.append(rs.getInt("age") + ",");
            csvWriter.append(rs.getString("email"));
            csvWriter.append("\n");

            csvWriter.flush();
            csvWriter.close();

            return filename;
        } catch (SQLException e) {
            System.out.println(e);
        } catch (Exception e) {
            System.out.println(e);
        }
        return "";
    }

    public static void logIn(Scanner sc, Connection con) {
        System.out.println("Welcome, please login to continue!");
        System.out.print("    Please enter user name (q for quit): ");
        String username = sc.nextLine();
        System.out.print("    Please enter password: ");
        String pwd = sc.nextLine();
        try {
            String sql = "select * from admins where username=? and pwd=?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, pwd);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                loggedIn = true;
                System.out.println("Welcome " + username + "!\n");
            } else {
                System.out.println("Username and password does not match...\n");
            }
        } catch (SQLException ex) {
            while (ex != null) {
                System.out.println("Message = " + ex.getMessage());
                System.out.println("SQLState = " + ex.getSQLState());
                System.out.println("Error code = " + ex.getErrorCode());
                ex = ex.getNextException();
            }

        }
    }

    public static void create_admin_account(Scanner sc, Connection con) {
        System.out.println("Welcome! For registering an administration account!");
        while (true) {
            System.out.print("    Please enter user name (q for quit): ");
            String username = sc.nextLine();
            if (username.toUpperCase().equals("Q"))
                return;
            System.out.print("    Please enter password: ");
            String pass1 = sc.nextLine();
            System.out.print("    Please enter again: ");
            String pass2 = sc.nextLine();

            if (!pass1.equals(pass2)) {
                System.out.println("Password does not matches...\n");
                continue;
            }

            try {
                String sql = "INSERT INTO ADMINS (user_name, user_pass) VALUES (?, ?)";
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setString(1, username);
                ps.setString(2, pass1);
                System.out.println(ps);
                ps.executeUpdate();
                System.out.println("Successfully Create user " + username);
                return;
            } catch (Exception ex) {
                System.out.println(ex);
                System.out.println("Please try another time with different username!\n");
            }
        }

    }
}
