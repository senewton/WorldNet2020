package nl.ntpr.worldnet.gis;

import nl.nea.neac.worldnet.network.Coord;
import nl.panteia.utils.exceptions.FileParseException;
import nl.panteia.utils.io.csv.CSVReader;
import nl.panteia.utils.io.mif.MapInfoInterchangeReader;
import nl.panteia.utils.io.mif.primitives.MifPoint;

import java.io.IOException;
import java.util.ArrayList;

public class LoadingPoints {
    /** Path to MID/MIF loading point data **/
    String lpFileName ;

    /** ArrayList holding all the loading points **/
    ArrayList<LoadingNode> arrLoadingNodes = new ArrayList<>();

    /** Constructor with filename **/
    public LoadingPoints(String loadingPointFileName ){
        System.out.println("## Loadings Points Initialised from:" + loadingPointFileName);
        this.lpFileName = loadingPointFileName ;

        // Clear the main array holding the loading points
        this.arrLoadingNodes.clear();
    }

    /** Read data in from MID MIF GIS file **/
    public void openMidMifFile(){
        int nodeSequenceCode = 5000; // Loading points are numbered from 5000 to 9999
        final int NUM_FLDS = 4 ;

        /*
          MID/MIF structure is like this
          ID integer, CNAME char(254), NNAME char(254), CCODE integer
          1,"FRANCE","PARIS",1
        */

        try{
            // Set up the MIF File Reader
            MapInfoInterchangeReader mifReader = new MapInfoInterchangeReader(this.lpFileName ) ;
            // This will read the full list of points
            MifPoint result ;
            while ( (result = (MifPoint) mifReader.readMifObject() ) != null ) {
                // AbstractMifMidObject result = mifReader.readMifObject(); // This returns a generic object
                // MifPline result = (MifPline) mifReader.readMifObject(); // This returns a particular type of geometry object
                String[] attrFields = result.getMidData();
                if(attrFields.length == NUM_FLDS){
                    // Translate attribute data
                    String cname = attrFields[1];
                    String nname = attrFields[2];
                    int ccode = CSVReader.convertToInt(attrFields[3]);

                    String[] geoFields = result.getMifData();
                    // Each string here is a pair of coordinates, data is a point so array should only be length 1
                    if(geoFields.length == 1){
                        // Parse the coordinates
                        String[] locations = geoFields[0].split(" ");
                        Coord c = new Coord(CSVReader.convertToDouble(locations[0]),
                                CSVReader.convertToDouble(locations[1]));
                        LoadingNode loadNode = new LoadingNode(nodeSequenceCode, nname, cname, ccode,c.getLongitude(), c.getLatitude());
                        this.arrLoadingNodes.add(loadNode);
                        nodeSequenceCode++;
                    } else{
                        System.out.println("Error reading loading points: MIF");
                    }
                } else {
                    System.out.println("Error reading loading points: MID");
                }
            }
        } catch( IOException | FileParseException ex ){
            ex.printStackTrace();
        }
        System.out.println("Finished reading loading points: Found: " + this.arrLoadingNodes.size() + " points.");
    }
}
