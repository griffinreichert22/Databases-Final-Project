import java.util.*;
import java.sql.*;
import java.text.SimpleDateFormat;

/**
 * Class storing global methods and variables used across classes 
 */
public class Methods
{
	/**
	 * Scanner used on global scope of project
	 */
	public static Scanner scan = new Scanner(System.in);

	/**
	 * Creates simple date format object
	 */
	public static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

	/**
	 * Clears the terminal and prints header
	 */
	public static void clearTerminal()
	{
		System.out.println(background);
		System.out.print(ANSI_CLEAR); //Clears terminal
		System.out.flush();
		System.out.println(TEXT_GREEN + "\n    $$$$$$$$$$$$$$$$$$$$$$");
		System.out.println("    $ " + TEXT_BRIGHT_YELLOW + ANSI_BOLD + "Hurts-Rent-A-Lemon" + ANSI_RESET + background + TEXT_GREEN + " $");
		System.out.println("    $$$$$$$$$$$$$$$$$$$$$$\n" + mainText);
	}

	/**
	 * Prints a string in the global error color
	 * @param s String to print
	 */
	public static void printE(String s) {
		System.out.println(errorText + s + mainText);
	}

	/** 
	 * Prints a string in the global bright color
	 * @param s String to print
	 */
	public static void printB(String s) {
		System.out.println(brightText + s + mainText);
	}

	/**
	 * Formats a string as bright text
	 * @param s input string
	 * @return bright string containing s
	 */
	public static String bStr(String s) 
	{
		return brightText + s + mainText;
	}

	/**
	 * Formats a string as error text
	 * @param s input string
	 * @return error string containing s
	 */
	public static String eStr(String s) 
	{
		return errorText + s + mainText;
	}

	/**
	 * Prompts user to input a string
	 * @param prompt to ask user
	 * @return non-empty string
	 */
	public static String promptString(String prompt)
	{
		while (1 == 1)
		{
			System.out.print(prompt);
			String s = scan.nextLine();
			if (s.isEmpty()) {
				clearTerminal();
				System.out.print(errorText + "Please try again! " + brightText + "('q' to quit) " + mainText);
				continue;
			}
			clearTerminal();
			return s;
		}
	}

	/**
	 * Prompts the user to input an integer within a specified range
	 * @param prompt to ask the user
	 * @param low lower bound for int (inclusive)
	 * @param high upper bound for int (inclusive)
	 * @return user input int within range
	 * 		   returns -99 if user quit by entering q
	 */
	public static int promptInt(String prompt, int low, int high) 
	{
		while (1 == 1)
		{
			System.out.print(prompt);
			if (scan.hasNextInt()) {
				int cur = scan.nextInt();
				scan.nextLine();
				clearTerminal();
				if (low <= cur && cur <= high) {
					return cur;
				}
				printB("Input must be between " + errorText + low + brightText + " and " + errorText + high + brightText + "! ");
				System.out.print(errorText + "Please try again! " + brightText + "('q' to quit) " + mainText);
				continue;
			}
			else if (scan.hasNext("q")) {				
				clearTerminal();
				scan.nextLine();
				clearTerminal();
				return -99;
			}
			scan.nextLine();
			clearTerminal();
			System.out.print(errorText + "Please try again! " + brightText + "('q' to quit) " + mainText);
		}
	}

	/**
	 * Prompts user to input a valid date
	 * @return map holding int formats of date attributes
	 */
	public static Map<String, Integer> promptDate()
	{
		int y = promptInt("Please enter a valid year (format yyyy): ", 1900, 2030);
		if (y == -99) {
			return null;
		}
		int m = promptInt("Please enter a valid month (number format mm): ", 1, 12);
		if (m == -99) {
			return null;
		}
		int maxDay = 31; //gets days in the month
		if (m == 4 || m == 6 || m == 9 || m == 11) {
			maxDay = 30;
		}
		else if (m == 2) {
			maxDay = 28;
		}
		int d = promptInt("Please enter a valid day (number format dd): ", 1, maxDay);
		if (d == -99) {
			return null;
		}
		Map<String, Integer> date = new HashMap<>();
		date.put("year", y);
		date.put("month", m);
		date.put("day", d);
		return date;
	}

