package Servers;

import Utils.Config;
import Utils.Config.Server_ID;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Created by quocminhvu on 2017-05-19.
 */

public class CenterServer extends UnicastRemoteObject implements ServerInterface {
    private Map<Character, ArrayList<Record>> recordsMap;
    private int recordID;
    private final Object lockID = new Object();
    private final Object lockCount = new Object();
    private Server_ID serverID;
    private int recordsCount;
    private int rmiPort;
    private int udpPort;
    private static final Logger LOGGER = Logger.getLogger(CenterServer.class.getName());

    public CenterServer(Server_ID serverID) throws Exception {
        super();
        this.recordsMap = new HashMap<>();
        this.recordID = 0;
        this.serverID = serverID;
        this.recordsCount = 0;
        this.rmiPort = Config.getRMIPortByServerID(serverID);
        this.udpPort = Config.getUDPPortByServerID(serverID);

        initiateLogger();
        LOGGER.info("Server " + this.serverID + " starts");
    }

    public int getRecordID() {
        return this.recordID;
    }

    public Server_ID getServerID() {
        return this.serverID;
    }

    public int getRmiPort() {
        return this.rmiPort;
    }

    public int getUdpPort() {
        return this.udpPort;
    }

    public int getRecordsNumber() {
        /**
         * This function could be called concurrently by many threads
         * when some servers request the number of records of this server at the same time
         * Make sure only one thread can access the shared variable at a time
         */
        synchronized (lockCount) {
            return this.recordsCount;
        }
    }

    public String createTRecord(String firstName, String lastName, String address, String phone, String specialization, String location) throws ServerNotActiveException {
        char lastNameInitial = Character.toUpperCase(lastName.charAt(0));

        /**
         * Generate the recordID for the new records
         * Lock the recordID variable to prevent multiple threads
         * from creating new records with the same recordID
         */
        String newRecordID;
        synchronized (lockID) {
            newRecordID = String.format(Config.TEACHER_RECORD_FORMAT, recordID);
            recordID++;
        }

        // Create new record
        TeacherRecord newRecord = new TeacherRecord(newRecordID, firstName, lastName, address, phone, specialization, location);

        /**
         * Lock the HashMap to prevent multiple threads from adding new ArrayList into the same key
         */
        ArrayList recordsList = getRecordsList(lastNameInitial);

        /**
         * Lock the corresponding ArrayList to the LastName's initial character
         * Multiple threads can modify the same ArrayList safely
         * Prevent unpredictable behaviors of ArrayList
         * Ensure recordCount is always true
         * Ensure server logs are updated and reflect server's activities correctly
         */
        synchronized (recordsList) {
            // Add the new record to the list
            recordsList.add(newRecord);
            recordsCount++;
            LOGGER.info(String.format(Config.LOG_ADD_TEACHER_RECORD,newRecordID, firstName, lastName, address, phone, specialization, location));
        }

        return newRecordID;
    }

    public String createSRecord(String firstName, String lastName, String coursesRegistered, String status) throws ServerNotActiveException {
        char lastNameInitial = Character.toUpperCase(lastName.charAt(0));

        /**
         * Generate the recordID for the new records
         * Lock the recordID variable to prevent multiple threads
         * from creating new records with the same recordID
         */
        String newRecordID;
        synchronized (lockID) {
            newRecordID = String.format(Config.STUDENT_RECORD_FORMAT, recordID);
            recordID++;
        }

        // Create new record
        StudentRecord newRecord = new StudentRecord(newRecordID, firstName, lastName, coursesRegistered, status, new Date());

        /**
         * Lock the HashMap to prevent multiple threads from adding new ArrayList into the same key
         */
        ArrayList recordsList = getRecordsList(lastNameInitial);

        /**
         * Lock the corresponding ArrayList to the LastName's initial character
         * Multiple threads can modify the same ArrayList safely
         * Prevent unpredictable behaviors of ArrayList
         * Ensure recordCount is always true
         * Ensure server logs are updated and reflect server's activities correctly
         */
        synchronized (recordsList) {
            // Add the new record to the list
            recordsList.add(newRecord);
            recordsCount++;
            LOGGER.info(String.format(Config.LOG_ADD_STUDENT_RECORD, newRecordID, firstName, lastName, newRecord.getCoursesRegistered(), status));
        }

        return newRecordID;
    }

