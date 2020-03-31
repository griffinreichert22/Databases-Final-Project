import java.util.*;
import java.sql.*;

public class Manager extends Methods
{
	private Connection con;

	/**
	 * Constructor for a Manager
	 * @param c Connection
	 * @return Manager Object
	 */
	public Manager(Connection c) {
		con = c;
	}

	//--------- MAIN MANAGER METHODS ------------------------------------------

	/**
	 * Prints inventory in a given location
	 * 	Allows the user to choose the order of the vehicles
	 */
	public void viewInventory()
	{		
		while (1 == 1) {
			int depotId = getLocation();
			if (depotId == -1) {
				return;
			}
			String depotName = getDepotName(depotId);
			if (depotName == "") {
				printB("You have been returned to the menu.");
				return;	
			}
			int o = printOrderOptions();
			if (o == 0) {
				printB("You have been returned to the menu.");
				return;
			}
			try	{
				String query = "SELECT * FROM vehicle WHERE location=?";
				if (o == 1) {
					query += " ORDER BY vin";
				}
				else if (o == 2) {
					query += " ORDER BY type";
				}
				else if (o == 3) {
					query += " ORDER BY make";
				}
				else if (o == 4) {
					query += " ORDER BY odometer";
				}
				PreparedStatement p = con.prepareStatement(query);
				p.setInt(1, depotId);
				ResultSet rs = p.executeQuery();
				if (!rs.next()) {
					printE("No vehicles in selected depot!");
					continue;
				}
				else
				{
					System.out.println("Viewing all vehicles in the " + depotName + ":\n");
					String pFormat = "%6s%9s%-16s%-20s%10s\n";
					System.out.print(brightText);
					System.out.printf(pFormat, "VIN", "Type", "  Make", "Model", "Odometer");
					printE("----------------------------------------------------------------");
					do {
					    String vin = Integer.toString(rs.getInt("vin"));
					    String type = rs.getString("type");
					    String make = "  " + rs.getString("make");
					    String model = rs.getString("model");
					    String odometer = Integer.toString(rs.getInt("odometer"));
						System.out.printf(pFormat, vin, type, make, model, odometer);
					} while (rs.next());
					printE("----------------------------------------------------------------");
					zeroReturn();
					return;
				}
			}
			catch (SQLException e) {
				printE("Caused an exception: " + mainText + e);
			}
		}
	}

	/**
	 * Method for a manager to return a vehicle
	 * 	Returns if a vehicle has already been returned
	 * 	Updates the mileage in the rents table
	 * 	Updates the vehicles location based on the depot it was dropped off at
	 *  Charges customer if they did not refill the fuel tank
	 */
	public void returnVehicle()
	{
		int id = getCustomerId();
		if (id == -1) {
			printB("You have been returned to the menu.");
			return;
		}
		int vin = getVin(id);
		if (vin == -1) {
			printB("You have been returned to the menu.");
			return;
		}
		try { 
			String q = "SELECT * FROM rents WHERE mileage IS NULL AND id=? AND vin=?";
			PreparedStatement p = con.prepareStatement(q);
			p.setInt(1, id);
			p.setInt(2, vin);
			ResultSet rs = p.executeQuery();
			if(!rs.next()) {
				printB("Vehicle has already been returned!");
				printB("You have been returned to the menu.");
				return;
			}
		}
		catch (SQLException e) {
 			printE("Caused an exception: " + mainText + e);
		}

		int odometer = getOdometer(vin);
		if (odometer == -1) {
			printB("You have been returned to the menu.");
			return;
		}
		int newOdom = promptInt("Previous odometer reading: " + odometer + "\nPlease enter the current odometer reading on the vehicle: ", odometer + 1, odometer + 10001);
		if (newOdom == -99) {
			printB("You have been returned to the menu.");
			return;
		}
		int mileage = newOdom - odometer;

		int dropoff = getDropoffDepot(id, vin);
		if (dropoff == -1) {
			printB("You have been returned to the menu.");
			return;
		}

		boolean fullTank = checkYN("Does the vehicle have a full tank of gas?");

		try {
			String q1 = "UPDATE rents SET mileage=? WHERE id=? AND vin=?";
			PreparedStatement p1 = con.prepareStatement(q1);
			p1.setInt(1, mileage);
			p1.setInt(2, id);
			p1.setInt(3, vin);

			String q2 = "UPDATE vehicle SET location=? WHERE vin=?";
			PreparedStatement p2 = con.prepareStatement(q2);
			p2.setInt(1, dropoff);
			p2.setInt(2, vin);

			String q3 = "INSERT INTO charges VALUES (?, ?, 'gas')";
			PreparedStatement p3 = con.prepareStatement(q3);
			p3.setInt(1, id);
			p3.setInt(2, vin);

			printB("Please verify the following information:");
			System.out.println("  ID: " + id);
			System.out.println("  VIN: " + vin);
			System.out.println("  Odometer: " + newOdom);
			String gas = "no";
			if (fullTank) {
				gas = "yes";
			}
			System.out.println("  Full Tank: " + gas);
			if (checkYN("Confirm vehicle return?")) {
				p1.executeUpdate();
				p2.executeUpdate();
				if (!fullTank) {
					p3.executeUpdate();
				}
				printB("Successfully returned vehicle!");
			}
			else {
				printB("Vehicle return cancelled.");
			}
		}
		catch (SQLException e) {
 			printE("Caused an exception: " + mainText + e);
		}
	}

