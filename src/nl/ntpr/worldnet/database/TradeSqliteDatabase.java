package nl.ntpr.worldnet.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;


/** TradeSqliteDatabase Class
 * Derived from all purpose class with functionality for connecting to SQLite
 * Includes an embedded class with the record structure
 * This class is able to extract the data from SQlite and store it in an ArrayList
 **/
public class TradeSqliteDatabase extends SqliteDatabase {

    /** Class for holding trade data which will extracted from database **/
    public class TradeDataRecord{
        int orig ;
        int dest ;
        int nstr ;
        double tTonnes ;
        double cTonnes ;
        double kEuro ;

        /** set fields by copying from another TradeDataRecord **/
        public void setValues( TradeDataRecord tdr ){
            this.orig = tdr.orig ;
            this.dest = tdr.dest ;
            this.nstr = tdr.nstr ;
            this.tTonnes = tdr.tTonnes ;
            this.cTonnes = tdr.cTonnes ;
            this.kEuro   = tdr.kEuro ;
        }

        /** set fields by copying from raw values **/
        public void setValues( int origin, int destination, int nstCommodity, double totalTonnes, double containerTonnes, double kEuroValue ){
            this.orig = origin ;
            this.dest = destination ;
            this.nstr = nstCommodity ;
            this.tTonnes = totalTonnes ;
            this.cTonnes = containerTonnes ;
            this.kEuro   = kEuroValue ;
        }

        public void seeRecord(){
            System.out.printf("\n%d %d %d %f %f %f", this.orig, this.dest, this.nstr, this.tTonnes, this.cTonnes, this.kEuro);
        }
    }

    /** Main data structure containing trade data records */
    ArrayList<TradeDataRecord> arrTradeData ;


    /** Constructor for TradeSqliteDatabase class **/
    public TradeSqliteDatabase( String dbName ){
        // Calls the base class to connect to the database
        super( dbName ) ;

        // Some temporary code for testing ways to build the data structure
        // testDataStructure();
    }


    /** Run a query and extract the data you want from the SQLite database table into the Arraylist **/
    public int extractDataFromDB( int filterOrig, int filterDest, String tabName ){

        this.arrTradeData = new ArrayList<>() ;

        try {
            Statement statement = this.conn.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.

            String queryStr = String.format("select origin, destination, prod_nstr," +
                    "sum(Quan_kg/1000.0) as sumTotalTonnes, " +
                    "sum(Cont_kg/1000.0) as sumContTonnes, " +
                    "sum(Val_eur/1000.0) as sumKEuro " +
                    "from %s " +
                    "where origin = %s and destination = %s " +
                    "group by origin, destination, prod_nstr " +
                    "order by origin, destination, prod_nstr", tabName, filterOrig, filterDest ) ;

            ResultSet rs = statement.executeQuery( queryStr ) ;

            while( rs.next() )
            {
                TradeDataRecord odRec = new TradeDataRecord() ;
                odRec.setValues( rs.getInt("origin"), rs.getInt("destination"), rs.getInt("prod_nstr"),
                                 rs.getDouble("sumTotalTonnes"), rs.getDouble("sumContTonnes"), rs.getDouble("sumKEuro") );
                // odRec.seeRecord();
                this.arrTradeData.add(odRec);
            }
        } catch ( SQLException ex) {
            ex.printStackTrace();
        }
        return this.arrTradeData.size() ;
    }


    /** Temporary method for testing out use of trade data records **/
    private void testDataStructure(){
        TradeDataRecord odRec = new TradeDataRecord() ;
        odRec.setValues( 100, 200, 156, 2457.6, 56789.4, 1024.7 );

        this.arrTradeData = new ArrayList<>() ;
        for (int i = 0; i < 10 ; i++) {
            odRec.orig += 10 ;
            TradeDataRecord odl = new TradeDataRecord() ;
            odl.setValues( odRec );
            this.arrTradeData.add( odl ) ;
        }

        for ( TradeDataRecord t : this.arrTradeData) {
            t.seeRecord();
        }
    }

}
