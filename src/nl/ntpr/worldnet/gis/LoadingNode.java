package nl.ntpr.worldnet.gis;

import nl.nea.neac.worldnet.network.Node;

/*
  MID/MIF structure is like this
  ID integer, CNAME char(254), NNAME char(254), CCODE integer
  1,"FRANCE","PARIS",1
 */

/** Class for holding loading point nodes **/
public class LoadingNode {
    final int MIN_LSEQ = 5000;
    final int MAX_LSEQ = 9999;

    int lnSeq ; // loading node sequence number: generated internally
    int ccode ; // COMEXT country code

    String lnName ; // loading node name e.g. city name
    String lnCName ; // loading node country code

    double xCoord; // X Coord = Longitude
    double yCoord; // Y Coord = Latitude
    Node worldNetNode; // Worldnet style node: needed for building multimodal network

    /** Constructor for Loading Node **/
    public LoadingNode(int lseq, String lName, String lCName, int ccd, double x, double y) {
        if( lseq < MIN_LSEQ || lseq > MAX_LSEQ ){
            System.out.println("Loading Node Error: LSEQ out of bounds:" + lseq );
        }
        this.lnSeq = lseq ;
        this.ccode = ccd ;
        this.lnName = lName ;
        this.lnCName = lCName ;
        this.xCoord = x ;
        this.yCoord = y ;
        this.worldNetNode = new Node( x, y, lseq, lName ) ;
    }

    /** Print out contents **/
    public void seeLoadingNode(){
        System.out.printf("\n%d %d %s %s %f %f",
                this.lnSeq, this.ccode, this.lnName, this.lnCName, this.xCoord, this.yCoord);
    }

    /** Get Worldnet Style Node **/
    public Node getWorldNetNode(){
        return this.worldNetNode ;
    }
}








