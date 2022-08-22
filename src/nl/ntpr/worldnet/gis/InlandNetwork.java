package nl.ntpr.worldnet.gis;

import nl.nea.neac.worldnet.network.Coord;
import nl.panteia.utils.exceptions.FileParseException;
import nl.panteia.utils.gis.GISUtil;
import nl.panteia.utils.io.csv.CSVReader;
import nl.panteia.utils.io.mif.MapInfoInterchangeReader;
import nl.panteia.utils.io.mif.primitives.MifPline;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;


public class InlandNetwork {
    /** Path+Filename for MID/MIF inland network data **/
    String inetFileName;


    /** ArrayList for holding full set of inland links **/
    ArrayList<InlandLink> arrInlandLinks = new ArrayList<>();


    /** Hashmap for holding master list of inland nodes, using string based on coordinates **/
    HashMap<String,InlandNode> hmInlandNodes = new HashMap<>();


    /** Constructor with filenames**/
    public InlandNetwork(String inlandNetFileName ){
        System.out.println("## Inland Network Initialised from:" + inlandNetFileName);
        this.inetFileName = inlandNetFileName ;
    }


    /** This opens and read the Polylines from the MID MIF files containing the inland network **/
    public void openInlandNetworkMidMifFile(){
        final int NUM_FLDS = 7 ;
        try{
            // Set up the MIF File Reader
            MapInfoInterchangeReader mifReader = new MapInfoInterchangeReader(this.inetFileName ) ;

            /* Inland Link data comes in via MID MIF File
               ID integer, SpeedKph integer, ISO char(2), WN3 integer, Active integer, Corridors char(10), CCODE integer
               62434,60,"IT",118,1,"",5
            */

            // This will read the full list of PLines
            MifPline result ;
            while ( (result = (MifPline) mifReader.readMifObject() ) != null ) {
                // AbstractMifMidObject result = mifReader.readMifObject(); // This returns a generic object
                // MifPline result = (MifPline) mifReader.readMifObject(); // This returns a particular type of geometry object

                // handle the attribute data first
                String[] attrFields = result.getMidData();
                double spd = 0.0 ;
                String iso = null;
                int wn3=0;
                int iAct;
                boolean bAct = false;
                String corr = null;
                int ccode = 0;
                if(attrFields.length == NUM_FLDS){
                    spd = CSVReader.convertToDouble(attrFields[1]);
                    iso = attrFields[2];
                    wn3 = CSVReader.convertToInt(attrFields[3]);
                    iAct = CSVReader.convertToInt(attrFields[4]);
                    if(iAct == 0 ){
                        bAct = false ;
                    } else {
                        bAct = true ;
                    }
                    corr = attrFields[5];
                    ccode = CSVReader.convertToInt(attrFields[6]);
                    // System.out.println( "Result type =" + result.getType() );
                    // System.out.println( "MID data =" + Arrays.toString( result.getMidData() ) );
                    // System.out.println( "MIF data =" + Arrays.toString( result.getMifData() ) );
                } else{
                    System.out.println("OpenInlandNetworkMidMifFile: Error reading MID/ATTR record:" + Arrays.toString(attrFields));
                }

                String[] geoData = result.getMifData(); // This is all the coordinate data as raw strings

                // Need the first and last node, plus the full stack of coordinates
                ArrayList<Coord> coords = new ArrayList<>();
                String keyStrNodeA = null;
                String keyStrNodeB = null;

                // Run through all the vertices
                for(int i=0; i< geoData.length; i++){
                    String[] locations = geoData[i].split(" ");
                    Coord c = new Coord(
                            CSVReader.convertToDouble(locations[0]),
                            CSVReader.convertToDouble(locations[1]));
                    coords.add(c);

                    // First Node
                    if(i == 0){
                        double tempx = c.getLongitude();
                        double tempy = c.getLatitude();
                        keyStrNodeA = getNodeKey(tempx, tempy);
                        storeNode(keyStrNodeA, tempx, tempy);
                    }

                    // Last Node
                    if(i == geoData.length-1){
                        double tempx = c.getLongitude();
                        double tempy = c.getLatitude();
                        keyStrNodeB = getNodeKey(tempx, tempy);
                        storeNode(keyStrNodeB, tempx, tempy);
                    }
                }
                // Build the link
                if(keyStrNodeA != null ) {
                    InlandNode ndA = this.hmInlandNodes.get(keyStrNodeA);

                    if(keyStrNodeB != null ) {
                        InlandNode ndB = this.hmInlandNodes.get(keyStrNodeB);

                        InlandLink il = new InlandLink(ndA, ndB, coords, spd, iso, wn3, bAct, corr, ccode);
                        this.arrInlandLinks.add(il);
                    }
                }
            }
        } catch( IOException | FileParseException ex ){
            ex.printStackTrace();
        }
        System.out.println("Finished reading inland network links: Found: " + this.arrInlandLinks.size() + " Links.");
        System.out.println(" and: " + this.hmInlandNodes.size() + " Nodes.");
    }


    /** Get a key for a node based on coordinates **/
    private String getNodeKey(double xCoord, double yCoord){
        int ix = (int)(xCoord*100000);
        int iy = (int)(yCoord*100000);
        return String.format("%d:%d", ix, iy);
    }


    /** Create and Store node if new in Hashmap **/
    private void storeNode(String keyStr, double xCoord, double yCoord){
        if(this.hmInlandNodes.containsKey(keyStr)){
            // Do nothing - it already exists
        } else {
            int nodeSeq = 10000 + this.hmInlandNodes.size();
            InlandNode inNd = new InlandNode(nodeSeq, xCoord, yCoord);
            this.hmInlandNodes.put(keyStr, inNd);
        }
    }


    /** List out all the nodes found in the network **/
    public void listNodesInNetwork(){
        // Iterating over hash set items
        System.out.println("Iterating over list:");
        this.hmInlandNodes.forEach((k, v) -> System.out.println(v.nSeq + ";" + v.worldNetNode.getLongitude() + ";" + v.worldNetNode.getLatitude()));

        System.out.println("Found " + this.hmInlandNodes.size() + "Nodes in Network");
    }


    /** return a reference to the array list of inland transport links **/
    ArrayList<InlandLink> getListOfInlandLinks(){
        return this.arrInlandLinks;
    }

    /** Return Node closest to given coordinates **/
    InlandNode locateNearestNode(double xCoord, double yCoord){
        InlandNode nearestNode = new InlandNode();
        double minDist = Double.MAX_VALUE;
        Boolean bNodeFound = false;

        for (InlandNode inode : this.hmInlandNodes.values()){
            double metres = GISUtil.getDistanceBetween(xCoord, yCoord, inode.xCoord, inode.yCoord);
            if(metres < minDist){
                minDist = metres;
                nearestNode = new InlandNode(inode); // Make a copy
                bNodeFound = true;
            }
        }
        if(bNodeFound == false){
            System.out.println("Error: could not find an inland Node");
            System.exit(0);
        }
        if((minDist/1000.0)>100.0){
            System.out.println("Warning: Distance connecting sea to land network is high (km): " + (minDist/1000.0));
        }

        return nearestNode;
    }
}