	/**
	 * Adds a new vehicle to the vehicles table
	 * Prompts the user for the necessary info
	 * Verifies information before insertion
	 */
	public void addVehicle()
	{
		printB("To add a new vehicle, please provide the necessary information.");
		int location = getLocation();
		if (location == -1) {
			printB("You have been returned to the menu.");
			return;
		}
		String make = promptString("What is the make of the new vehicle? ");
		if (make.equals("q")) {
			printB("You have been returned to the menu.");
			return;
		}
		String model = promptString("What is the model of the new vehicle? ");
		if (model.equals("q")) {
			printB("You have been returned to the menu.");
			return;
		}

		ArrayList<String> t = new ArrayList<String>();
		t.add("Please select the type of the vehicle:");
		t.add("Compact");
		t.add("Midsize");
		t.add("Premium");
		t.add("SUV");
		t.add("Truck");
		int index = printMenu(t);
		if (index == 0) {
			printB("You have been returned to the menu.");
			return;
		}
		String type = t.get(index);

		int odometer = promptInt("What is the vehicle odometer value? ", 0, 400000);
		if (odometer == -99) {
			printB("You have been returned to the menu.");
			return;
		}

		printB("Please verify the following information:");
		System.out.println(bStr("    Depot: ") + getDepotName(location));
		System.out.println(bStr("     Make: ") + make);
		System.out.println(bStr("    Model: ") + model);
		System.out.println(bStr("     Type: ") + type);
		System.out.println(bStr(" Odometer: ") + odometer + "\n");
		if (checkYN("Confirm creation of new vehicle?")) {
			try {
				String q = "INSERT INTO vehicle VALUES (null, ?, ?, ?, ?, ?)";
				PreparedStatement p = con.prepareStatement(q);
				p.setInt(1, odometer);
				p.setInt(2, location);
				p.setString(3, make);
				p.setString(4, model);
				p.setString(5, type);
				p.executeUpdate();
				printB("Successfully added " + make + " " + model + " to " + getDepotName(location) + "!");
			}
			catch (SQLException e) {
	 			printE("Caused an exception: " + mainText + e);
			}
		}
		else {
			printB("Vehicle creation cancelled!");
		}
	}

	/**
	 * Prints a list of all groups, their verification codes, and their discount
	 */
	public void viewAllGroups()
	{
		try {
			String q = "SELECT * FROM organization ORDER BY title";
			PreparedStatement p = con.prepareStatement(q);
			ResultSet r = p.executeQuery();
			if (!r.next()) {
				printE("Error, no organizations in organization table!");
				printB("Hurts Staff will fix this problem immediately!");
				return;
			}
			else {
				System.out.println("Viewing all groups:\n");
				String pFormat = "%-16s%12s%12s\n";
				System.out.print(brightText);
				System.out.printf(pFormat, "Title", "Verification", "Discount");
				printE("----------------------------------------");
				do {
					String t = r.getString("title");
					String v = Integer.toString(r.getInt("verification"));
					String d = Integer.toString(r.getInt("discount"));
					while (d.length() < 3) {
						d = " " + d;
					}
					d = "$ " + d;
					System.out.printf(pFormat, t, v, d);
				} while(r.next());
				printE("----------------------------------------");
				zeroReturn();
			}
		}
		catch(SQLException e) {
 			printE("Caused an exception: " + mainText + e);
		}
	}

