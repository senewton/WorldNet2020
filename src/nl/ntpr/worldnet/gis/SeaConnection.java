package nl.ntpr.worldnet.gis;

/* Data comes in via MID MIF File
   ID integer, O_PortSeq integer, O_PortID char(10), O_PortName char(50),
   D_PortSeq integer, D_PortID char(10), D_PortName char(50),
   Vessel char(10), VolumePA float, DistKM float
   33409,0,"AE_","Dubai",12,"BE_0BEANR","Antwerpen","ODC_CNT","206584.5","12205.18"
*/

import nl.nea.neac.worldnet.network.Node;

/** This is a simple record structure for the port to port connections loaded from the sea network **/
public class SeaConnection {

    int origPortSeq;
    String origPortID;
    String origPortName;

    int destPortSeq;
    String destPortID;
    String destPortName;

    String vessel;

    double volumePA;
    double distKM;

    Node origWorldNetNode;
    Node destWorldNetNode;

    /** Constructor **/
    public SeaConnection(PortNode opn, PortNode dpn, String vess, double vol, double dkm) {
        this.origPortSeq = opn.portSeq;
        this.origPortID = opn.portID;
        this.origPortName = opn.portName;
        this.destPortSeq = dpn.portSeq;
        this.destPortID = dpn.portID;
        this.destPortName = dpn.portName;
        this.vessel = vess;
        this.volumePA = vol;
        this.distKM = dkm;

        this.origWorldNetNode = new Node(opn.xCoord, opn.yCoord, opn.portSeq, opn.portName);
        this.destWorldNetNode = new Node(dpn.xCoord, dpn.yCoord, dpn.portSeq, dpn.portName);
    }

    public void seeSeaConnection(){
        System.out.printf("\nOrig:%d %s %s", this.origPortSeq, this.origPortID, this.origPortName);
        System.out.printf("\nDest:%d %s %s", this.destPortSeq, this.destPortID, this.destPortName);
        System.out.printf("\nKms:%f", this.distKM);
    }
}
