import java.util.*;
import java.sql.*;

public class Charge extends Methods
{
	private Connection con;
	private int id;
	private int vin;
	private String fee;
	private PreparedStatement p;

	/**
	 * Constructor for Charge object
	 * @param c Connection
	 * @param id of user
	 * @param vin of vehicle being rented
	 * @param fee name of fee being charged
	 */
	public Charge(Connection c, int id, int vin, String fee)
	{
		con = c;
		this.id = id;
		this.vin = vin;
		this.fee = fee;
	}

	/**
	 * Prepare the Prepared Statement
	 * @return true if prepared, false if not
	 */
	public boolean prepare()
	{
		try {
			p = con.prepareStatement("INSERT INTO charges VALUES (?, ?, ?)");
			p.setInt(1, id);
			p.setInt(2, vin);
			p.setString(3, fee);
			return true;
		}
		catch (SQLException e) {
			printE("Error creating prepare statement! " + e);
			return false;
		}
	}

	/**
	 * Inserts charge into database 
	 * @return true if inserted, false otherwise
	 */
	public boolean insert() 
	{
		try {
			p.executeUpdate();
			return true;
		}
		catch (SQLException e) {
			printE("Error inserting into charges! " + e);
			return false;
		}
	}
}
