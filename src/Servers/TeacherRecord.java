package Servers;

/**
 * Created by quocminhvu on 2017-05-19.
 */

public class TeacherRecord extends Record {
    public enum Mutable_Fields {address, phone, location}
    private String address;
    private String phone;
    private String specialization;
    private String location;

    public TeacherRecord(String recordID, String firstName, String lastName, String address, String phone, String specialization, String location) {
        super(recordID, firstName, lastName, Record_Type.TEACHER);
        this.address = address;
        this.phone = phone;
        this.specialization = specialization;
        this.location = location;
    }

    public String getAddress() {
        return this.address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return this.phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getSpecialization() {
        return this.specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getLocation() {
        return this.location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
