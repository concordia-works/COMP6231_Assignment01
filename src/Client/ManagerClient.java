package Client;

import Servers.Record;
import Servers.ServerInterface;
import Utils.Config;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ServerNotActiveException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import static Utils.Config.RMI_REGISTRY_FORMAT;
import static java.lang.Thread.sleep;

/**
 * Created by quocminhvu on 2017-05-19.
 */

public class ManagerClient implements Runnable {
    private Logger LOGGER;
    private String managerID;
    private String testPurpose;

    protected ManagerClient(String managerID, String testPurpose) throws IOException {
        this.managerID = managerID;
        this.testPurpose = testPurpose;
    }

    @Override
    public void run() {
        // Get the serverID by servers' acronym as managerID's prefix
        Config.Server_ID serverID = Config.Server_ID.valueOf(managerID.substring(0, 3).toUpperCase());

        try {
            // Initiate different log file for different managers login
            initiateLogger(managerID);

            // Connect to the RMI server that the managerID belongs to
            ServerInterface server = connectToRmiServer(serverID);
            LOGGER.info(String.format(Config.LOG_CONNECT_RMI_SUCCESS, serverID.name(), Config.getRMIPortByServerID(serverID)));

            switch (testPurpose) {
                case "newStudent":
                    newStudent(server);
                    break;
                case "newTeacher":
                    newTeacher(server);
                    break;
                case "editRecord":
                    for (int i = 0; i < 32; i++) {
                        editRecord(server, String.format("%05d", i));
                    }
//                    editRecord(server, String.format("00002"));
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            LOGGER.severe(e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String args[]) throws Exception {
        /**
         * 32 threads create 16 new StudentRecord and 16 new TeacherRecord in MTL Server concurrently
         */
        for (int i = 0; i < 32; i++) {
            String ID = String.format("MTL%04d", i);
            if (i % 2 == 0)
                new Thread(new ManagerClient(ID, "newStudent")).start();
            else
                new Thread(new ManagerClient(ID, "newTeacher")).start();
        }

        // Print all records in MTL server
        sleep(6000);
        ServerInterface server = connectToRmiServer(Config.Server_ID.MTL);
        System.out.println(server.printRecords());
//
//        /**
//         * Multiple threads invoke all 3 servers to get the number of records in all servers concurrently
//         */
        for (int i = 0; i < 9; i++) {
            int finalI = i;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Config.Server_ID sID;
                        if (finalI % 3 == 0)
                            sID = Config.Server_ID.MTL;
                        else if (finalI % 3 == 1)
                            sID = Config.Server_ID.LVL;
                        else
                            sID = Config.Server_ID.DDO;
                        ServerInterface server = connectToRmiServer(sID);
                        System.out.println(server.getRecordCounts());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
//
        // 32 threads (32 clients) trying to edit all Records concurrently
        for (int i = 0; i < 32; i++) {
            String ID = String.format("MTL%04d", i);
            new Thread(new ManagerClient(ID, "editRecord")).start();
        }

        // Print all records
        sleep(10000);
        System.out.println(server.printRecords());
    }

    // Helper functions
    private static ServerInterface connectToRmiServer(Config.Server_ID serverID) throws Exception {
        int rmiPort = Config.getRMIPortByServerID(serverID);
        Registry registry = LocateRegistry.getRegistry(rmiPort);
        return (ServerInterface) registry.lookup(String.format(RMI_REGISTRY_FORMAT, serverID, rmiPort));
    }

    private void initiateLogger(String managerID) throws IOException {
        LOGGER = Logger.getLogger(managerID);
        FileHandler fileHandler = new FileHandler(String.format(Config.LOG_MANAGER_FILENAME, managerID));
        LOGGER.addHandler(fileHandler);
        SimpleFormatter formatter = new SimpleFormatter();
        fileHandler.setFormatter(formatter);
    }

    // Test function
    private void newStudent(ServerInterface server) throws RemoteException, ServerNotActiveException {
        String recordID = server.createSRecord("Student", "S", "maths/physics", "Active");
        LOGGER.info(String.format(Config.LOG_ADD_STUDENT_RECORD, recordID, "Student", "S", "maths/physics", "Active"));
    }

    private void newTeacher(ServerInterface server) throws RemoteException, ServerNotActiveException {
        String recordID = server.createTRecord("Teacher", "S", "Concordia", "+1 111 111 1111", "maths", "MTL");
        LOGGER.info(String.format(Config.LOG_ADD_TEACHER_RECORD, recordID, "Teacher", "S", "Concordia", "+1 111 111 1111", "maths", "MTL"));
    }

    private void editStudent(ServerInterface server, String recordID) throws RemoteException, ServerNotActiveException {
        boolean isSuccess = server.editRecord(recordID, "coursesRegistered", "everything");
        LOGGER.info(Boolean.toString(isSuccess));
    }

    private void editTeacher(ServerInterface server, String recordID) throws RemoteException, ServerNotActiveException {
        boolean isSuccess = server.editRecord(recordID, "location", "LVL");
        LOGGER.info(Boolean.toString(isSuccess));
    }

    private void editRecord(ServerInterface server, String recordID) throws RemoteException, ServerNotActiveException {
        Record record = server.locateRecord(recordID);
        if (record.getRecordType() == Record.Record_Type.TEACHER)
            editTeacher(server, recordID);
        else
            editStudent(server, recordID);
    }
}
