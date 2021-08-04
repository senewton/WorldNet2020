package nl.ntpr.worldnet.gis;

/* Link data comes in via MID MIF File
   ID integer, SpeedKph integer, ISO char(2), WN3 integer, Active integer, Corridors char(10), CCODE integer
   62434,60,"IT",118,1,"",5
*/

import nl.nea.neac.worldnet.network.Coord;
import nl.panteia.utils.gis.GISUtil;

import java.util.ArrayList;

/** This is a simple record structure for the inland network links loaded from the network mid mif **/
public class InlandLink {

    /** These are the nodes outlining the link **/
    InlandNode NodeA; // Has a Worldnet Node structure inside
    InlandNode NodeB;

    /** Attributes of the link **/
    double SpeedKph;
    String CountryCodeIso; // ISO code for the country where the link is located
    int CountryCodeWn3; // Worldnet 3-Digit code for the country where the link is located
    int CountryCodeCCode; // Comext country code
    boolean IsActive; // Whether to treat the link as active
    String Corridors; // Corridor codes e.g. BGH

    double distKm;

    /** This holds the sequence of coordinates - making it possible to draw the link or calculate its length **/
    ArrayList<Coord> arrCoords = new ArrayList<>();

    /** Constructor **/
    public InlandLink(InlandNode ndA, InlandNode ndB, ArrayList<Coord> coords,
                      double speedKph, String iso, int wn3, boolean bActive, String corr, int ccode ) {

        this.NodeA = new InlandNode(ndA.nSeq, ndA.xCoord, ndA.yCoord);
        this.NodeB = new InlandNode(ndB.nSeq, ndB.xCoord, ndB.yCoord);

        // Push the incoming coordinates into the class array of coordinates
        for (Coord coord : coords) {
            Coord c = new Coord(coord.getLongitude(), coord.getLatitude());
            this.arrCoords.add(c);
        }

        this.SpeedKph = speedKph;
        this.CountryCodeIso = iso;
        this.CountryCodeWn3 = wn3;
        this.IsActive = bActive;
        this.Corridors = corr;
        this.CountryCodeCCode = ccode;

        // Set up impedance calculations
        this.distKm = calcLinkDistance();
    }

    /** Print out contents of link**/
    public void seeInlandLink(){
        System.out.printf("\nNodeA:%d %f %f", this.NodeA.nSeq, this.NodeA.xCoord, this.NodeA.yCoord);
        System.out.printf("\nNodeB:%d %f %f", this.NodeB.nSeq, this.NodeB.xCoord, this.NodeB.yCoord);
        System.out.printf("\nKms:%f", this.distKm);
    }

    /** Method for calculating length of polyline **/
    private double calcLinkDistance(){
        double tempMetres = 0.0;
        int arrSize = this.arrCoords.size();
        for(int i = 0; i < arrSize; i++){
            if (i < (arrSize - 1)){
                tempMetres += GISUtil.getDistanceBetween(
                        this.arrCoords.get(i).getLongitude(),
                        this.arrCoords.get(i).getLatitude(),
                        this.arrCoords.get(i+1).getLongitude(),
                        this.arrCoords.get(i+1).getLatitude());
            }
        }
        return tempMetres/1000.0;
    }
}











