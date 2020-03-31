import java.util.*;
import java.sql.*;

public class Customer extends Methods
{
	private Connection con; 	 // Connection to database
	private int customerId = -1; // set to customer's id in end of verify method

	/**
	 * Constructor for Customer object
	 * @param c connection to database
	 * @return New Customer object
	 */
	public Customer(Connection c)
	{
		con = c;
	}

	// ------------------------- Customer Main Methods ------------------------

	/**
	 * Registers a new customer in the system
	 */
	public void register()
	{
		try 
		{
			String insert_query = 	"INSERT INTO customer (name, birthdate, address, city, state, zip, DL_number, DL_expiration) " +
									"VALUES (?, TO_DATE(?, 'yyyy-mm-dd'), ?, ?, ?, ?, ?, TO_DATE(?, 'yyyy-mm-dd'))";
			PreparedStatement p = con.prepareStatement(insert_query);
			
			printB("In order to register you in our system, please provide us with the following information!");
			String name = promptString("What is your name? ");
			if (name.equals("q")) {
				printB("You have been returned to the menu.");
				return;
			}
			System.out.println("What is your birthdate? ");
			Map<String, Integer> birthdate = promptDate();
			if (birthdate == null) {
				printB("You have been returned to the menu.");
				return;
			}
			String address = promptString("What is your street address? ");
			if (address.equals("q")) {
				printB("You have been returned to the menu.");
				return;
			}
			String city = promptString("What city are you from? ");
			if (city.equals("q")) {
				printB("You have been returned to the menu.");
				return;
			}
			String state;
			while (1 == 1) {
				state = promptString("What state are you from? (Ex: CA, NY): ");
				if (state.equals("q")) {
					printB("You have been returned to the menu.");
					return;
				}	
				if (state.length() == 2) {
					break;
				}
				System.out.print(eStr("Invalid state!") + bStr(" Please try again! "));
			}
			int zip = promptInt("What is your ZIP code? ", 1, 99999);
			if (zip == -99) {
				printB("You have been returned to the menu.");
				return;
			}
			int dlnum = promptInt("What is your Driver License number? ", 1, 999999999);
			if (dlnum == -99) {
				printB("You have been returned to the menu.");
				return;
			}
			System.out.println("What is your Driver License expiration date? ");
			Map<String, Integer> expiration = promptDate();
			if (expiration == null) {
				printB("You have been returned to the menu.");
				return;
			}
			// Dates as strings
			String sBirthdate = toString(birthdate);  
			String sExpiration = toString(expiration);

			clearTerminal();
			System.out.println("Before being registered, please verify the following information:");
			System.out.println(bStr("       Name: ") + name);
			System.out.println(bStr("  Birthdate: ") + sBirthdate);
			System.out.println(bStr("    Address: ") + address + ", " + city + ", " + state + "  " + zip);
			System.out.println(bStr("    License: ") + dlnum);
			System.out.println(bStr(" Expiration: ") + sExpiration + "\n");

			if (checkYN("Is the information correct?")) {
				clearTerminal();
				p.setString(1, name);
				p.setString(2, sBirthdate);
				p.setString(3, address);
				p.setString(4, city);
				p.setString(5, state);
				p.setInt(6, zip);
				p.setInt(7, dlnum);
				p.setString(8, sExpiration);
				p.executeUpdate();
				printB("Registered " + name + "!");
			}
			else {
				clearTerminal();
				printB("Registration cancelled!");
			}
		}
		catch (SQLException e) {
			printE("Invalid entry into customer!"); 
			//e.printStackTrace();
		}
	} // register method

	/**
	 * verifies a customer
	 * STATUS: Completed & Tested
	 */
	public boolean verify()
	{
		boolean invalidName = true;
		try 
		{
			clearTerminal();
			String result = "";
			while (invalidName)
			{
				System.out.print("Please enter your name (" + bStr("'?'") + " for help): ");
				result = scan.nextLine();
				if (result.equals("q")) {
					clearTerminal();
					printB("You have been returned to the menu.");
					return false;
				}
				else if (result.equals("?")) {
					clearTerminal();
					printB("       Help menu:");
					System.out.println("  Example name: Griffin Reichert");
					System.out.println("  Example DL num: 12345");
					System.out.println("  Or enter " + bStr("'q'") + " to quit\n");
					System.out.print("Please enter your name (" + bStr("'?'") + " for help): ");
					continue;
				}
				else if (result.isEmpty()) {
					clearTerminal();
					System.out.print(eStr("Try again!") + " Please enter your name (" + bStr("'?'") + " for help " + bStr("'q'") + " to quit): ");
					continue;
				}
				PreparedStatement pst = con.prepareStatement("select * from customer where name = ?");
				pst.setString(1, result);
				ResultSet r = pst.executeQuery();
				if (r.next()) {
					int userDL = r.getInt("dl_number");
					int inputDL = verifyDriverLicense(userDL);
					invalidName = false;
					if (inputDL == -3) {
						clearTerminal();
						printE("You have reached 3 incorrect attempts! " + brightText + "\nYou have been returned to the main menu.");
						return false;
					}
					else if (inputDL == -1) {
						clearTerminal();
						printB("You have been returned to the menu.");
						return false;
					}
					if (inputDL == userDL) {	// verified user
						customerId = r.getInt("id"); 	//IMPORTANT: Sets class variable customerId
						clearTerminal();
						System.out.println("Welcome " + bStr(r.getString("name")) + "!\n");
						return true;
					}
				}
				else {
					clearTerminal();
					printE("Could not find user with that name!");
				}
			} // while
		}
		catch(SQLException e) {
			System.out.println("Caused an exception: " + e);
		}
		return false;
	} // verify method