	/**
	 * Adds a new group to the organization table
	 */
	public void addGroup()
	{
		String title = promptString("What is the title of the group you would like to create? ");
		if (title.equals("q")) {
			printB("You have been returned to the menu.");
			return;
		}
		int discount = promptInt("What is the value of " + title + "'s discount? $", 1, 1000);
		if (discount < 0) {
			printB("You have been returned to the menu.");
			return;
		}
		String sDiscount = "$ " + Integer.toString(discount);
		printB("Before creating " + title + ", please veryify the information you have entered:");
		System.out.println(bStr("    Title: ") + title);
		System.out.println(bStr(" Discount: ") + sDiscount);
		if (checkYN("Confirm creation of " + title + ".")){
			try 
			{
				String q = "INSERT INTO organization (title, discount) VALUES (?, ?)";
				PreparedStatement p = con.prepareStatement(q);
				p.setString(1, title);
				p.setInt(2, discount);
				p.executeUpdate();
				printB(title + " was successfully created!");
			}
			catch(SQLException e) {
	 			printE("Caused an exception: " + mainText + e);
			}
		}
		else {
			printB("Creation of " + title + " was cancelled!");
		}
	}

	// ---------------- MANAGER HELPER METHODS --------------------------------

	/**
	 * Gets the id of a specific depot
	 * @return depot id, -1 on quit
	 */
	private int getLocation()
	{
		try {
			PreparedStatement allLocationsQuery = con.prepareStatement("SELECT DISTINCT city FROM depot ORDER BY city");
			PreparedStatement depotsInLocationQuery = con.prepareStatement("SELECT id, type FROM depot WHERE city=? ORDER BY id");
			ResultSet locationRS = allLocationsQuery.executeQuery();
			ArrayList<String> locations = new ArrayList<String>();
			locations.add("Please select one of the cities we operate in: ");
			while (locationRS.next()) {
				locations.add(locationRS.getString("city"));
			}
			int locationChoice = printMenu(locations);
			if (locationChoice == 0) {
				clearTerminal();
				return -1;
			}
			String sLocation = locations.get(locationChoice);
			
			depotsInLocationQuery.setString(1, sLocation);
			ResultSet depotsRS = depotsInLocationQuery.executeQuery();
			
			ArrayList<Integer> depotID = new ArrayList<Integer>();
			ArrayList<String> depotType = new ArrayList<String>();
			
			depotType.add("Please select a depot in " + sLocation + ":");
			while (depotsRS.next()) {
				depotID.add(depotsRS.getInt("id"));
				depotType.add(depotsRS.getString("type"));
			}
			int depotChoice = printMenu(depotType);
			if (depotChoice == 0) {
				clearTerminal();
				return -1;
			}
			return depotID.get(depotChoice -1);
		}
		catch (SQLException e) {
			System.out.println("Threw a SLQ Exception: " + e);
			e.printStackTrace();
			return -1;
		}
	}

	/**
	 * Gets the name of a depot based on id
	 * @param id of depot
	 * @return String depot name, "" if do depot exists with that id
	 */
	private String getDepotName(int id)
	{
		try 
		{
			String query = "SELECT city, type FROM depot WHERE id=?";
			PreparedStatement p = con.prepareStatement(query);
			p.setInt(1, id);
			ResultSet rs = p.executeQuery();
			if (!rs.next()) {
				printE("Error, invalid depotId!");
			}
			else {
				return rs.getString("city") + " " + rs.getString("type");
			}
		}
		catch (SQLException e) {
			System.out.println("Threw a SLQ Exception: " + e);
		}
		return "";
	}