	/**
	 * Formats a date as a string "yyyy-mm-dd"
	 * @param map holding year month day
	 * @return string of format "yyyy-mm-dd"
	 */
	public static String toString(Map<String, Integer> map) {
		if (map == null) {
			return "null";
		}
		String y = map.get("year").toString();
		String m = map.get("month").toString();
		String d = map.get("day").toString();
		if (m.length() == 1) {
			m = "0" + m;
		}
		if (d.length() == 1) {
			d = "0" + d;
		}
		return y + "-" + m + "-" + d;
	}

	/** 
	 * Takes a string of format "yyyy-mm-dd" and converts it to a map
	 * @param s string of format "yyyy-mm-dd"
	 * @return map of date
	 */
	public static Map<String, Integer> toMap(String s)
	{
		//2021-03-24
		//0123456789
		int y = Integer.parseInt(s.substring(0,4));
		int m = Integer.parseInt(s.substring(5,7));
		int d = Integer.parseInt(s.substring(8,10));
		Map<String, Integer> date = new HashMap<>();
		date.put("year", y);
		date.put("month", m);
		date.put("day", d);
		return date;
	}

	/** 
	 * Turns a date obejct into a string
	 * @param sqlDate Date object in java.sql.Date format
	 * @return string of format "yyyy-mm-dd"
	 */
	public static String dateToString(java.sql.Date sqlDate)
	{
		java.util.Date date = new java.util.Date(sqlDate.getTime());
		return simpleDateFormat.format(date);
	}

	/**
	 * Compares two dates in Map format
	 * @param a first date
	 * @param b second date
	 * @return 	1 if a is before b
	 * 			-1 if b is before a
	 * 	 		0 if the same date
	 */
	public static int compare(Map<String, Integer> a, Map<String, Integer> b) {
		if (a == null || b == null || a.isEmpty() || b.isEmpty()) {
			return -1;
		}

		int ay = a.get("year");
		int am = a.get("month");
		int ad = a.get("day");
		int by = b.get("year");
		int bm = b.get("month");
		int bd = b.get("day");
		
		if (ay < by) {
			return 1;
		}
		else if (ay > by) {
			return -1;
		}
		else if (am < bm) {
			return 1;
		}
		else if (am > bm) {
			return -1;
		}
		else if (ad < bd) {
			return 1;
		}
		else if (ad > bd) {
			return -1;
		}
		return 0; //dates are equal
	}

	/**
	 * Holds the user until they enter 0
	 */
	public static void zeroReturn()
	{
		while (1 == 1)
		{
			System.out.print("\nEnter " + brightText + "0" + mainText + " to return: ");
			if (scan.hasNextInt())
			{
				if (scan.nextInt() == 0) {
					clearTerminal();
					scan.nextLine();
					return;
				}
			}
			else if (scan.hasNext("q")) {				
				clearTerminal();
				scan.next();
				return;
			}
			else if (scan.hasNext()) {
				scan.next();
			}
		}
	}

	/**
	 * TODO fix comments
	 * Checks if user input is correct to avoid unneccesary insertion to the database
	 * @return true if correct to allow program to continue with insertion
	 */
	public static boolean checkYN(String str)
	{
		while (1==1) 
		{
			String s = promptString(str + " Enter " + brightText + "'y'" + mainText + " or " + brightText + "'n'" + mainText + ": ");
			if (s.equals("y")) {
				return true;
			}
			if (s.equals("n") || s.equals("q")) {
				return false;
			}
			System.out.print(errorText + "Please try again! " + mainText);
		}
	}

