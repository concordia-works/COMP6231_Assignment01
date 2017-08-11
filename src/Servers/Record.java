package Servers;

import java.io.Serializable;

/**
 * Created by quocminhvu on 2017-05-19.
 */

public class Record implements Serializable {
    public enum Record_Type {TEACHER, STUDENT}
    private String recordID;
    private String firstName;
    private String lastName;
    private Record_Type recordType;

    public Record(String recordID, String firstName, String lastName, Record_Type recordType) {
        this.recordID = recordID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.recordType = recordType;
    }

    public String getRecordID() {
        return this.recordID;
    }

    public void setRecordID(String recordID) {
        this.recordID = recordID;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return this.lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Record_Type getRecordType() {
        return this.recordType;
    }
}
