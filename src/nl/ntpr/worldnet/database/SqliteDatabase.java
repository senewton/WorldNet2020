package nl.ntpr.worldnet.database;

import java.sql.*;

/** This is a base class for accessing data on SQLite **/
public class SqliteDatabase {

    /** Connection to sqlite database **/
    public Connection conn ;

    /** Field showing that connection is active **/
    public boolean activeConnection ;

    /** Constructor **/
    public SqliteDatabase( String dbName ){
        System.out.println("## SqliteDatabase Initialised: " + dbName );
        this.activeConnection = false ;
        try {
            Class.forName("org.sqlite.JDBC");
            String dbURL = "jdbc:sqlite:c:/sqlite/db/"+ dbName ;
            this.conn = DriverManager.getConnection(dbURL);
            if (this.conn != null) {
                this.activeConnection = true ;
                System.out.println("Connected to the database:" + dbURL );
                this.seeDbMetadata();
            }
        } catch (ClassNotFoundException | SQLException ex) {
            ex.printStackTrace();
        }
    }

    /** Close Database **/
    public void closeDatabase(){
        try {
            if (this.conn != null) {
                this.conn.close();
                this.activeConnection = false ;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    /** Method for displaying version of sqlite jdbc driver **/
    void seeDbMetadata(){
        try {
            DatabaseMetaData dm = this.conn.getMetaData();
            System.out.println("Driver name: " + dm.getDriverName());
            System.out.println("Driver version: " + dm.getDriverVersion());
            System.out.println("Product name: " + dm.getDatabaseProductName());
            System.out.println("Product version: " + dm.getDatabaseProductVersion());
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    /** Run a select Query **/
    public void runSelectQuery( String tabName ){
        try {
            Statement statement = this.conn.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.
            ResultSet rs = statement.executeQuery("select * from [" + tabName + "]" ) ;

            while(rs.next())
            {
                // read the result set
                System.out.printf("\nREC:%s %s", rs.getString(1), rs.getString(2) );
            }
        } catch ( SQLException ex) {
            ex.printStackTrace();
        }
    }
}
