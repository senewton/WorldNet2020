package nl.ntpr.worldnet;

import nl.nea.neac.worldnet.network.Node;
import nl.ntpr.worldnet.database.SqliteDatabase;
import nl.ntpr.worldnet.database.TradeSqliteDatabase;
import nl.ntpr.worldnet.gis.MultiModalNetwork;
import nl.panteia.utils.gis.GISUtil;

public class Main {

    public static void main(String[] args) {
        System.out.println("## WorldNet2020: European Model of Worldwide Container Flows");

        // ==== Test the library utilities
        // testUtils() ;

        // ==== Test you can open a GIS file
        // testGISFile() ;

        // ==== Section for opening database and retrieving trade data
        //   accessTradeDatabase();
        ContainerFlowModel contFM = new ContainerFlowModel() ;
    }

    /** Function for retrieving trade data into TradeSqliteDatabase class **/
    private static void accessTradeDatabase(){
        String dbname = "trade_comext_transport.db" ;
        TradeSqliteDatabase tradeSqlDb = new TradeSqliteDatabase( dbname ) ;

        if (tradeSqlDb.activeConnection == true ) {
            final int ORIG = 720 ; // 720=China
            final int DEST =   3 ; // 3=Netherlands
            int numRecords = 0 ;
            numRecords = tradeSqlDb.extractDataFromDB(ORIG, DEST, "tr201952od") ;
            System.out.println("\nRead " + numRecords + " Records");

            // tradeSqlDb.runSelectQuery( "meta-country-groups");
            tradeSqlDb.closeDatabase();
        }
    }

    /** Test function for using GISUtils and the WorldNet Libraries **/
    private static void testUtils() {
        double rtmLat  = 51.924 ;
        double rtmLong =  4.477 ;

        double nwcLat  = 54.978 ;
        double nwcLong = -1.618 ;

        double distm = GISUtil.getDistanceBetween( rtmLong, rtmLat, nwcLong, nwcLat ) ;

        System.out.printf("\nDistance (m) = %.4f", distm );

        System.out.println("\n## Building WorldNet Node" );
        Node n = new Node( 375 ) ;
        System.out.println( n.toString() );
    }

}