	/**
	 * Prints a menu and returns user selected option
	 * @param a arraylist of strings representing the menu
	 * 			index 0 stores the prompt for the menu
	 * @return index user selected
	 */
	public static int printMenu(ArrayList<String> a)
	{
		while (1 == 1) 
		{
			System.out.println(a.get(0));
			int d = a.get(0).length();
			String bar = "";
			for (int i = 0; i < d; i++) {
				bar += "-";
			}
			printE(bar);


			int n = a.size();
			for (int i = 1; i < n; i++)
			{
				if (i < 10 && n >= 10) {
					System.out.print(" ");
				}
				System.out.print(bracket + "  [" + mainText + i + bracket + "] " + mainText);
				System.out.println(a.get(i));
			}
			if (n >= 10) {
				System.out.print(" ");
			}
			if (a.get(1).equals("Customer")) {
				System.out.println(bracket + "  [" + mainText + "0" + bracket + "]" + mainText + " Quit");

			}
			else {
				System.out.println(bracket + "  [" + mainText + "0" + bracket + "]" + mainText + " Back");
			}
			printE(bar);
			System.out.print("Please select an option: ");
			
			if (scan.hasNext("q")) {
				clearTerminal();
				scan.nextLine();
				return 0;
			}
			if (scan.hasNextInt()) {
				int cur = scan.nextInt();
				if (cur >= 0 && cur < n) {
					clearTerminal();
					scan.nextLine();
					return cur;
				}
			}
			scan.nextLine();
			clearTerminal();
			System.out.print(bStr("Please try again! "));
		}
	}

	/**
	 * ANSI CODES
	 */
	public static final String ANSI_RESET  	= "\u001B[0m";
	public static final String ANSI_BOLD 	= "\u001b[1m";
	public static final String ANSI_UNDER 	= "\u001b[4m";
	public static final String ANSI_CLEAR 	= "\033[H\033[2J";

	/**
	 * ANSI TEXT COLOR
	 */
	public static final String TEXT_BLACK  = "\u001B[30m";
	public static final String TEXT_RED    = "\u001B[31m";
	public static final String TEXT_GREEN  = "\u001B[32m";
	public static final String TEXT_YELLOW = "\u001B[33m";
	public static final String TEXT_BLUE   = "\u001B[34m";
	public static final String TEXT_PURPLE = "\u001B[35m";
	public static final String TEXT_CYAN   = "\u001B[36m";
	public static final String TEXT_WHITE  = "\u001B[37m";

	/**
	 * ANSI BRIGHT TEXT COLOR
	 */
	public static final String TEXT_BRIGHT_BLACK  = "\u001B[90m";
	public static final String TEXT_BRIGHT_RED    = "\u001B[91m";
	public static final String TEXT_BRIGHT_GREEN  = "\u001B[92m";
	public static final String TEXT_BRIGHT_YELLOW = "\u001B[93m";
	public static final String TEXT_BRIGHT_BLUE   = "\u001B[94m";
	public static final String TEXT_BRIGHT_PURPLE = "\u001B[95m";
	public static final String TEXT_BRIGHT_CYAN   = "\u001B[96m";
	public static final String TEXT_BRIGHT_WHITE  = "\u001B[97m";

	/**
	 * ANSI BACKGROUND COLORS
	 */
	public static final String BG_BLACK 	= "\u001b[40m";
	public static final String BG_RED 		= "\u001b[41m";
	public static final String BG_GREEN 	= "\u001b[42m";
	public static final String BG_YELLOW 	= "\u001b[43m";
	public static final String BG_BLUE 		= "\u001b[44m";
	public static final String BG_PURPLE 	= "\u001b[45m";
	public static final String BG_CYAN 		= "\u001b[46m";
	public static final String BG_WHITE 	= "\u001b[47m";

	/**
	 * Selected ANSI formatting colors to be applied on a global scope
	 */
	public static String mainText 	= TEXT_BRIGHT_CYAN;
	public static String brightText = TEXT_BRIGHT_YELLOW;
	public static String errorText 	= TEXT_BRIGHT_PURPLE;	
	public static String bracket	= TEXT_YELLOW;
	public static String background = BG_BLACK;
}