    public String getRecordCounts() {
        DatagramSocket socket = null;
        String result = String.format("%s %d", serverID, getRecordsNumber());
        try {
            for (Server_ID id : Server_ID.values()) {
                if (id != serverID) {
                    socket = new DatagramSocket();
                    byte[] request = Config.GET_RECORDS_COUNT_FUNC_NAME.getBytes();
                    InetAddress host = InetAddress.getByName(Config.getHostnameByServerID(id));
                    DatagramPacket sentPacket = new DatagramPacket(request, Config.GET_RECORDS_COUNT_FUNC_NAME.length(), host, Config.getUDPPortByServerID(id));
                    socket.send(sentPacket);
                    LOGGER.info(String.format(Config.LOG_UDP_REQUEST_TO, host, Config.getUDPPortByServerID(id)));

                    byte[] response = new byte[1000];
                    DatagramPacket receivedPacket = new DatagramPacket(response, response.length);
                    socket.receive(receivedPacket);
                    LOGGER.info(String.format(Config.LOG_UDP_RESPONSE_FROM, host, Config.getUDPPortByServerID(id)));
                    result += String.format(", %s %s", id, new String(receivedPacket.getData()).trim());
                }
            }

            /**
             * Make sure that Log content will always come together
             */
            synchronized (LOGGER) {
                LOGGER.info(String.format(Config.LOG_CLIENT_IP, RemoteServer.getClientHost()));
                LOGGER.info(result);
            }
        } catch (Exception e) {
            LOGGER.info(e.getMessage());
            System.out.println(e.getMessage());
        } finally {
            if (socket != null)
                socket.close();
        }
        return result;
    }