	// ---------------------------- Customer Helper Methods -------------------


	private int verifyDriverLicense(int userDL)
	{
		int tries = 0;
		boolean help = false;
		while (tries < 3) {
			// Print user prompts
			if (tries == 0) {
				System.out.print("To confirm your identity, please enter your Driver License Number(" + bStr("'?'") + " for help): ");
			}
			else if (tries == 1 && !help) {
				System.out.println(eStr("Incorrect Driver License Number!") + " (" + bStr("'?'") + " for help)");
			}
			if (tries > 0) {
				System.out.print(bStr((3 - tries) + " attempts remaining! ") + "Enter your driver license again: ");
			}

			if (help) {
				help = false;
			}
			String s = scan.nextLine();
			// check inputs
			if (s.equals("q")) {
			//if (scan.hasNext("q")) {
				//scan.next();
				return -1;
			}
			//else if (scan.hasNext("\\?")) {
			else if (s.equals("?")) {
				//scan.next();
				clearTerminal();
				printB("\n       Help menu:");
				System.out.println("  Example name: Griffin Reichert");
				System.out.println("  Example DL num: 12345");
				System.out.println("  Or enter " + bStr("'q'") + " to quit\n");
				if (tries == 1) {
					System.out.println(eStr("Incorrect Driver License Number!") + " (" + bStr("'?'") + " for help)");
				}
				help = true;
				continue;
			}
			int input = Integer.parseInt(s);
			// else if (scan.hasNextInt()) {
			// 	int input = scan.nextInt();
				if (input == userDL) {
					return input;
				}
			//}
			// else if (scan.hasNext()) {
			// 	scan.next();
			// }
			// scan.nextLine();
			tries++;
		}
		return -3;
	}

	// ---------------------------- Customer Menu Methods ---------------------

	/**
	 * Prints menu and returns customer type
	 * @return 	0 to return
	 * 			1 for a new customer
	 * 			2 for an existing customer
	 */
	public static int getType()
	{
		ArrayList<String> m = new ArrayList<>();
		m.add("Are you a new customer or a returning customer?");
		m.add("New Customer");
		m.add("Existing Customer");
		return printMenu(m);
	}

	/**
	 * Prompts a verified customer to chose their next action, executes action
	 */
	public void nextAction()
	{
		while (1 == 1)
		{
			int action = printActions();
			if (action == 0 || customerId == -1) { // return
				clearTerminal();
				return;						
			}
			if (action == 1) { // My Account
				int acctAction = -1;
				while (acctAction != 0)
				{
					acctAction = accountActions();
					if (acctAction == 0) {
						clearTerminal();
					}
					else if (acctAction == 1) {
						Group group = new Group(con, customerId);
						group.viewAccountInfo();
					}
					else if (acctAction == 2) { // view charges to account
						Billing billing = new Billing(con, customerId);
						billing.viewCharges();
					}
					else if (acctAction == 3) { // view memberships
						Group group = new Group(con, customerId);
						group.viewMemberships();
					}
					else if (acctAction == 4) { // Join a group
						Group group = new Group(con, customerId);
						group.join();
					}
				}
			}
			else if (action == 2) { // Reservations
				int rAction = -1;
				while (rAction != 0)
				{
					rAction = resActions();
					if (rAction == 0) {
						clearTerminal();
					}
					else if (rAction == 1) { 	// make a reservation
						Reservation reservation = new Reservation(con, customerId);
						reservation.makeNew();
					}
					else if (rAction == 2) {    // view reservations
						Reservation reservation = new Reservation(con, customerId);
						reservation.viewAll();
					}
				}
			}
		} // while
	} // getAction

	/**
	 * Prompts user to select a next action
	 * @return int corresponding to chosen action
	 */
	public int printActions()
	{
		ArrayList<String> m = new ArrayList<>();
		m.add("What would you like to see?");
		m.add("My Account");
		m.add("Reservations");
		return printMenu(m);
	}

	/**
	 * Prompts user to select a next action
	 * @return int corresponding to chosen action
	 */
	public int resActions()
	{
		ArrayList<String> m = new ArrayList<>();
		m.add("What would you like to do?  ");
		m.add("Make a new reservation");
		m.add("View my reservations");
		return printMenu(m);
	}

	/**
	 * Prompts user to select a next action
	 * @return int corresponding to chosen action
	 */
	public int accountActions()
	{
		ArrayList<String> m = new ArrayList<>();
		m.add("What would you like to do?      ");
		m.add("View account information");
		m.add("View charges to my account");
		m.add("View my group memberships");
		m.add("Join a group");
		return printMenu(m);
	}
}