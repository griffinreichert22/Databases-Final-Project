import java.util.*;
import java.sql.*;

public class MainInterface extends Methods
{
	private static Connection con;

	/**
	 * Main Method
	 */
	public static void main(String [] args) 
	{	
		try {
			clearTerminal();
			System.out.println("Welcome to " + brightText + "Hurts-Rent-A-Lemon" + mainText + "! \n");
			boolean logInFlag = true;
			String userId = "";	
			String password = ""; 	
			while (logInFlag) 
			{
				System.out.println("Please enter your credentials to begin:");
				userId = promptString("Plese enter User ID: ");
				if (userId.equals("q")) {
					break;
				}
				System.out.println("Please enter your credentials to begin:");
				password = promptString("Plese enter password: ");
				if (password.equals("q")) {
					break;
				}
				try
				{
					//attempts to make a connection with userId and password
					System.out.println("Establishing Connection...");
				 	con = DriverManager.getConnection("jdbc:oracle:thin:@edgar1.cse.lehigh.edu:1521:cse241", userId, password);
					logInFlag = false; //If conneciton is successful
					clearTerminal();
					
					int userState = -1;
					while(userState != 0)
					{
						userState = printMainMenu();
						if (userState == 1) {  		// user selects customer
							Customer customer = new Customer(con);
							int type = customer.getType();
							if (type == 1) { 		// new customer
								customer.register();
							}
							else if (type == 2) { 	// existing customer
								if (customer.verify()) {
									customer.nextAction();
								}
							}
						}
						else if (userState == 2) { 	//user selects manager
							Manager manager = new Manager(con);
							int action = -1;
							while (action != 0) 
							{
								action = manager.printActions();
								if (action == 1) { 		// Vehicles
									int vAction = -1;
									while (vAction != 0) {
										vAction = manager.printVehicleActions();
										if (vAction == 1) {			// return a vehicle
											manager.returnVehicle();
										}
										else if (vAction == 2) {	// view all vehicles
											manager.viewInventory();
										}
										else if (vAction == 3) { 	// add a new vehicle
											manager.addVehicle();
										}
									}									
								}
								else if (action == 2) { 	// Groups
									int gAction = -1;
									while (gAction != 0) {
										gAction = manager.printGroupActions();
										if (gAction == 1) {		// view all groups
											manager.viewAllGroups();
										}
										else if (gAction == 2) { // add a new group
											manager.addGroup();
										}
									} // while gAction
								}
							} // while action
						}
					}
					System.out.print("\nThank you for renting with ");
					printB("Hurts-Rent-A-Lemon!");
					con.close();
					System.out.println("\nConnection Closed");
					System.out.println(ANSI_RESET);
					return;
				}
				catch(SQLException e) {
					printE("Incorrect username or password! " + brightText + "Please try again");
				}
			} // log in
		} // throwable try catch
		catch (Throwable t)
		{
			clearTerminal();
			printE("\n\nOur system encountered an unexpected error!");
			printB("The development team will fix this as soon as possible!");
		}
		System.out.print("\nThank you for renting with ");
		printB("Hurts-Rent-A-Lemon!");
		System.out.println(ANSI_RESET);
	} //main method

	/**
	 * Prints the main menu
	 * @return int user selects
	 */
	public static int printMainMenu()
	{
		ArrayList<String> m = new ArrayList<>();
		m.add("How may we help you today?");
		m.add("Customer");
		m.add("Depot Manager");
		return printMenu(m);
	}
}