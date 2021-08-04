package nl.ntpr.worldnet.gis;
import nl.nea.neac.worldnet.network.Node;

/** This is a simple record structure for ports **/
public class PortNode {

    final int MIN_PSEQ = 1000;
    final int MAX_PSEQ = 4999;

    int portSeq; // Internally generated sequence number
    String portID; // ID code based on Eurostat e.g. NL_9NLIJM or MY_
    String portNUTS3; // NUTS3 code
    String portName; // Name
    String portType; // Type: should be maritime
    int basgoedRegion; // Basgoed region (I know..)
    double xCoord; // X Coordinate = Longitude
    double yCoord; // Y Coordinate = Latitude
    Node worldNetNode; // Worldnet style node: needed for building multimodal network

    // PORTID;NUTS3;PORTNM;PORT_TYPE;BGREG;X;Y
    // AE_;AE999;Dubai;MARITIME;353;55.27911377;25.26531982

    /** Constructor **/
    public PortNode(int pseq, String pid, String pn3, String pnm, String pty, int bgreg, double x, double y){
        if( pseq < MIN_PSEQ || pseq > MAX_PSEQ ){
            System.out.println("PortNode Error: PSEQ out of bounds:" + pseq );
        }
        this.portSeq = pseq ;
        this.portID = pid ;
        this.portNUTS3 = pn3 ;
        this.portName = pnm ;
        this.portType = pty ;
        this.basgoedRegion = bgreg ;
        this.xCoord = x ;
        this.yCoord = y ;
        this.worldNetNode = new Node( x, y, pseq, pnm ) ;
    }

    /** Print out contents **/
    public void seePort(){
        System.out.printf("\n%d %s %s %s %s %d %f %f",
                this.portSeq, this.portID, this.portNUTS3, this.portName, this.portType,
                this.basgoedRegion, this.xCoord, this.yCoord);
    }

    /** Get Worldnet Style Node **/
    public Node getWorldNetNode(){
        return this.worldNetNode ;
    }
}
