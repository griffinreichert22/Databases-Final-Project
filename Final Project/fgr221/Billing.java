import java.util.*;
import java.sql.*;

public class Billing extends Methods
{
	private Connection con;
	private int userId;
	private ArrayList<Integer> vins;

	public Billing (Connection c, int id)
	{
		con = c;
		userId = id;
		vins = new ArrayList<Integer>();
	}

	public void viewCharges()
	{
		if (!populateVins()) { 	// fills arraylist vins with vins of vehicles user has rented
			return; 			// returns if user has no reservations
		}
		String s = "You have " + vins.size() + " reservation";
		if (vins.size() != 1) {
			s += "s";
		}
		s += "!\n";
		printB(s);

		for (int i = 0; i < vins.size(); i++) {
			printCharges(vins.get(i));
		}
		zeroReturn();
	}

	// ------------- Billing Helper Methods -----------------------------------

	/**
	 * Helper method for view charges
	 * prints fees, discount, total for each reservation
	 * @param vin of reservation
	 */
	private void printCharges(int vin)
	{
		try 
		{
			String query = "SELECT * FROM charges NATURAL JOIN fees WHERE id=? AND vin=? ORDER BY price DESC";
			PreparedStatement p = con.prepareStatement(query);
			p.setInt(1, userId);
			p.setInt(2, vin);
			ResultSet rs = p.executeQuery();

			ArrayList<Double> t = new ArrayList<Double>();

			if (!rs.next()) {
				return;
			}
			else {
				String pFormat = "%-19s%8s%6s%11s\n";
				
				auxilaryRentalInfo(vin); // prints vehicle and start date

				System.out.print(brightText);
				System.out.printf(pFormat, "Fee:", "Rate:", "Qty:", "Charge:");
				printE("--------------------------------------------");
				Double tempPrice = rs.getDouble("price");
				String tempFee = rs.getString("fee");
				int tempQuant = getQuantity(tempFee, vin);
				Double tempTotal = tempPrice * tempQuant;
				int r = String.format("%.2f", tempPrice).length();
				int c = 6;
				do {
					String fee = rs.getString("fee");
					Double price = rs.getDouble("price");
					int qty = getQuantity(fee, vin);
					if (qty < 1) {
						continue;
					}
					Double total = qty * price;
					t.add(total);
					String sFee = switchFee(fee);
					String sQty = Integer.toString(qty);

					String sPrice = String.format("%.2f", price);
					while (sPrice.length() < r) {
						sPrice = " " + sPrice;
					}
					sPrice = "$ " + sPrice;
					
					String sTotal = String.format("%.2f", total);
					while (sTotal.length() < c) {
						sTotal = " " + sTotal;
					}
					sTotal = "$ " + sTotal;

					System.out.printf(pFormat, sFee, sPrice, sQty, sTotal);
				} while (rs.next());
				Double total = 0.0;
				for (int i = 0; i < t.size(); i++) {
					total += t.get(i);
				}
				String sTotal =  String.format("%.2f", total);
				int f = sTotal.length();
				sTotal = "Total Fees:  $ " + sTotal;

				System.out.print(brightText);
				System.out.printf("%44s\n", sTotal);

				String q2 = "SELECT * FROM organization NATURAL JOIN member WHERE id=? ORDER BY discount DESC";
				PreparedStatement p2 = con.prepareStatement(q2);
				p2.setInt(1, userId);
				ResultSet r2 = p2.executeQuery();
				int d = 0;
				if (r2.next()) {
					printE("--------------------------------------------"); // length 44
					d = String.format("%.2f", Double.valueOf(r2.getInt("discount"))).length();
					do {
						String orgName = r2.getString("title");
						Double discount = Double.valueOf(r2.getInt("discount"));
						total -= discount;
						String sDiscount = String.format("%.2f", discount);
						while (sDiscount.length() < d || sDiscount.length() < c) {
							sDiscount = " " + sDiscount;
						}
						sDiscount = "Discount:  $ " + sDiscount;
						String sMem = "Member of " + orgName;
						int mem = sMem.length();
						int dis = sDiscount.length();
						for (int i = 0; i < (44 - mem - dis); i++) {
							sMem += " ";
						}
						printB(sMem + sDiscount);
					} while (r2.next());
				}
				printE("--------------------------------------------"); // length 44
				if (total < 0.0) {
					total = 0.0;
				}
				sTotal = String.format("%.2f", total);
				while (sTotal.length() < r || sTotal.length() < c || sTotal.length() < f || sTotal.length() < d) {
					sTotal = " " + sTotal;
				}
				sTotal = "Total Due:  $ " + sTotal;
				System.out.print(brightText);
				System.out.printf("%44s\n", sTotal);
				System.out.print(mainText);
			}
		}
		catch (SQLException e) {
			printE("Caused an exception: " + e);
			e.printStackTrace();
		}
	}

