package Servers;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.ServerNotActiveException;

/**
 * Created by quocminhvu on 2017-05-26.
 */
public interface ServerInterface extends Remote {
    String createTRecord(String firstName, String lastName, String address, String phone, String specialization, String location) throws RemoteException, ServerNotActiveException;
    String createSRecord(String firstName, String lastName, String coursesRegistered, String status) throws RemoteException, ServerNotActiveException;
    String getRecordCounts() throws RemoteException;
    boolean editRecord(String recordID, String fieldName, String newValue) throws RemoteException;
    String printRecords() throws RemoteException;
    Record locateRecord(String recordID) throws RemoteException;
}
