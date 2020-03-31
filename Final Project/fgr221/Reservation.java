import java.util.*;
import java.sql.*;

public class Reservation extends Methods
{
	private Connection con;
	private int userId;
	private Map<String, Integer> todaysDate = new HashMap<>();
	Map<String, Integer> sDate;
	Map<String, Integer> eDate;

	public Reservation(Connection c, int id)
	{
		con = c;
		userId = id;
		todaysDate.put("year", 2019);
		todaysDate.put("month", 1);
		todaysDate.put("day", 1);
		sDate = null;
		eDate = null;
	}

	// ------------------- Reservation Main Methods ---------------------------

	/**
	 * Creates a new reservation 
	 */
	public void makeNew()
	{
		clearTerminal();
		int pickup_depot = getLocation(); 
		if (pickup_depot == -1) {
			clearTerminal();
			return;
		}
		int vin = getVehicle(pickup_depot);
		if (vin == -1) {
			return;
		}
		
		if (!getValidDates(vin)) {
			return;
		}

		String start_date = toString(sDate);
		String end_date = toString(eDate);

		int dropoff_depot = pickup_depot;
		if (checkYN("Would you like to drop off the vehicle at a different depot?")) {
			dropoff_depot = getLocation();
			if (dropoff_depot == -1) {
				clearTerminal();
				return;
			}
		}

		ArrayList<Charge> charges = new ArrayList<Charge>();
		String vType = getVehicleType(vin);
		if (vType.equals("")) {
			return;
		}
		charges.add(new Charge(con, userId, vin, vType)); 		// standar vehicle charge based on type
		charges.add(new Charge(con, userId, vin, "insurance"));	// standard insurance
		charges.add(new Charge(con, userId, vin, "carbon"));		// standard carbon offset charge
		charges.add(new Charge(con, userId, vin, "mileage"));		// standard mileage charge

		if (pickup_depot != dropoff_depot) {
			charges.add(new Charge(con, userId, vin, "depot"));
		}

		if (checkYN("Would you like to add a navigation system?")) {
			charges.add(new Charge(con, userId, vin, "navigation"));
		}
		if (checkYN("Would you like to add satelite radio?")) {
			charges.add(new Charge(con, userId, vin, "satelite"));
		}
		if (checkYN("Would you like to add a carseat?")) {
			charges.add(new Charge(con, userId, vin, "carseat"));
		}


		for (int i = 0; i < charges.size(); i++) {
			if (!charges.get(i).prepare()) {
				return;
			}
		}
		//TODO consider printing all rental info using an arraylist
		if (checkYN("Please confirm your " + bStr(vType) + " reservation begining on " + bStr(start_date) + ".")) {	
			try {
				String query = 	"INSERT INTO rents (id, vin, start_date, end_date, pickup_depot, dropoff_depot) " +
						"VALUES (?, ?, TO_DATE(?, 'yyyy-mm-dd'), TO_DATE(?, 'yyyy-mm-dd'), ?, ?)";
		
				PreparedStatement p = con.prepareStatement(query);
				p.setInt(1, userId);
				p.setInt(2, vin);
				p.setString(3, start_date);
				p.setString(4, end_date);
				p.setInt(5, pickup_depot);
				p.setInt(6, dropoff_depot);
				p.executeUpdate();
			}
			catch (SQLException e) {
				printE("Unable to create reservation! " + e);
				return;
			}
			for (int i = 0; i < charges.size(); i++) {
				charges.get(i).insert();
			}
			printB("Successfully created reservation!");
		}
		else {
			printB("Reservation cancelled!");
		}
	} //makeNew

