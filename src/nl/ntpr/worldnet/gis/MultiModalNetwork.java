package nl.ntpr.worldnet.gis;

import nl.nea.neac.worldnet.network.*;
import nl.nea.neac.worldnet.network.algorithms.Dijkstra;
import nl.nea.neac.worldnet.network.algorithms.PathEnumerator ;
import nl.ntpr.worldnet.MultinomialLogit;
import nl.panteia.utils.gis.GISUtil;

import java.util.*;

public class MultiModalNetwork {

    /** Set of links for the multimodal network **/
    HashSet<Link> mmNetworkLinks = new HashSet<>();

    /** List of loading points which link the trading countries to the network **/
    LoadingPoints loadPoints;

    /** Sea network - made up of port to port connections, and the set of seaports **/
    SeaNetwork seaNet;

    /** Inland network - to connect the loading points to the ports **/
    InlandNetwork inlNet;

    /** WorldNet Style Network **/
    Network mmWorldnetNetwork;

    /** List of mm network paths **/
    ArrayList<Path> mmPathList;

    /** Worldnet Style Dijkstra Short Path Algorithm **/
    Dijkstra mmDijkstra;

    /** Constructor **/
    public MultiModalNetwork(){
        // test();
        System.out.println("\n#MM: Setting up MultiModal Network:");
        this.mmNetworkLinks.clear();
    }

    /** Method for attaching a connection for this class to the set of loading points **/
    public void AttachLoadingPoints(LoadingPoints lpts ){
        System.out.println("#MM: Attaching Loading Points");
        this.loadPoints = lpts;
        ArrayList<LoadingNode> loadPts = this.loadPoints.getListOfNodes();
        // for (LoadingNode ln : loadPts) {
           // ln.seeLoadingNode();
        // }
    }

    /** Method for attaching set network including ports to this class **/
    public void AttachSeaNetwork(SeaNetwork snet ){
        System.out.println("#MM: Attaching Sea Network");
        this.seaNet = snet;
        ArrayList<SeaConnection> sConns = this.seaNet.getListOfSeaConnections();
        // for (SeaConnection sc : sConns) {
           // sc.seeSeaConnection();
        // }
    }

    /** Method for attaching inland network to this class **/
    public void AttachInlandNetwork(InlandNetwork inet ){
        System.out.println("#MM: Attaching Inland Network");
        this.inlNet = inet;
        ArrayList<InlandLink> iLinks = this.inlNet.getListOfInlandLinks();
        // for (InlandLink inl : iLinks) {
            // inl.seeInlandLink();
        // }
    }

    /** Start building the network by adding sea and inland networks to combined 'mm' network **/
    public void ConstructNetwork(){
        this.mmNetworkLinks.clear();

        // Sea connections
        ArrayList<SeaConnection> sConns = this.seaNet.getListOfSeaConnections();
        int seaID = 1000;
        for (SeaConnection sc : sConns) {
            seaID++;
            Link sl_to = new Link(seaID, sc.distKM, sc.origWorldNetNode, sc.destWorldNetNode, LinkModality.SEA);
            Link sl_fm = new Link(-1*seaID, sc.distKM, sc.destWorldNetNode, sc.origWorldNetNode, LinkModality.SEA);
            this.mmNetworkLinks.add(sl_to);
            this.mmNetworkLinks.add(sl_fm);
        }

        // Inland links
        ArrayList<InlandLink> iLinks = this.inlNet.getListOfInlandLinks();
        int inlID = 2000;
        for (InlandLink ilnk : iLinks) {
            inlID++;
            Link il_to = new Link(inlID, ilnk.distKm, ilnk.NodeA.getWorldNetNode(), ilnk.NodeB.getWorldNetNode(),LinkModality.MIXED);
            Link il_fm = new Link(-1*inlID, ilnk.distKm, ilnk.NodeB.getWorldNetNode(), ilnk.NodeA.getWorldNetNode(),LinkModality.MIXED);
            this.mmNetworkLinks.add(il_to);
            this.mmNetworkLinks.add(il_fm);
        }
    }

    /** Add connecting links (representing seaports) joining sea connections to inland nets **/
    public void JoinSeaToLand(){
        // Run through list of active ports (the ones being used for the list of sea connections)
        ArrayList<PortNode> activePorts = this.seaNet.getListOfPortsInNetwork();
        Iterator<PortNode> pnit = activePorts.iterator();
        int seaInlID = 3000;
        while (pnit.hasNext()) {
            // Each port
            PortNode spNode = pnit.next();
            // System.out.println("Seaport: " + spNode.portName + ";" + spNode.xCoord + ";" + spNode.yCoord);

            if(this.seaNet.doesPortConnectToHinterland(spNode.portID)==true){
                seaInlID++;

                // Locate the nearest inland links
                InlandNode inNode = this.inlNet.locateNearestNode(spNode.xCoord, spNode.yCoord);
                double minMetres = GISUtil.getDistanceBetween(spNode.xCoord, spNode.yCoord, inNode.xCoord, inNode.yCoord);
                // System.out.println("Connects to: " + inNode.nSeq + ";" + inNode.xCoord + ";" + inNode.yCoord);
                // System.out.println("Dist Kms: " + minMetres/1000.0 );

                // Make new links for these and add them to mm network
                Link sll_to = new Link(seaInlID, minMetres/1000.0, spNode.getWorldNetNode(), inNode.getWorldNetNode());
                Link sll_fm = new Link(-1*seaInlID, minMetres/1000.0, inNode.getWorldNetNode(), spNode.getWorldNetNode());
                this.mmNetworkLinks.add(sll_to);
                this.mmNetworkLinks.add(sll_fm);
            } else{
                // System.out.println("Not connected to inland network");
            }
        }
    }

