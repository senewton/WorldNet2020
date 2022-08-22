package nl.ntpr.worldnet;

import nl.ntpr.worldnet.database.TradeSqliteDatabase;
import nl.ntpr.worldnet.gis.LoadingPoints;
import nl.ntpr.worldnet.gis.MultiModalNetwork;
import nl.ntpr.worldnet.gis.SeaNetwork;
import nl.ntpr.worldnet.gis.InlandNetwork;

public class ContainerFlowModel {
    private String wnDataPath = "c:/home/code/WorldNet2020/data/";

    //private int[] reportingCountries = {1, 3, 4, 5, 17, 18, 38, 39, 61};
    private int[] reportingCountries = {1};

    //private int[] partnerCountries = {400, 508, 706, 720, 728, 732}; // USA, Brazil, Spore, China, Korea, Japan
    private int[] partnerCountries = {400};

    private String[] portCountries = {"BE", "DE", "EL", "ES", "FI", "FR", "IE", "IT", "LT", "NL", "PL", "PT", "RO",
            "SE", "SI", "UK", "BR", "CN", "JP", "KR", "SG", "US"};

    public ContainerFlowModel(){
        System.out.println("## Container Flow Model Initialised");

        // 1. Set up connection to trade database
        String trdbName = "trade_comext_transport.db";
        TradeSqliteDatabase tradeSqlDb = new TradeSqliteDatabase( trdbName );

        // 2. Open loading point nodes
        String lpName = this.wnDataPath + "loading/loading_points_01";
        LoadingPoints loadPts = new LoadingPoints(lpName);
        loadPts.openMidMifFile();

        // 3. Open Sea Network and List of Ports
        String portNodesName = this.wnDataPath + "ports/BZVP_Port_List_GEO_9";
        String snetName = this.wnDataPath + "networks/sea_net_S1P1_01";
        SeaNetwork seaNet = new SeaNetwork(snetName, portNodesName, this.portCountries);
        seaNet.openPortsCsvFile();
        seaNet.openSeaNetworkMidMifFile();
        //seaNet.listPortsInNetwork();

        // 4. Open inland networks
        String inetName = this.wnDataPath + "networks/inland_network_01";
        InlandNetwork inlNet = new InlandNetwork(inetName);
        inlNet.openInlandNetworkMidMifFile();
        //inlNet.listNodesInNetwork();

        // 5. Create the multimodal network and attach the components
        MultiModalNetwork mmNet = new MultiModalNetwork();
        mmNet.AttachLoadingPoints(loadPts);
        mmNet.AttachSeaNetwork(seaNet);
        mmNet.AttachInlandNetwork(inlNet);

        //mmNet.test();
        //mmNet.testNetwork();
        //System.exit(0);

        // 6: Start constructing the multimodal network:
        // - Add the sea connections and inland links to build up the links of the network
        mmNet.ConstructNetwork();
        // - Join the sea connections to the nearest inland nodes inside the mm network
        mmNet.JoinSeaToLand();
        // - Join the loading points to the nearest inland nodes inside the mm network
        // TODO: change this later to allow traffic to be spread across nodes in trading country
        mmNet.JoinLoadPtsToInland();
        // - And then turn these various sea, land links and the connectors into a network
        mmNet.CreateNetworkOutOfLinks();

        // 7: Try to route some traffic through the network
        for (int repC:reportingCountries) {
            for( int parC:partnerCountries){
                mmNet.FindRoutes(repC, parC);
            }
        }

    }

}
