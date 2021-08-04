package nl.ntpr.worldnet.gis;

import nl.nea.neac.worldnet.network.Node;

/** Class for holding inland network (anonymous) nodes **/
public class InlandNode {
    final int MIN_NSEQ = 10000;
    final int MAX_NSEQ = 19999;

    int nSeq ; // inland node sequence number: generated internally
    double xCoord; // X Coord = Longitude
    double yCoord; // Y Coord = Latitude
    Node worldNetNode;

    /** Constructor for Inland Node **/
    public InlandNode(int nseq, double x, double y) {
        if( nseq < MIN_NSEQ || nseq > MAX_NSEQ ){
            System.out.println("Inland Node Error: NSEQ out of bounds:" + nseq );
        }
        this.nSeq = nseq ;
        this.xCoord = x ;
        this.yCoord = y ;
        this.worldNetNode = new Node( x, y, nseq, "Anonymous" ) ;
    }

    /** Print out contents **/
    public void seeInlandNode(){
        System.out.printf("\n%d %f %f",this.nSeq, this.xCoord, this.yCoord);
    }

    /** Get Worldnet Style Node **/
    public Node getWorldNetNode(){
        return this.worldNetNode ;
    }
}