    /** Add connecting links joining sea connections to inland nets **/
    public void JoinLoadPtsToInland(){
        // Run through list of loading points
        ArrayList<LoadingNode> loadPoints = this.loadPoints.getListOfNodes();
        Iterator<LoadingNode> lpit = loadPoints.iterator();
        int lpInlID = 4000;
        while (lpit.hasNext()) {
            // Each loading point
            LoadingNode lpNode = lpit.next();
            // System.out.println("Loading Point: " + lpNode.lnName + ";" + lpNode.xCoord + ";" + lpNode.yCoord);

            lpInlID++;

            // Locate the nearest inland links
            InlandNode inNode = this.inlNet.locateNearestNode(lpNode.xCoord, lpNode.yCoord);
            double minMetres = GISUtil.getDistanceBetween(lpNode.xCoord, lpNode.yCoord, inNode.xCoord, inNode.yCoord);
            // System.out.println("Connects to: " + inNode.nSeq + ";" + inNode.xCoord + ";" + inNode.yCoord);
            // System.out.println("Dist Kms: " + minMetres/1000.0 );

            // Make new links for these and add them to mm network
            Link lil_to = new Link(lpInlID, minMetres/1000.0, lpNode.getWorldNetNode(), inNode.getWorldNetNode());
            Link lil_fm = new Link(-1*lpInlID, minMetres/1000.0, inNode.getWorldNetNode(), lpNode.getWorldNetNode());
            this.mmNetworkLinks.add(lil_to);
            this.mmNetworkLinks.add(lil_fm);
        }
    }


    /** Build the Worldnet style multimodal network out of links**/
    public void CreateNetworkOutOfLinks(){
        // Set up the Worldnet Style Network and construct the related data structures for holding the paths
        this.mmWorldnetNetwork = new Network(this.mmNetworkLinks);
        this.mmPathList = new ArrayList<>();
        this.mmDijkstra = new Dijkstra(this.mmWorldnetNetwork);
        System.out.println("#MM: Constructed Multimodal Network: " );
        System.out.println("#MM: Total Links: " + this.mmWorldnetNetwork.numberOfLinks());
        System.out.println("#MM: Total Nodes: " + this.mmWorldnetNetwork.numberOfNodes());
    }

    public void testNetwork(){
        // Inland links
        ArrayList<InlandLink> iLinks = this.inlNet.getListOfInlandLinks();
        int inlID = 2000;
        int rvlID = 20000;
        for (InlandLink ilnk : iLinks) {
            inlID++;
            rvlID++;
            Link il = new Link(inlID, ilnk.distKm, ilnk.NodeA.getWorldNetNode(), ilnk.NodeB.getWorldNetNode(),LinkModality.MIXED);
            Link rv = new Link(rvlID, ilnk.distKm, ilnk.NodeB.getWorldNetNode(), ilnk.NodeA.getWorldNetNode(),LinkModality.MIXED);
            this.mmNetworkLinks.add(il);
            this.mmNetworkLinks.add(rv);
        }

        this.mmWorldnetNetwork = new Network(this.mmNetworkLinks);
        // this.mmPathEnumerator = new PathEnumerator(this.mmWorldnetNetwork);
        Dijkstra mmDijkstra = new Dijkstra(this.mmWorldnetNetwork);

        Coord dc = new Coord(0.096, 49.479);
        Coord oc = new Coord(17.0, 51.0);

        //this.mmPathList = new ArrayList<Path>(this.mmPathEnumerator.calcPaths(
        this.mmPathList = new ArrayList<Path>(mmDijkstra.calcPaths(
                this.mmWorldnetNetwork.getClosestNode(oc),
                this.mmWorldnetNetwork.getClosestNode(dc))
        );
        System.out.println("Size of path list:" + this.mmPathList.size());

        // === Step 5: Use a multinomial logit function to assign probabilities to each path
        Map<Path, Double> pathMap = MultinomialLogit.calculate(this.mmPathList, -1.0);
        System.out.println("Calculated paths:" + pathMap.size());

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

    /** Test function for generating multimodal paths **/
    /** Inputs are the Comext style country codes **/
    public void FindRoutes(int repCountry, int parCountry){
        LoadingNode repLoadingNode = this.loadPoints.getLoadingNodeFromComextCode(repCountry);
        LoadingNode parLoadingNode = this.loadPoints.getLoadingNodeFromComextCode(parCountry);

        this.mmPathList = new ArrayList<Path>(this.mmDijkstra.calcPaths(repLoadingNode.getWorldNetNode(), parLoadingNode.getWorldNetNode()));

        // === Step 5: Use a multinomial logit function to assign probabilities to each path
        Map<Path, Double> pathMap = MultinomialLogit.calculate(this.mmPathList, -1.0);
        System.out.println("Calculated paths:" + pathMap.size());

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


    /* Shows how to build up a simple network */
    public void test() {
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
