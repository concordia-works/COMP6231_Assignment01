package Utils;

/**
 * Created by quocminhvu on 2017-05-26.
 */

public class Config {
    // Records
    public static final String STUDENT_RECORD_FORMAT = "%05d";
    public static final String TEACHER_RECORD_FORMAT = "%05d";

    // Servers
    public enum Server_ID {MTL, LVL, DDO}
    public static final String MONTREAL_HOSTNAME = "localhost";
    public static final String LAVAL_HOSTNAME = "localhost";
    public static final String DOLLARD_DES_ORMEAUX_HOSTNAME = "localhost";
    public static final int MONTREAL_UDP_PORT = 1234;
    public static final int LAVAL_UDP_PORT = 2345;
    public static final int DOLLARD_DES_ORMEAUX_UDP_PORT = 3456;
    public static final int MONTREAL_RMI_PORT = 4567;
    public static final int LAVAL_RMI_PORT = 5678;
    public static final int DOLLARD_DES_ORMEAUX_RMI_PORT = 6789;
    public static final String GET_RECORDS_COUNT_FUNC_NAME = "getRecordsNumber";
    public static final String RMI_REGISTRY_FORMAT = "rmi://%s:%d/";

    // Logging
    public static final String LOG_SERVER_FILENAME = "/Users/quocminhvu/Documents/workspace/IdeaProjects/COMP6231_Assignment01/log/server_%s.log";
    public static final String LOG_MANAGER_FILENAME = "/Users/quocminhvu/Documents/workspace/IdeaProjects/COMP6231_Assignment01/log/manager_%s.log";
    public static final String LOG_MODIFIED_RECORD_SUCCESS = "Modified record: ID = %s, FieldName = %s, Value = %s";
    public static final String LOG_MODIFIED_RECORD_FAILED = "No record found: ID = %s, FieldName = %s, Value = %s";
    public static final String LOG_ADD_TEACHER_RECORD = "Added record: ID = %s, Name = %s %s, Address = %s, Phone = %s, Specialization = %s, Location = %s";
    public static final String LOG_ADD_STUDENT_RECORD = "Added record: ID = %s, Name = %s %s, CoursesRegistered = %s, Status = %s";
    public static final String LOG_CLIENT_IP = "Client IP: %s";
    public static final String LOG_UDP_REQUEST_FROM = "UDP Request from: Address = %s, Port = %s";
    public static final String LOG_UDP_RESPONSE_TO = "UDP Response to: Address = %s, Port = %s";
    public static final String LOG_UDP_REQUEST_TO = "UDP Request to: Address = %s, Port = %s";
    public static final String LOG_UDP_RESPONSE_FROM = "UDP Response from: Address = %s, Port = %s";
    public static final String LOG_UDP_SERVER_START = "UDP Server started at port %s";
    public static final String LOG_UDP_SERVER_STOP = "UDP Server at port %s stopped";
    public static final String LOG_CONNECT_RMI_SUCCESS = "Connect to the %s server at port %s successfully";

    public static int getUDPPortByServerID(Server_ID server_id) {
        switch (server_id) {
            case MTL:
                return MONTREAL_UDP_PORT;
            case LVL:
                return LAVAL_UDP_PORT;
            case DDO:
                return DOLLARD_DES_ORMEAUX_UDP_PORT;
            default:
                return 0;
        }
    }

    public static int getRMIPortByServerID(Server_ID server_id) {
        switch (server_id) {
            case MTL:
                return MONTREAL_RMI_PORT;
            case LVL:
                return LAVAL_RMI_PORT;
            case DDO:
                return DOLLARD_DES_ORMEAUX_RMI_PORT;
            default:
                return 0;
        }
    }

    public static String getHostnameByServerID(Server_ID server_id) {
        switch (server_id) {
            case MTL:
                return MONTREAL_HOSTNAME;
            case LVL:
                return LAVAL_HOSTNAME;
            case DDO:
                return DOLLARD_DES_ORMEAUX_HOSTNAME;
            default:
                return "Wrong Server ID";
        }
    }
}
