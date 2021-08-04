package nl.ntpr.worldnet.gis;

import nl.nea.neac.worldnet.network.Path;
import nl.nea.neac.worldnet.network.Node;
import nl.nea.neac.worldnet.network.Link;
import nl.nea.neac.worldnet.network.Network;
import nl.nea.neac.worldnet.network.LinkModality ;
import nl.nea.neac.worldnet.network.algorithms.PathEnumerator ;
import nl.ntpr.worldnet.MultinomialLogit;

import java.io.IOException;
import java.util.Collections ;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class MultiModalNetwork {

    public MultiModalNetwork(){
        test();
    }

    private void test() {
        // === Step 1; Start with some nodes
        // Rotterdam
        double rtmLong = 4.48; // E
        double rtmLat = 51.92; // N
        int rtmId = 1;
        String rtmName = "RTM";

        // Shanghai
        double shgLong = 121.46; // E
        double shgLat = 31.22; // N
        int shgId = 2;
        String shgName = "SHG";

        // Port Said (Suez)
        double sueLong = 32.30; // E
        double sueLat = 31.26; // N
        int sueId = 3;
        String sueName = "PSD";

        // Cape Town
        double capLong = 18.42; // E
        double capLat = -33.92; // N
        int capID = 4;
        String capName = "CPT";

        Node rtmNode = new Node(rtmLong, rtmLat, rtmId, rtmName);
        Node shgNode = new Node(shgLong, shgLat, shgId, shgName);
        Node sueNode = new Node(sueLong, sueLat, sueId, sueName);
        Node capNode = new Node(capLong, capLat, capID, capName);

        // === Step 2; Connect the nodes to make links
        HashSet<Link> myNetworkLinks = new HashSet<Link>();

        // Set up a maritime link from Rotterdam to Port Said
        int idLnk1 = 1;
        double impLnk1 = 5850.0; // KM

        // Port Said to Shanghai
        int idLnk2 = 2;
        double impLnk2 = 13428.0; // KM

        // Rotterdam to Cape Town
        int idLnk3 = 3;
        double impLnk3 = 11413.0; // KM

        // Cape Town to Shanghai
        int idLnk4 = 4;
        double impLnk4 = 14258.0; // KM

        // And a railway link
        int idLnk5 = 5;
        double impLnk5 = 10947.0 + 1967.0;  // Land KM 10947 is Rotterdam to Chengdu, 1967 is Chengdu to Shanghai

        Link Link1 = new Link(idLnk1, impLnk1, rtmNode, sueNode, LinkModality.SEA);
        Link Link2 = new Link(idLnk2, impLnk2, sueNode, shgNode, LinkModality.SEA);
        Link Link3 = new Link(idLnk3, impLnk3, rtmNode, capNode, LinkModality.SEA);
        Link Link4 = new Link(idLnk4, impLnk4, capNode, shgNode, LinkModality.SEA);
        Link Link5 = new Link(idLnk5, impLnk5, rtmNode, shgNode, LinkModality.UNITISED_RAIL);

        // === Step 3: Make a set out of the links and create a network from it
        myNetworkLinks.add(Link1);
        myNetworkLinks.add(Link2);
        myNetworkLinks.add(Link3);
        myNetworkLinks.add(Link4);
        myNetworkLinks.add(Link5);

        Network myNetwork = new Network(myNetworkLinks);

        // === Step 4: Analyse the network paths available
        ArrayList<Path> pathList = new ArrayList<Path>();
        PathEnumerator pathE = new PathEnumerator(myNetwork);
        pathList = new ArrayList<Path>(pathE.calcPaths(shgNode, rtmNode));

        // === Step 5: Use a multinomial logit function to assign probabilities to each path
        Map<Path, Double> pathMap = MultinomialLogit.calculate(pathList, -1.0);

        // === Step 6: Sort on the probabilities
        ArrayList<Path> keys = new ArrayList<Path>(pathMap.keySet());
        Collections.sort(keys);

        // See the results
        for (Path path : keys) {
            System.out.println(path.fullDescription());
            System.out.println("\tProbability: " + pathMap.get(path) + "\n");
            //List<Link> links = path.getLinks() ;
        }
    }
}
