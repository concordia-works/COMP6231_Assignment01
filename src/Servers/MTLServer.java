package Servers;

import Utils.Config;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import static Utils.Config.RMI_REGISTRY_FORMAT;

/**
 * Created by quocminhvu on 2017-05-26.
 */

public class MTLServer {
    public static void main(String args[]) throws Exception {
        // Change server ID here
        final Config.Server_ID serverID = Config.Server_ID.MTL;

        // Start RMI server
        CenterServer server = new CenterServer(serverID);
        Registry registry = LocateRegistry.createRegistry(server.getRmiPort());
        registry.bind(String.format(RMI_REGISTRY_FORMAT, serverID, server.getRmiPort()), server);
        System.out.println("Server " + String.format(RMI_REGISTRY_FORMAT, serverID, server.getRmiPort()) + " is running!!!");

        // Start UDP server
        server.startUDPServer();
    }
}
