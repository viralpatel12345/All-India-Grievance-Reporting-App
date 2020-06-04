package com.rcoe.allindia;

public class UserAndComplaint
{
    String user_id,email_address,complaint_id,complaint_type,complaint_sub_type,dateofcomplaint,url,postalcode,status;

    UserAndComplaint()
    {

    }

    public UserAndComplaint(String user_id, String email_address, String complaint_id, String complaint_type,
                            String complaint_sub_type, String dateofcomplaint,String url,String postalcode,String status)
    {
        this.user_id = user_id;
        this.email_address = email_address;
        this.complaint_id = complaint_id;
        this.complaint_type = complaint_type;
        this.complaint_sub_type = complaint_sub_type;
        this.dateofcomplaint = dateofcomplaint;
        this.url = url;
        this.postalcode=postalcode;
        this.status=status;

    }

    public String getUrl() {
        return url;
    }

    public String getPostalcode() {
        return postalcode;
    }

    public void setPostalcode(String postalcode) {
        this.postalcode = postalcode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDateofcomplaint() {
        return dateofcomplaint;
    }

    public void setDateofcomplaint(String dateofcomplaint) {
        this.dateofcomplaint = dateofcomplaint;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getEmail_address() {
        return email_address;
    }

    public void setEmail_address(String email_address) {
        this.email_address = email_address;
    }

    public String getComplaint_id() {
        return complaint_id;
    }

    public void setComplaint_id(String complaint_id) {
        this.complaint_id = complaint_id;
    }

    public String getComplaint_type() {
        return complaint_type;
    }

    public void setComplaint_type(String complaint_type) {
        this.complaint_type = complaint_type;
    }

    public String getComplaint_sub_type() {
        return complaint_sub_type;
    }

    public void setComplaint_sub_type(String complaint_sub_type) {
        this.complaint_sub_type = complaint_sub_type;
    }
}