	/**
	 * Allows a customer to view all of thiir reservations
	 * STATUS: Completed & Tested
	 */
	public void viewAll()
	{
		clearTerminal();
		try
		{
			String query = 	"SELECT depot.city AS d_city, depot.type AS d_type, rents.start_date AS r_start, rents.end_date AS r_end, vehicle.make AS v_make, vehicle.model as v_model, vehicle.type AS v_type " + 
							"FROM (rents natural join vehicle), depot " + 
							"WHERE rents.id=? AND rents.pickup_depot = depot.id " + 
							"order by rents.start_date";

			PreparedStatement p = con.prepareStatement(query); 
			p.setInt(1, userId);
			ResultSet rs = p.executeQuery();
			if (!rs.next()) {
				printB("You have no reservations. " + mainText + "Enter 1 to make a new reservation!");
			}
			else
			{
				System.out.println("Viewing all reservations: \n");
				String pFormat = "%-15s%-15s%-12s%-12s%-16s%-16s%-8s\n";
				System.out.print(brightText);
				System.out.printf(pFormat, "City", "Depot", "Start Date", "End Date", "Make", "Model", "Type");
				printE("----------------------------------------------------------------------------------------------");
				do 
				{
				    String city = rs.getString("d_city");
				    String depot = rs.getString("d_type");
					java.sql.Date sqlStartDate = rs.getDate("r_start");
					java.sql.Date sqlEndDate = rs.getDate("r_end");
				    String startDate = dateToString(sqlStartDate);
				    String endDate = dateToString(sqlEndDate);
				    String make = rs.getString("v_make");
				    String model = rs.getString("v_model");
				    String type = rs.getString("v_type");
					System.out.printf(pFormat, city, depot, startDate, endDate, make, model, type);
				} while (rs.next());
				printE("----------------------------------------------------------------------------------------------");
				zeroReturn();
			}
		}
		catch(SQLException e)
		{
			System.out.println("Caused an exception: " + e);
		}
	}

	// -------------------------- Reservation Helper Methods ------------------

