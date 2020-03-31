# ***CSE 241 Final Project***
## By: Griffin Reichert
---
## **Outline of Readme:**
1. **Summary of Project**
2. **Overview of Directory Structure**
    - Directory Contents
    - Makefile Instructions
    - Organization of Classes
3. **Overview of Interfaces**
    - Customer Interface
    - Manager Interface
    - Notable Features
4. **Testing the Program**
    - Running the Program
    - Suggested Test Cases
5. **Database and Data**
    - Source of Data
    - Use of PL/SQL
    - Database Design
---
## **Summary of Project**

Welcome to my final project for CSE 241! This is my final database project which is a rental car agency interface. I have enjoyed working hard to produce a program that is dynamic and professional. Please read this README to understand the aspects of this program before running and testing it. Thank you, and enjoy!

Note: I reccommend viewing this README in an enviromnent that enables you to view the formatting.

---
## **Overview of Directory Structure**

### **Directory Contents**

The main directory of this project is `fgr221reichert/`. It contains this README, the `fgr221.jar` executable file,  the `ojdbc7.jar` drivers, the `fgr221/` directory, the `sql/` directory, and the makefile. 

All java source code for the project is located in the `fgr221/` directory. The `sql/` directory contains a variety of SQL based files including the data, table creation, and queries I used throughout development. To learn more about the data I used, read section 5: **Database and Data**.

### **Makefile Instructions**

This project utilizes a makefile stored in the main directory `fgr221reichert/`. The makefile has three commands which should be execuded while in `fgr221reichert/`:
- `make`: compiles all source code in `fgr221/`, moves all `*.class` files and `Manifest.txt` to main directory, adds `Manifest.txt` and class files into a JAR file named `fgr221.jar`, then moves them back into `fgr221/`.
- `make run`: executes `java -jar fgr221.jar` to run the program
- `make clean`: deletes all `*.class` files and `fgr221.jar`.

### **Organization of Classes**

`MainInterface.java` is the main class of the program. All actions begin here and move between other class objects created as the program runs. All classes extend the `Methods.java` class which is a collection of methods I created that are universally used by the project.  


---
## **Overview of Interfaces**
In this project, I chose to create two comprehensive, user-friendly, dynamic interfaces that model real world use cases. I modeled two users who would use this product, a customer and a manager and implemented the features each would need. 

The interfaces print menus that allow the user to select their next option, or go back to the previous menu. Users are able to view information stored in the database, and are prompted to enter information to be inserted into the database.

**IMPORTANT NOTE:** By entering ***`q`*** at any propt in the interface, the user can quit and will immediately be returned to the last menu.  

The user is always asked to verify their information is correct before the system will execute an action. For example before registering a new customer, the system will ask them if the information they have entered is correct. This helps avoid unneccesary insertion into the database, as well as provide the customer with a more comprehensive user experience. 

### **Customer Interface**

In designing the customer interface, I wanted the customer to be able to create new rentals, join new groups, and view important information in a visually appealing way.

When a user selects Customer from the main menu, they are asked which type of customer they are:
1. New Customer
    - User will be prompted to provide information to register in the system 
2. Existing Customer
    - User will be prompted to verify themselves by providing their drivers license number.

Once a customer has been verified, they will be able to utilize the features of the customer interface. Users are then asked if they would like to go to `My Account` or `Reservations`. 

Features of `My Account`:
- View account information
    - Here the customer is shown the account information they have entered into the system. 
- View Charges to my account
    - If the user has charges associated with their rentals, this prints all rentals and the charges to the customer. It factors in the discounts they may have from the groups they may be a part of, and prints the total ammount due for each rental.
    - Good users to test with: Griffin Reichert, or Murvyn Worms have lots of charges, Benni Cawood has none.
- View group memberships
    - This allows the user to view which groups they are a member of, and the discounts associated with each group
    - Note: Griffin Reichert is a good test case for this (multiple group memberships), Benni Cawood is also good (no group memberships).
- Join a new group
    - Here the user can join a new group either by selecting them from a list, or by entering the name. They will then be prompted to provide the verification code. Joining a group will allow the customer to recieve their corresponding discount. 
    - Since joining a group requires a specific verification code, I have found it is easiest to use the manager interface to view all groups and their verification codes, pick one, then come back to the customer and enter that code. 
    - Note when testing, have a customer join a new group then view memberships and see it appear. 

Features of `Reservations`:
- Make a new reservation
    - This is one of the most important features from a business perspective as customers need to be able to rent vehicles from the interface! The user will be asked to select which depot they would like to rent from, and then select the vehicle they wish to rent. 
    - Then they will be prompted to answer questions about other rental information.
    - If a user confirms they would like to make a rental, this will insert the rental information into the `rents` table, as well as creating the appropriate charges in the `charges` table. 
    - It is discussed in the notable features table about Rental Concurrency and Valid Rentals, but this method has a ton of features I have implemented to make sure that the dates of rentals do not conflict with existing rentals of the same vehicle, and that the user has a valid driverss license. 
- View Reservations
    - This feature is self explanatory, the user can view details about reservations they have in the system.
    - Murvyn Worms or Griffin Reichert are great test cases for this (lots of reservations), so is Benni Cawood (no reservations).