	/**
	 * queries and returns the odometer value for vehicle
	 * @param vin corresponding to a specific vehicle
	 * @return odometer of vehicle
	 */
	private int getOdometer(int vin)
	{
		try {
			String query = "SELECT odometer FROM vehicle WHERE vin=?"; 
			PreparedStatement p = con.prepareStatement(query);
			p.setInt(1, vin);
			ResultSet rs = p.executeQuery();
			if (rs.next()) {
				return rs.getInt("odometer");
			}
			else {
				return -1;
			}		
		}
		catch (SQLException e) {
			System.out.println("Threw a SLQ Exception: " + e);
			return -1;
		}
	}

	/**
	 * gets the specific id of a customer from their name
	 * @return id of customer, -1 on quit
	 */
	private int getCustomerId()
	{
		try {
			while (1 == 1) 
			{
				String name = promptString("What is the name of the customer who rented the vehicle? ");
				if (name.equals("q")) {
					return -1;
				}
				String query = "SELECT id FROM customer WHERE name=?"; 
				PreparedStatement p = con.prepareStatement(query);
				p.setString(1, name);
				ResultSet rs = p.executeQuery();
				if (rs.next()) {
					return rs.getInt("id"); // TODO Consider if multiple customers with same name
				}
				else {
					printE("No customer found with that name, please try again! ");
				}		
			}
		}
		catch (SQLException e) {
			System.out.println("Threw a SLQ Exception: " + e);
			return -1;
		}
	}

	/**
	 * 
	 * @param id of customer
	 * @return vin or -1 to quit
	 */
	private int getVin(int id) 
	{
		try {
			ArrayList<String> v = new ArrayList<String>();
			v.add("Please select the VIN of the vehicle being returned:");
			String query = "SELECT vin FROM rents WHERE id=?"; 
			PreparedStatement p = con.prepareStatement(query);
			p.setInt(1, id);
			ResultSet rs = p.executeQuery();
			if (!rs.next()) {
				printE("User has no rentals!");
				return -1;
			}
			else {
				do {
					v.add(Integer.toString(rs.getInt("vin")));
				} while (rs.next());
				int index = printMenu(v);
				if (index == 0) {
					return -1;
				}
				return Integer.parseInt(v.get(index));
			}
		}
		catch(SQLException e) {
			System.out.println("Threw a SLQ Exception: " + e);
			return -1;
		}
	}

	/**
	 * Returns the dropoff depot
	 * @param id of user
	 * @param vin vehicle
	 * @return depot id of dropoff depot
	 */
	private int getDropoffDepot(int id, int vin)
	{
		try {
			String q = "SELECT dropoff_depot FROM rents WHERE id=? and vin=?";
			PreparedStatement p = con.prepareStatement(q);
			p.setInt(1, id);
			p.setInt(2, vin);
			ResultSet r = p.executeQuery();
			if (r.next()) {
				return r.getInt("dropoff_depot");
			}
		}
		catch(SQLException e) {
 			printE("Caused an exception: " + mainText + e);
		}
		return -1;
	}

	// --------------- MANAGER MENU METHOD ------------------------------------

	/**
	 * Prints and prompts user to select a manager option
	 * @return user choice
	 */
	public int printActions()
	{
		ArrayList<String> m = new ArrayList<>();
		m.add("What department would you like to visit today?");
		m.add("Vehicles");
		m.add("Groups");
		return printMenu(m);
	}

	/**
	 * After user selects "Vehicles", they are prompted to chose
	 * one of the following actions
	 * @return user choice
	 */
	public int printVehicleActions()
	{
		ArrayList<String> m = new ArrayList<>();
		m.add("What would you like to do today?");
		m.add("Return a Vehicle");
		m.add("View Inventory");
		m.add("Add a new Vehicle");
		return printMenu(m);
	}

	/**
	 * After user selects "Groups", they are prompted to chose
	 * one of the following actions
	 * @return user choice
	 */
	public int printGroupActions()
	{
		ArrayList<String> m = new ArrayList<>();
		m.add("What would you like to do today?");
		m.add("View all Groups");
		m.add("Add a new Group");
		return printMenu(m);
	}

	/**
	 * Prompts the user to order vehicle inventory by one of the options
	 * @return user choice
	 */
	private int printOrderOptions()
	{
		ArrayList<String> m = new ArrayList<>();
		m.add("How would you like the vehicles ordered?");
		m.add("VIN");
		m.add("Type");
		m.add("Make");
		m.add("Odometer");
		return printMenu(m);
	}
}