	/**
	 * Gets the location that the user wants to rent from
	 * @return depotId
	 * STATUS: Tested and it works
	 */
	private int getLocation()
	{
		try 
		{
			PreparedStatement allLocationsQuery = con.prepareStatement("SELECT DISTINCT city FROM depot ORDER BY city");
			PreparedStatement depotsInLocationQuery = con.prepareStatement("SELECT id, type FROM depot WHERE city=? ORDER BY id");
			ResultSet locationRS = allLocationsQuery.executeQuery();
			ArrayList<String> locations = new ArrayList<String>();
			locations.add("Please select one of the cities we operate in: ");
			while (locationRS.next())
			{
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
			while (depotsRS.next())
			{
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
		catch (SQLException e)
		{
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
	 * STATUS: tested return correct vin and quit
	 * @param depotId depot to rent from
	 * @return vin of selected vehicle, (-1 to quit)
	 */
	private int getVehicle(int depotId) 
	{
		int vin = -1;
		try {
			// TODO: modify query to exclude VIN's where id, vin exists in rents
			String query = "SELECT * FROM vehicle WHERE location=? AND vin NOT IN (SELECT vin FROM rents WHERE id=?) ORDER BY type";
			PreparedStatement p = con.prepareStatement(query);
			p.setInt(1, depotId);
			p.setInt(2, userId);
			ResultSet rs = p.executeQuery();
			ArrayList<String> v = new ArrayList<>();
			ArrayList<Integer> vinList = new ArrayList<>();
			if (!rs.next()) {
				printE("No vehicles in selected depot!");
			}
			else {
				v.add("Please select a vehicle to rent: ");
				do {
					String all = brightText;
					all += rs.getString("type");
					all += mainText + " - ";
					all += rs.getString("make") + " ";
					all += rs.getString("model");
					v.add(all);
					vinList.add(rs.getInt("vin"));
				} while (rs.next());
				int vChoice = printMenu(v);
				if (vChoice != 0) {
					vin = vinList.get(vChoice - 1);
				}
			}
		}
		catch (SQLException e) {
			printE("Caused an exception: " + mainText + e);
		}
		return vin;
	}

	/**
	 * @param
	 * @return
	 */
	private String getVehicleType(int vin) 
	{
		try
		{
			String query = "SELECT type FROM vehicle WHERE vin=?";
			PreparedStatement p = con.prepareStatement(query);
			p.setInt(1, vin);
			ResultSet rs = p.executeQuery();
			if (!rs.next()) {
				printE("Error no vehicles with vin!");
				return "";
			}
			else {
				return rs.getString("type");
			}
		}
		catch (SQLException e) {
			printE("Caused exception: " + e);
			return "";
		}
	}

	/**
	 * @param
	 * @return
	 */
	private boolean getValidDates(int vin)
	{
		while (sDate == null && eDate == null) 
		{
			while (compare(todaysDate, sDate) == -1)
			{
				System.out.println("What date would you like to begin your reservation (after 2019-1-1)?");
				sDate = promptDate();
				if (sDate == null) {
					return false;
				}
				if (compare(todaysDate, sDate) == -1) {
					printE("Date must be after 2019-01-01");
				}
			}

			while (compare(todaysDate, eDate) == -1) 
			{
				System.out.println("What date would you like to end your reservation (after 2019-1-1)?");
				eDate = promptDate();
				if (eDate == null) {
					return false;
				}
				if (compare(todaysDate, eDate) == -1) {
					printE("Date must be after 2019-01-01");
				}
			}

			if (compare(sDate, eDate) == -1) {
				printE("End date must be after or the same as the start date!" + bStr(" Please try again!"));
				sDate = null;
				eDate = null;
			}
			else {
				Map<String, Integer> driverLicense = getDriverLicense();
				if (driverLicense == null) {
					return false;
				}

				if (compare(eDate, driverLicense) < 1) {
					printE("Driver License must be valid for rental period!" + bStr(" Please try again!"));
					sDate = null;
					eDate = null;
				}
				else {
					ArrayList<String> s = new ArrayList<String>();
					ArrayList<String> e = new ArrayList<String>();

					try 
					{
						String q = "SELECT start_date, end_date FROM rents WHERE vin=?";
						PreparedStatement p = con.prepareStatement(q);
						p.setInt(1, vin);
						ResultSet r = p.executeQuery();
						if (r.next()) {
							do {
								java.sql.Date sqlStart = r.getDate("start_date");
								java.sql.Date sqlEnd = r.getDate("end_date");
				    			String start = dateToString(sqlStart);
				    			String end = dateToString(sqlEnd);
				    			s.add(start);
				    			e.add(end);
							} while(r.next());
						}
					}
					catch(SQLException ex) {
			 			printE("Caused an exception: " + mainText + ex);
					}
					boolean noConflict = true;
					for (int i = 0; i < s.size(); i++)  // iterates through all reservations involving the vehicle
					{
						Map<String, Integer> a = toMap(s.get(i)); // existing reservation's start date as a map
						Map<String, Integer> b = toMap(e.get(i)); // existing reservation's end date as a map
						
						/**
						 * Checks for conflict with existing rentals
						 * Three cases of conflict:
						 * 	 1. New rental begins during existing rental
						 * 	 2. New rental ends during existing rental
						 * 	 3. New rental starts before existing rental and ends after it
						 */
						
						// check if s is between a and b
						if (compare(a, sDate) >= 0 && compare(sDate, b) >=0) {
							noConflict = false;
							break;
						}
						if (compare(a, eDate) >= 0 && compare(eDate, b) >= 0) { // end date conflicts
							noConflict = false;
							break;
						}
						if (compare(sDate, a) >= 0 && compare(b, eDate) >= 0) { // end date conflicts
							noConflict = false;
							break;
						}
					} 
					if (noConflict) {
						return true;
					}
					else {
						printE("This vehicle is already reserved at that time!" + bStr(" Please try again!"));
						sDate = null;
						eDate = null;
					}
				} // reservation conflict
			} // driver license
		} // while s before e or null
		return false;
	} // method

	private Map<String, Integer> getDriverLicense()
	{
		try
		{
			String query = "SELECT dl_expiration FROM customer WHERE id=?";
			PreparedStatement p = con.prepareStatement(query);
			p.setInt(1, userId);
			ResultSet rs = p.executeQuery();
			if (!rs.next()) {
				printE("Error no vehicles with vin!");
				return null;
			}
			else {
				java.sql.Date sqlDate = rs.getDate("dl_expiration");
				String sExpDate = dateToString(sqlDate);
				return toMap(sExpDate);
			}
		}
		catch (SQLException e) {
			printE("Caused exception: " + e);
			return null;
		}
	}
}