### **Manager Interface**

In designing the manager interface, I was focused on adding features that actual depot managers would need, such as the ability to conveniently return a rental car, view different information in different ways, and create new entries in the system. 

Managers are asked if what they would like to deal with:
1. Vehicles
    - Managers can first and foremost return a vehicle. This will update the mileage of the rental, the odometer of the vehicle, and the location of the vehicle if it was dropped off at a different depot than it was picked up at, so that inventory can be accurately accounted for. 
    - Managers are able to view inventory in a specific location. Since managers may have different needs based on what they are looking for, the vehicles can be ordered by different characteristics which is a neat feature.
    - As in the real world, rental car agencies require new vehicles to replace older ones and expand their supply, so I enabled the manager to be able to create a new vehicle in the `vehicles` table. 
2. Groups
    - Managers can view all groups, their verification codes, and their discounts. This is a helpful way to get a verification code before a customer tries to join a group and is prompted for one. 
    - In the real world, new corporate groups would come to Hurts and want to be added to the system, so the manager is able to add new groups as they come along. 

### **Notable Features**

I have truly enjoyed creating this project, and have poured countless hours into the details of these interfaces to make them realistic, easy to use, and professional. Here are some of the features that I am most proud of (in no particular order), that I hope you look for during testing:

1. Customer Verification 
    - Existing customers are asked to verify themselves by providing their drivers license number. While verification was not required, I chose to implement this as to best model a log in that a real system would have.
    - The user is given three chances to provide the drivers license number corresponding to their account. As in the real world, when the user enters three incorrect attemps, they are sent back to the main menu, and are not verified.
2. Dates and their Methods
    - Very early on in this project, I identified that dealing with dates would be a significant challenge that I would need to figure out. Here is how I decided to tackle this.
    - In the database, dates are stored as Date objects, but I wanted a more dynamic data structure that would allow me to easily let the user input dates, compare dates, and insert said dates into my prepared statements to put them in the database.
    - In order to accomplish this, I use two data structures. The first is a string of the format "yyyy-mm-dd", which is used for printing, prepared statements, etc. The second, and more dynamic data structure is a Map storing years, months and days. Maps can be compared, and dates can be converted between the two types. 
3. The Entire Methods Class
    - I refactored a ton of methods to be universally accessible in the Methods class. These have some neat features such as how I print Menus, compare dates, transfer from dates to strings, and most importanlty, prompt users for information. Developing these methods helped me implement cool features later on by relying on work I had already done.
4. View charges
    - I implemented the ability for customers to view charges to their account and think that the way that it prints and formats is very clean. This is a realistic way for customers to view their fees, discounts, and total charges for rentals. 

---
## **Testing the Program**

### **Running the Program**

The program can be run by entering `make run` in the command line, or by entering `java -jar fgr221.jar`. It will ask the user to enter their User ID and Password in order log into the database. 

Once the user provides the valid credentials, they can interract with the interfaces described above.

At any point while using the interface, the user can enter `'q'` to quit and be returned to the previous menu. 

### **Test Cases**

There are a variety of test cases that can be run on this interface. I have broken down a few cases to test for on each interface.
1. **Customer Interface**
Here are a list of customers that may be helpful when testing. I have tried to select ones that have varrying attributes (rentals vs no rentals, group memberships vs no group memberships, etc) so that all edge cases can be tested. 

I noted in the Overview of Interfaces section where each customer would be a good fit

Note on format: name (DL: drivers license number) 
- Griffin Reichert (DL: 12345)
- Murvyn Worms (DL: 55319215)
- Benni Cawood (DL: 44716692)

Other Notes when Testing:
- Griffin Reichert's Drivers License expires 2022-06-09, so try to make a reservation after that.
- The Ford Mustang (VIN: 15) in the Atlanta train station is rented from 2019-12-21 to 2019-12-25. Pick a customer and try to rent this vehicle at that time. (works with other vehicles but I am providing an example date)

2. **Manager Interface**

The manager interface is more straightforward from a testing perspective. Try to view inventory by ordering it in different ways. Note that Managers can create groups with the same name so that customers can recieve different discount levels within a company.

---
## **Database and Data**

### **Source of Data**
Data is stored in the `sql/` directory, in the `data/` table. Data is stored in `.sql` files named based on the table they correspond to. Most data was generated with the help of Mockaroo.com. Some tables I generated the data for manually. 

### **Use of PL/SQL**

I utilized triggers to help me create unique values for primary keys in my tables. I included these triggers in the `triggers.sql` file. I also included some queries that were helpful to me during my development in the `queries.sql` file.

### **Database Design**
I wanted to include one note about how the interface interracts with the database design. Since `customer` has id as the primary key, there could be two customers with the same name in the database. When using the interface, there are a few times that the user is prompted to enter a customers name, and the program uses this to find their ID. I realize that if there are multiple users in the system with the same name, this would cause issues. However I chose to implement the interface this way because realistically customers would not know their unique id, and the user experience is improved by using name to identify the user. If I could go back in time I would have the primary key include name, however the interface is funcitonal and complete. 
