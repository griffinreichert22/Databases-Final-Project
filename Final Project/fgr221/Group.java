import java.util.*;
import java.sql.*;

/**
 * Group method
 * 	Display groups a member is a part of
 * 	Customers can join groups
 * 	
 */
public class Group extends Methods
{
	private Connection con;
	private int userId;
	private ArrayList<Integer> g; 

	public Group(Connection c, int id) 
	{
		con = c;
		userId = id;
		g = new ArrayList<Integer>();
		populateGroups();
	}

	// ----------------- Group Main Methods -----------------------------------

	public void viewAccountInfo()
	{
		try 
		{
			String q = "SELECT * FROM customer WHERE id=?";
			PreparedStatement p = con.prepareStatement(q);
			p.setInt(1, userId);
			ResultSet r = p.executeQuery();
			if (!r.next()) {
				printE("Error, no customers with that ID!");
				return;
			}
			else {

				String name = r.getString("name");
				java.sql.Date sqlBirthdate = r.getDate("birthdate");
				String birthdate = dateToString(sqlBirthdate);
				String address = r.getString("address");
				String city = r.getString("city");
				String state = r.getString("state");
				int zip = r.getInt("zip");
				int num = r.getInt("dl_number");
				java.sql.Date sqlExpiration = r.getDate("dl_expiration");
				String expiration = dateToString(sqlExpiration);

				System.out.println("Viewing Account Information: \n");
				System.out.println(bStr("       Name: ") + name);
				System.out.println(bStr("  Birthdate: ") + birthdate);
				System.out.println(bStr("    Address: ") + address);
				System.out.println(bStr("       City: ") + city);
				System.out.println(bStr("      State: ") + state);
				System.out.println(bStr("        ZIP: ") + zip);
				System.out.println(bStr("  DL Number: ") + num);
				System.out.println(bStr(" Expiration: ") + expiration);
				zeroReturn();
			}
		}
		catch(SQLException e) {
 			printE("Caused an exception: " + mainText + e);
		}
	}

	/**
	 * Allows a customer to join a group
	 * STATUS:
	 * 		successfully inserts record into member
	 * 		rejects title not in organization, and incorrect verification codes
	 */
	public void join()
	{
		while (1 == 1) {
			
			String title = promptString("Enter the name of the group you wish to join (" + bStr("'list'") + " to see all groups, " + bStr("'q'") + " to quit): ");
			if (title.equals("q")) {
				printB("You have been returned to the menu.");
				return;
			}		
			else if (title.equals("list")) { // user requests a list of organizations
				title = listGroups();
				if (title.equals("")) {
					printB("You have been returned to the menu.");
					return;
				}
			}
			ArrayList<Integer> v = new ArrayList<>();
			try {
				String query = "SELECT verification FROM organization WHERE title=?";
				PreparedStatement p = con.prepareStatement(query);
				p.setString(1, title);
				ResultSet rs = p.executeQuery();
				while (rs.next()) {
					v.add(rs.getInt("verification"));
				}
				if (v.isEmpty()) {
					printE("No groups found with name " + title + "! " + brightText + "Please try again!");
					continue;
				}
				for (int i = 0; i < v.size(); i++) { // checks if user is already a member of this organization
					if (g.contains(v.get(i))) {
						clearTerminal();
						printB("You are already a member of " + title + "!");
						return;
					}
				}
			}
			catch (SQLException e) {
				printE("Caused an error");
			}

			try {
				while (1 == 1) {
					int verification = promptInt("What is the verification code for " + title + "? (" + bStr("'q'") + " to quit): ", 0, 99999);
					if (verification == -99) {
						printB("You have been returned to the menu.");
						return;
					}
					if (v.contains(verification)) {
						String q = "INSERT INTO member (id, verification) VALUES (?,?)";
						PreparedStatement p = con.prepareStatement(q);
						p.setInt(1, userId);
						p.setInt(2, verification);
						if (checkYN("Confirm you would like to join " + bStr(title) + "?")) {	
							p.executeUpdate();
							printB("Successfully created membership!");
						}
						return;
					}
					else {
						System.out.print(errorText + "Incorrect verification code! " + brightText + "Please try again! " + mainText); 
					}
				}
			}
			catch(SQLException e) {
				printE("Error inserting into database: " + e);
			}
		}
	}

	public void viewMemberships()
	{
		try {
			String query = "SELECT * FROM member NATURAL JOIN organization WHERE id=?";
			PreparedStatement p = con.prepareStatement(query);
			p.setInt(1, userId);
			ResultSet rs = p.executeQuery();
			if (!rs.next()) {
				clearTerminal();
				printB("You have no group memberships!" + mainText + " Enter 3 to join a group:");
				return;
			}
			else {
				System.out.println("Viewing all memberships: \n");
				String pFormat = "%-16s%8s\n";
				System.out.print(brightText);
				System.out.printf(pFormat, "Group", "Discount");
				printE("------------------------");
				do 
				{
				    String group = rs.getString("title");
				    String discount = Integer.toString(rs.getInt("discount"));
				    while (discount.length() < 3) {
				    	discount = " " + discount;
				    }
				    discount = "$ " + discount;
					System.out.printf(pFormat, group, discount);
				} while (rs.next());
				printE("------------------------");
				zeroReturn();
			}
		}
		catch (SQLException e) {
			System.out.println("Caused an exception: " + e);
		}
	}

	// -------------------------- Group Helper Methods ------------------------

	/**
	 * 
	 * 
	 */
	private void populateGroups()
	{
		try 
		{
			String query = "SELECT * FROM member WHERE id=?";
			PreparedStatement p = con.prepareStatement(query);
			p.setInt(1, userId);
			ResultSet rs = p.executeQuery();
			while (rs.next())
			{
				g.add(rs.getInt("verification"));
			}
		}
		catch (SQLException e) {
			System.out.println("Caused an exception: " + e);
		}
	}

	private String listGroups()
	{
		try 
		{
			ArrayList<String> t = new ArrayList<String>(); //stores all titles of organizations
			t.add("Please enter the group you wish to join: ");
			String query = "SELECT DISTINCT title FROM organization ORDER BY title";
			PreparedStatement p = con.prepareStatement(query);
			ResultSet rs = p.executeQuery();
			if (!rs.next()) {
				printE("Error, no organizations in database!");
				return "";
			}
			do {
				t.add(rs.getString("title"));
			} while (rs.next());
			int choice = printMenu(t);
			if (choice == 0) {
				return "";
			}
			return t.get(choice);
		}
		catch (SQLException e) {
			System.out.println("Caused an exception: " + e);
			return "";
		}
	}

}
