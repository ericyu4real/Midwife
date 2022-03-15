import java.sql.* ;
import java.util.*;
import java.util.ArrayList;
import java.util.List;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.Objects;

class GoBabbyApp
{
    public static void main ( String [ ] args ) throws SQLException
    {
	
      // Unique table names.  Either the user supplies a unique identifier as a command line argument, or the program makes one up.
        String tableName = "";
        int sqlCode=0;      // Variable to hold SQLCODE
        String sqlState="00000";  // Variable to hold SQLSTATE

        if ( args.length > 0 )
            tableName += args [ 0 ] ;
        else
          tableName += "exampletbl";

        // Register the driver.  You must register the driver before you can use it.
        try { DriverManager.registerDriver ( new com.ibm.db2.jcc.DB2Driver() ) ; }
        catch (Exception cnfe){ System.out.println("Class not found"); }

        // This is the url you must use for DB2.
        //Note: This url may not valid now ! Check for the correct year and semester and server name.
        String url = "jdbc:db2://winter2022-comp421.cs.mcgill.ca:50000/cs421";

        //REMEMBER to remove your user id and password before submitting your code!!
        String your_userid = null;
        String your_password = null;
        //AS AN ALTERNATIVE, you can just set your password in the shell environment in the Unix (as shown below) and read it from there.
        //$  export SOCSPASSWD=yoursocspasswd 
        if(your_userid == null && (your_userid = System.getenv("SOCSUSER")) == null)
        {
          System.err.println("Error!! do not have a password to connect to the database!");
          System.exit(1);
        }
        if(your_password == null && (your_password = System.getenv("SOCSPASSWD")) == null)
        {
          System.err.println("Error!! do not have a password to connect to the database!");
          System.exit(1);
        }
        Connection con = DriverManager.getConnection (url,your_userid,your_password) ;
        Statement statement = con.createStatement ( ) ;


	    //ask for practioner_ID
        boolean b=true;
        while (b) {
            Scanner sc= new Scanner(System.in);    //System.in is a standard input stream
            System.out.print("Please enter your practitioner id [E] to exit:\n");
            String pID= sc.next();
            if (pID.equals("E")) {
                statement.close( );
                con.close( );
                System.exit(1);
            }
            else {
                ResultSet rs = statement.executeQuery("SELECT practitioner_id FROM Midwife WHERE practitioner_id="+pID);
                if (! rs.next()) {
                    rs.close();
                    System.err.println ("Invalid Practitioner ID!");
                }
                else {
                    boolean c=true;
                    while (c) {
                        Scanner sc1= new Scanner(System.in);
                        System.out.print("Please enter the date for appointment list [E] to exit:\n");
                        String date = sc1.next();
                        if (date.equals("E")) {
                            statement.close( );
                            con.close( );
                            System.exit(1);
                        }
                        else {
                            String selectSQL = "SELECT pim.appointID, pim.aTime, pim.aDate, pim.practitioner_id, pim.coupleid, Mother.name, Mother.hid FROM Mother, (SELECT Appointment.appointID, Appointment.aTime, Appointment.aDate, Couple.hid, Appointment.practitioner_id, Appointment.coupleid FROM Appointment, Couple WHERE Appointment.coupleid=Couple.coupleid and aDate='"+date+"' and practitioner_id="+pID+" ORDER BY aTime) pim WHERE Mother.hid=pim.hid ORDER BY pim.aTime";
                            ResultSet test = statement.executeQuery(selectSQL);
                            if (! test.next()) {
                                test.close();
                                System.out.println("There's no records of appointments for you on this date!");
                            }
                            else {
                                int count=0;
                                String Y = "SELECT * FROM MidwifeAssignment";
                                ResultSet Assign = statement.executeQuery(Y);
                                List<String> a=new ArrayList<String>();
                                while (Assign.next()) {
                                    a.add(Assign.getString("practitioner_id")+Assign.getString("coupleid"));
                                } //create a list of primary midwifes

                                ResultSet rs1 = statement.executeQuery(selectSQL);
                                List<List<String>> infoList=new ArrayList<List<String>>();
                                while (rs1.next()) {
                                    count++;
                                    List<String> temp = new ArrayList<String>();
                                    temp.add(rs1.getString("coupleid"));
                                    temp.add(rs1.getString("appointID"));
                                    temp.add(rs1.getString("name")+" "+rs1.getString("hid"));
                                    temp.add(rs1.getString("aDate"));
                                    temp.add(rs1.getString("aTime"));
                                    infoList.add(temp);
                                    // so basically our infoList: first element is coupleID and the second is appointID and the third is mom's info, fourth date, fifth time
                                    if (a.contains(rs1.getString("practitioner_id")+rs1.getString("coupleid"))) {
                                        System.out.println(String.valueOf(count)+":  "+rs1.getString("aTime")+" P "+rs1.getString("name")+" "+rs1.getString("hid"));
                                    }
                                    else {
                                        System.out.println(String.valueOf(count)+":  "+rs1.getString("aTime")+" B "+rs1.getString("name")+" "+rs1.getString("hid"));
                                    }
                                }

                                while (true) {
                                    Scanner getNum= new Scanner(System.in);
                                    System.out.println("\nEnter the appointment number that you would like to work on.\n    [E] to exit [D] to go back to another date :");
                                    String appointNum = getNum.next();
                                    if (appointNum.equals("E")) {
                                        statement.close( );
                                        con.close( );
                                        System.exit(1);
                                    }
                                    else if (appointNum.equals("D")) {
                                        break;
                                    }
                                    else if (Integer.valueOf(appointNum)>0 && Integer.valueOf(appointNum)<=count) {
                                        List<String> info = infoList.get(Integer.valueOf(appointNum)-1);

                                        while (true) {
                                            Scanner getChoice=new Scanner(System.in);
                                            System.out.println("For "+info.get(2)+"\n\n1. Review notes\n2. Review tests\n3. Add a note\n4. Prescribe a test\n5. Go back to the appointments.\n\nEnter your choice:");
                                            String choice = getChoice.next();
                                            String coupleID = info.get(0);
                                            String appointID = info.get(1);
                                            String appointDate = info.get(3);
                                            String appointTime = info.get(4);
                                            if (choice.equals("1")) {
                                                String selectNote = "SELECT nDate, nTime, info\n" +
                                                        "FROM Note\n" +
                                                        "WHERE appointID IN\n" +
                                                        "(\n" +
                                                        "    SELECT appointID\n" +
                                                        "    FROM Appointment\n" +
                                                        "    WHERE coupleid=" + coupleID + "\n)\n" +
                                                        "ORDER BY nDate DESC, nTime DESC";

                                                ResultSet getAllnotes = statement.executeQuery(selectNote);
                                                while (getAllnotes.next()) {
                                                    // limit note info within 50 characters
                                                    String limit_char = getAllnotes.getString("info").substring(0, Math.min(getAllnotes.getString("info").length(), 50));
                                                    System.out.println(getAllnotes.getString("nDate") + " " + getAllnotes.getString("nTime") + " " + limit_char);
                                                }
                                            }

                                            else if (choice.equals("2")) {
                                                String selectTest = "SELECT test_date, type, result\n" +
                                                        "FROM MedicalTest\n" +
                                                        "WHERE appointID IN\n" +
                                                        "(\n" +
                                                        "    SELECT appointID\n" +
                                                        "    FROM Appointment\n" +
                                                        "    WHERE coupleid=" + coupleID + "\n)\n" +
                                                        "ORDER BY test_date DESC";

                                                ResultSet getAlltest = statement.executeQuery(selectTest);
                                                while (getAlltest.next()) {
                                                    String limit_char = "PENDING";
                                                    if (! Objects.isNull(getAlltest.getString("result"))){
                                                        // limit test result within 50 characters
                                                        limit_char = getAlltest.getString("result").substring(0, Math.min(getAlltest.getString("result").length(), 50));
                                                    }
                                                    System.out.println(getAlltest.getString("test_date") + " [" + getAlltest.getString("type") + "] " + limit_char);
                                                }
                                            }

                                            else if (choice.equals("3")) {
                                                Scanner getNewNote=new Scanner(System.in);
                                                System.out.println("Please type your observation:");
                                                String newNote = getNewNote.nextLine();
                                                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                                                LocalDateTime now_date = LocalDateTime.now();
                                                DateTimeFormatter ttf = DateTimeFormatter.ofPattern("HH:mm:ss");
                                                LocalDateTime now_time = LocalDateTime.now();
                                                String addNote = "INSERT INTO Note VALUES (" + appointID + ", '" + dtf.format(now_date) + "', '" + ttf.format(now_time) + "', '" + newNote + "') ";
                                                System.out.println (addNote);

                                                statement.executeUpdate ( addNote ) ;
                                                System.out.println ( "The note has been successfully added!" ) ;
                                            }

                                            else if (choice.equals("4")) {
                                                Scanner getNewTest=new Scanner(System.in);
                                                System.out.println("Please enter the type of test:");
                                                String newTest = getNewTest.nextLine();
                                                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                                                LocalDateTime now_date = LocalDateTime.now();
                                                String addTest = "INSERT INTO MedicalTest (appointID, type, test_date, sample_date)  VALUES ( " + appointID + ", '" + newTest + "', '" + dtf.format(now_date) + "', '" + dtf.format(now_date) + "') ";
                                                System.out.println (addTest);

                                                statement.executeUpdate ( addTest ) ;
                                                System.out.println ( "The test has been successfully added!" ) ;
                                            }

                                            else if (choice.equals("5")) {
                                                break;
                                            }

                                            else {
                                                System.err.println("Wrong input! Please enter again");
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }




        // Creating a table
        try
        {
          String createSQL = "CREATE TABLE " + tableName + " (id INTEGER, name VARCHAR (25)) ";
          System.out.println (createSQL ) ;
          statement.executeUpdate (createSQL ) ;
          System.out.println ("DONE");
        }
        catch (SQLException e)
        {
          sqlCode = e.getErrorCode(); // Get SQLCODE
          sqlState = e.getSQLState(); // Get SQLSTATE
                
          // Your code to handle errors comes here;
          // something more meaningful than a print would be good
          System.out.println("Code: " + sqlCode + "  sqlState: " + sqlState);
          System.out.println(e);
         }

        // Inserting Data into the table
        try
        {
          String insertSQL = "INSERT INTO " + tableName + " VALUES ( 1 , \'Vicki\' ) " ;
          System.out.println ( insertSQL ) ;
          statement.executeUpdate ( insertSQL ) ;
          System.out.println ( "DONE" ) ;

          insertSQL = "INSERT INTO " + tableName + " VALUES ( 2 , \'Vera\' ) " ;
          System.out.println ( insertSQL ) ;
          statement.executeUpdate ( insertSQL ) ;
          System.out.println ( "DONE" ) ;
          insertSQL = "INSERT INTO " + tableName + " VALUES ( 3 , \'Franca\' ) " ;
          System.out.println ( insertSQL ) ;
          statement.executeUpdate ( insertSQL ) ;
          System.out.println ( "DONE" ) ;

        }
        catch (SQLException e)
        {
          sqlCode = e.getErrorCode(); // Get SQLCODE
          sqlState = e.getSQLState(); // Get SQLSTATE
                
          // Your code to handle errors comes here;
          // something more meaningful than a print would be good
          System.out.println("Code: " + sqlCode + "  sqlState: " + sqlState);
          System.out.println(e);
        }

        // Querying a table
        try
        {
          String querySQL = "SELECT id, name from " + tableName + " WHERE NAME = \'Vicki\'";
          System.out.println (querySQL) ;
          java.sql.ResultSet rs = statement.executeQuery ( querySQL ) ;

          while ( rs.next ( ) )
          {
            int id = rs.getInt ( 1 ) ;
            String name = rs.getString (2);
            System.out.println ("id:  " + id);
            System.out.println ("name:  " + name);
          }
         System.out.println ("DONE");
        }
        catch (SQLException e)
        {
          sqlCode = e.getErrorCode(); // Get SQLCODE
          sqlState = e.getSQLState(); // Get SQLSTATE
                
          // Your code to handle errors comes here;
          // something more meaningful than a print would be good
          System.out.println("Code: " + sqlCode + "  sqlState: " + sqlState);
          System.out.println(e);
        }

      //Updating a table
      try
      {
        String updateSQL = "UPDATE " + tableName + " SET NAME = \'Mimi\' WHERE id = 3";
        System.out.println(updateSQL);
        statement.executeUpdate(updateSQL);
        System.out.println("DONE");

        // Dropping a table
        String dropSQL = "DROP TABLE " + tableName;
        System.out.println ( dropSQL ) ;
        statement.executeUpdate ( dropSQL ) ;
        System.out.println ("DONE");
      }
      catch (SQLException e)
      {
        sqlCode = e.getErrorCode(); // Get SQLCODE
        sqlState = e.getSQLState(); // Get SQLSTATE
                
        // Your code to handle errors comes here;
        // something more meaningful than a print would be good
        System.out.println("Code: " + sqlCode + "  sqlState: " + sqlState);
        System.out.println(e);
      }

      // Finally but importantly close the statement and connection
      statement.close ( ) ;
      con.close ( ) ;
    }
}
