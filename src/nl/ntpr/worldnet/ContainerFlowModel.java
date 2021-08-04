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
    private int[] partnerCountries = {732};

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
        SeaNetwork seaNet = new SeaNetwork(snetName, portNodesName);
        seaNet.openPortsCsvFile();
        seaNet.openSeaNetworkMidMifFile();
        // seaNet.listPortsInNetwork();

        // 4. Open inland networks
        String inetName = this.wnDataPath + "networks/inland_network_01";
        InlandNetwork inlNet = new InlandNetwork(inetName);
        inlNet.openInlandNetworkMidMifFile();
        //inlNet.listNodesInNetwork();

        // 5. Set up the multimodal network
        MultiModalNetwork multiNet = new MultiModalNetwork();

    }

}