	private void auxilaryRentalInfo(int vin)
	{
		String vehicle = "";
		String start = "";
		try {
			String query = "SELECT * FROM rents NATURAL JOIN vehicle WHERE id=? AND vin=?";
			PreparedStatement p = con.prepareStatement(query);
			p.setInt(1, userId);
			p.setInt(2, vin);
			ResultSet r = p.executeQuery();
			if(r.next()) {
				vehicle = r.getString("make") + " " + r.getString("model");
				java.sql.Date date = r.getDate("start_date");
				start = dateToString(date);
			}
			System.out.println(bStr("\nVehicle: ") + vehicle); 
			System.out.println(bStr("Start Date: ") + start + "\n");
		}
		catch (SQLException e) {
			printE("Caused an exception!");
		}
		
	}

	/**
	 * 
	 * @param
	 * @param
	 * @return
	 */
	private int getQuantity(String s, int vin)
	{
		if (s.equals("mileage")) {
			return getMileage(vin);
		}
		else if (s.equals("Compact") || s.equals("Premium") || s.equals("Truck") ||
			s.equals("Midsize") || s.equals("SUV") || s.equals("insurance")) 
		{
			return getDuration(vin);
		}
		else {
			return 1;
		}
	}

	/**
	 * 
	 * @param
	 * @return
	 */
	private int getMileage(int vin)
	{
		try 
		{
			String query = "SELECT mileage FROM rents WHERE id=? AND vin=?"; // TODO consider keyword is not null to avoid null mileages
			PreparedStatement p = con.prepareStatement(query);
			p.setInt(1, userId);
			p.setInt(2, vin);
			ResultSet rs = p.executeQuery();
			if (rs.next()) {
				return rs.getInt("mileage");
			}
			else {
				return -1;
			}
		}
		catch (SQLException e) {
			printE("Caused an exception: " + e);
			return -1;
		}
	}

	/**
	 * 
	 * @param
	 * @return
	 */
	private int getDuration(int vin)
	{
		try 
		{
			String query = "SELECT ((end_date - start_date) + 1) AS dif FROM rents WHERE id=? AND vin=?";
			PreparedStatement p = con.prepareStatement(query);
			p.setInt(1, userId);
			p.setInt(2, vin);
			ResultSet rs = p.executeQuery();
			if (rs.next()) {
				return rs.getInt("dif");
			}
			else {
				return -1;
			}
		}
		catch (SQLException e) {
			printE("Caused an exception: " + e);
			return -1;
		}
	}

	/**
	 * @param
	 * @return
	 */
	private String switchFee(String s)
	{
		if (s.equals("mileage")) {
			return "Mileage";
		}
		else if (s.equals("Compact")) {
			return "Compact Vehicle";
		}
		else if (s.equals("Midsize")) {
			return "Midsize Vehicle";
		}
		else if (s.equals("SUV")) {
			return "SUV Rental";
		}
		else if (s.equals("Premium")) {
			return "Premium Rental";
		}
		else if (s.equals("Truck")) {
			return "Truck Rental";
		}
		else if (s.equals("insurance")) {
			return "Insurance";
		}
		else if (s.equals("carbon")) {
			return "Carbon Offset";
		}
		else if (s.equals("gas")) {
			return "Fuel Refill";
		}
		else if (s.equals("navigation")) {
			return "Navigation System";
		}
		else if (s.equals("satelite")) {
			return "Satelite Radio";
		}
		else if (s.equals("carseat")) {
			return "Child Seat";
		}
		else if (s.equals("depot")) {
			return "Depot Change";
		}
		else {
			return s;
		}
	}

	/**
	 * 
	 * @return
	 */
	private boolean populateVins()
	{
		try 
		{
			String query = "SELECT DISTINCT vin FROM charges WHERE id=?";
			PreparedStatement p = con.prepareStatement(query);
			p.setInt(1, userId);
			ResultSet rs = p.executeQuery();
			if (!rs.next()) {
				clearTerminal();
				printB("You have no charges to your account!");
				return false;
			}
			else {
				do {
					vins.add(rs.getInt("vin"));
				} while (rs.next());
			}
			return true;
		}
		catch (SQLException e) {
			printE("Caused an exception: " + e);
			return false;
		}
	}
}