    public boolean editRecord(String recordID, String fieldName, String newValue) {
        boolean isSuccess = false;
        Record recordFound = locateRecord(recordID);
        if (recordFound != null) {
            /**
             * Lock the record found to prevent multiple threads edit the same record
             */
            synchronized (recordFound) {
                if (recordFound.getRecordType().equals(Record.Record_Type.TEACHER)) {
                    TeacherRecord teacherRecord = (TeacherRecord) recordFound;
                    for (TeacherRecord.Mutable_Fields field : TeacherRecord.Mutable_Fields.values()) {
                        if (fieldName.compareTo(field.name()) == 0) try {
                            Class<?> c = teacherRecord.getClass();
                            Field f = c.getDeclaredField(fieldName);
                            f.setAccessible(true);
                            f.set(teacherRecord, newValue);
                            f.setAccessible(false);
                            isSuccess = true;
                        } catch (Exception e) {
                            LOGGER.severe(e.getMessage());
                            System.out.println(e.getMessage());
                            e.printStackTrace();
                        }
                    }
                } else { // Record_Type == STUDENT
                    StudentRecord studentRecord = (StudentRecord) recordFound;
                    for (StudentRecord.Mutable_Fields field : StudentRecord.Mutable_Fields.values()) {
                        if (fieldName.compareTo(field.name()) == 0) try {
                            Class<?> c = studentRecord.getClass();
                            Field whicheverField = c.getDeclaredField(fieldName);
                            whicheverField.setAccessible(true);
                            whicheverField.set(studentRecord, newValue);
                            whicheverField.setAccessible(false);
                            if (field == StudentRecord.Mutable_Fields.status) {
                                Field statusDateField = c.getDeclaredField(StudentRecord.Mutable_Fields.statusDate.name());
                                statusDateField.setAccessible(true);
                                statusDateField.set(studentRecord, new Date());
                                statusDateField.setAccessible(false);
                            }
                            isSuccess = true;
                        } catch (Exception e) {
                            LOGGER.severe(e.getMessage());
                            System.out.println(e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }
            }

            // Logging
            LOGGER.info(String.format(Config.LOG_MODIFIED_RECORD_SUCCESS, recordID, fieldName, newValue));
        } else
            LOGGER.info(String.format(Config.LOG_MODIFIED_RECORD_FAILED, recordID, fieldName, newValue));
        return isSuccess;
    }

    public void startUDPServer() {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(this.udpPort);
            LOGGER.info(String.format(Config.LOG_UDP_SERVER_START, this.udpPort));
            byte[] buffer = new byte[1000];

            while (true) {
                // Get the request
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                socket.receive(request);

                /**
                 * Each request will be handled by a thread
                 * Making sure that no request would be missed
                 */
                DatagramSocket finalSocket = socket;
                CenterServer finalServer = this;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        LOGGER.info(String.format(Config.LOG_UDP_REQUEST_FROM, request.getAddress(), request.getPort()));
                        String replyStr = "-1";

                        // Run whatever method requested by clients
                        Method whichEverMethod;
                        try {
                            whichEverMethod = finalServer.getClass().getMethod(new String(request.getData()).trim());
                            replyStr = whichEverMethod.invoke(finalServer).toString();
                        } catch (NoSuchMethodException e) {
                            LOGGER.severe(e.getMessage());
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            LOGGER.severe(e.getMessage());
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            LOGGER.severe(e.getMessage());
                            e.printStackTrace();
                        }

                        // Reply back
                        DatagramPacket response = new DatagramPacket(replyStr.getBytes(), replyStr.length(), request.getAddress(), request.getPort());
                        try {
                            finalSocket.send(response);
                        } catch (IOException e) {
                            LOGGER.severe(e.getMessage());
                            e.printStackTrace();
                        }
                        LOGGER.info(String.format(Config.LOG_UDP_RESPONSE_TO, request.getAddress(), request.getPort()));
                    }
                }).start();
            }
        } catch (Exception e) {
            LOGGER.severe(e.getMessage());
            System.out.println(e.getMessage());
        } finally {
            if (socket != null) {
                socket.close();
                LOGGER.info(String.format(Config.LOG_UDP_SERVER_STOP, this.udpPort));
            }

        }
    }

    public String printRecords() {
        String result = "";
        synchronized (recordsMap) {
            for (ArrayList<Record> recordsList : this.recordsMap.values()) {
                for (int i = 0; i < recordsList.size(); i++) {
                    if (recordsList.get(i).getRecordType() == Record.Record_Type.TEACHER) {
                        TeacherRecord teacherRecord = (TeacherRecord) recordsList.get(i);
                        result += i + " " + String.format(Config.LOG_ADD_TEACHER_RECORD, teacherRecord.getRecordID(), teacherRecord.getFirstName(), teacherRecord.getLastName(),teacherRecord.getAddress(), teacherRecord.getPhone(), teacherRecord.getSpecialization(), teacherRecord.getLocation());
                        result += System.getProperty("line.separator");
                    } else {
                        StudentRecord studentRecord = (StudentRecord) recordsList.get(i);
                        result += i + " " + String.format(Config.LOG_ADD_STUDENT_RECORD, studentRecord.getRecordID(), studentRecord.getFirstName(), studentRecord.getLastName(), studentRecord.getCoursesRegistered(), studentRecord.getStatus(), studentRecord.getStatusDate());
                        result += System.getProperty("line.separator");
                    }
                }
            }
        }
        return result;
    }

    public Record locateRecord(String recordID) {
        Record recordFound = null;
        boolean isRecordFound = false;
        /**
         * Synchronize the whole HashMap to prevent other
         * threads from modifying it when it is being iterated
         */
        synchronized (recordsMap) {
            for (ArrayList<Record> recordsList : this.recordsMap.values()) {
                Iterator<Record> iterator = recordsList.iterator();
                while ((iterator.hasNext()) && (!isRecordFound)) {
                    recordFound = iterator.next();
                    if (recordFound.getRecordID().compareTo(recordID) == 0) {
                        isRecordFound = true;
                        break;
                    }
                }
            }
        }
        if (!isRecordFound)
            recordFound = null;
        return recordFound;
    }

    private void initiateLogger() throws IOException {
        FileHandler fileHandler = new FileHandler(String.format(Config.LOG_SERVER_FILENAME, this.serverID));
        LOGGER.addHandler(fileHandler);
        SimpleFormatter formatter = new SimpleFormatter();
        fileHandler.setFormatter(formatter);
    }

    private ArrayList<Record> getRecordsList(char lastNameInitial) {
        synchronized (recordsMap) {
            if (recordsMap.containsKey(lastNameInitial))
                return recordsMap.get(lastNameInitial);
            else {
                ArrayList<Record> recordsList = new ArrayList<>();
                recordsMap.put(lastNameInitial, recordsList);
                return recordsList;
            }
        }
    }
}
