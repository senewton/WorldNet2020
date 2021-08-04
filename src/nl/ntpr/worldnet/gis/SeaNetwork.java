package nl.ntpr.worldnet.gis;

import nl.panteia.utils.exceptions.FileParseException;
import nl.panteia.utils.io.csv.CSVReader;
import nl.panteia.utils.io.mif.MapInfoInterchangeReader;
import nl.panteia.utils.io.mif.primitives.MifPline;

import java.io.IOException;
import java.util.*;

public class SeaNetwork {
    /** Path+Filename for MID/MIF sea network data **/
    String snetFileName;
    /** Path+Filename for CSV data listing ports **/
    String portsFileName;

    /** Hashset for holding port codes that appear in the network **/
    HashSet<String> hsPortsInSeaNetwork = new HashSet<>();

    /** Hashmap for holding master list of ports, using Eurostat style portid as key e.g. UK_1GBMNC or EG_ **/
    HashMap<String,PortNode> hmPortIDLookup = new HashMap<>();

    /** Hashmap for holding master list of ports, using internal port sequence as key e.g. 1001 **/
    HashMap<Integer,PortNode> hmPortSeqLookup = new HashMap<>();

    /** ArrayList for holding full set of sea connections **/
    ArrayList<SeaConnection> arrSeaConnections = new ArrayList<>();

    /** Constructor with filenames**/
    public SeaNetwork(String seaNetFileName, String portNodesFileName ){
        System.out.println("## Sea Network Initialised from:" + seaNetFileName);
        System.out.println("## Port Data Initialised from:" + portNodesFileName);
        this.snetFileName = seaNetFileName ;
        this.portsFileName = portNodesFileName ;

        this.hsPortsInSeaNetwork.clear();
        this.hmPortIDLookup.clear();
        this.hmPortSeqLookup.clear();
        this.arrSeaConnections.clear();
    }

    /** This opens and read the Polylines from the MID MIF files containing the maritime connections **/
    public void openSeaNetworkMidMifFile(){
        final int NUM_FLDS = 10 ;
        try{
            // Set up the MIF File Reader
            MapInfoInterchangeReader mifReader = new MapInfoInterchangeReader(this.snetFileName ) ;

            /* ID integer, O_PortSeq integer, O_PortID char(10), O_PortName char(50),
               D_PortSeq integer, D_PortID char(10), D_PortName char(50),
               Vessel char(10), VolumePA float, DistKM float
               33409,0,"AE_","Dubai",12,"BE_0BEANR","Antwerpen","ODC_CNT","206584.5","12205.18"
            */

            // This will read the full list of PLines
            MifPline result ;
            while ( (result = (MifPline) mifReader.readMifObject() ) != null ) {
                // AbstractMifMidObject result = mifReader.readMifObject(); // This returns a generic object
                // MifPline result = (MifPline) mifReader.readMifObject(); // This returns a particular type of geometry object
                String[] fields = result.getMidData();
                if(fields.length == NUM_FLDS){
                    // Convert the connection into two port nodes and make a sea connection object
                    // There is no need to use the coordinate data because it is already stored in the port nodes
                    String origPortID = fields[2] ;
                    String destPortID = fields[5] ;
                    String vessel = fields[7];
                    double volPA = CSVReader.convertToDouble(fields[8]);
                    double distKm = CSVReader.convertToDouble(fields[9]);
                    this.hsPortsInSeaNetwork.add(origPortID);
                    this.hsPortsInSeaNetwork.add(destPortID);
                    PortNode opn = this.getPortNodeFromID(origPortID);
                    PortNode dpn = this.getPortNodeFromID(destPortID);
                    SeaConnection scon = new SeaConnection(opn, dpn, vessel, volPA, distKm );
                    this.arrSeaConnections.add(scon);

                    // System.out.println( "Result type =" + result.getType() );
                    // System.out.println( "MID data =" + Arrays.toString( result.getMidData() ) );
                    // System.out.println( "MIF data =" + Arrays.toString( result.getMifData() ) );
                } else{
                    System.out.println("OpenSeaNetworkMidMifFile: Error reading record:" + Arrays.toString(fields));
                }
            }

        } catch( IOException | FileParseException ex ){
            ex.printStackTrace();
        }
        System.out.println("Finished reading sea connections: Found: " + this.arrSeaConnections.size());
    }

    /** This opens the master list of ports **/
    public void openPortsCsvFile(){
        // PORTID;NUTS3;PORTNM;PORT_TYPE;BGREG;X;Y
        // AE_;AE999;Dubai;MARITIME;353;55.27911377;25.26531982
        final int NUM_FLDS = 7 ;
        int portSequenceCode = 1000; // Start a new numbering from 1000
        try {
            CSVReader csvReader = new CSVReader(this.portsFileName+".csv",";","\"");
            //skip first line:
            String line = csvReader.getNextLine();
            try {
                String[] fields ;
                while (( fields = csvReader.splitNextLine()) != null) {
                    // System.out.println("Row: " + fields[0] + "--" + fields[1]);
                    if(fields.length == NUM_FLDS) {
                        // System.out.println(fields[0]);
                        PortNode portNode = new PortNode(
                                portSequenceCode, fields[0], fields[1], fields[2], fields[3],
                                CSVReader.convertToInt(fields[4]),
                                CSVReader.convertToDouble(fields[5]),
                                CSVReader.convertToDouble(fields[6]));
                        this.hmPortIDLookup.put(fields[0], portNode);
                        this.hmPortSeqLookup.put(portSequenceCode, portNode);
                        portSequenceCode++;
                    } else{
                        System.out.println("OpenPortsCSVFile: Error reading record:" + Arrays.toString(fields));
                    }
                }
            } finally {
                csvReader.close();
            }
        } catch( IOException ex ){
            ex.printStackTrace();
        }
    }

    /** List out all the ports found in the network **/
    public void listPortsInNetwork(){
        // Iterating over hash set items
        System.out.println("Iterating over list:");
        Iterator<String> hsit = this.hsPortsInSeaNetwork.iterator();
        int i = 0;
        while (hsit.hasNext()) {
            String portid = hsit.next();
            PortNode pn = this.getPortNodeFromID(portid);
            System.out.println(pn.worldNetNode.getID() + ";" + portid + ";" +pn.portName+";"+pn.xCoord+";"+ pn.yCoord );
            i++;
        }
        System.out.println("Found " + i + "Ports in Network");
    }

    /** Use a portID string to get a complete data record for that port
     * A port ID is a string such as NL_9NLIJM or MY_**/
    public PortNode getPortNodeFromID(String portID){
        PortNode pn = null;
        if (this.hmPortIDLookup.containsKey(portID)) {
            pn = this.hmPortIDLookup.get(portID);
            return pn ;
        }
        System.out.println("Error: unable to find port node data for port ID:" + portID );
        return pn ;
    }

    /** Use an internally generated port sequence integer to get a complete data record for that port
     * A port ID is e.g. 1001 **/
    public PortNode getPortNodeFromSeq(int portSeq){
        PortNode pn = null;
        if (this.hmPortSeqLookup.containsKey(portSeq)) {
            pn = this.hmPortSeqLookup.get(portSeq);
            return pn ;
        }
        System.out.println("Error: unable to find port node data for sequence number:" + portSeq );
        return pn ;
    }

}
