package com.rcoe.allindia;

public class Details {
    private String c_type, c_subtype, area, postalcode, lantitude, longitude, description, status, state, dateofcomplaint;

    public Details() {

    }

    public Details(String c_type, String c_subtype, String area, String postalcode, String lantitude, String longitude, String description, String status, String state, String dateofcomplaint) {
        this.c_type = c_type;
        this.c_subtype = c_subtype;
        this.area = area;
        this.postalcode = postalcode;
        this.lantitude = lantitude;
        this.longitude = longitude;
        this.description = description;
        this.status = status;
        this.state = state;
        this.dateofcomplaint = dateofcomplaint;
    }

    public String getC_type() {
        return c_type;
    }

    public void setC_type(String c_type) {
        this.c_type = c_type;
    }

    public String getC_subtype() {
        return c_subtype;
    }

    public void setC_subtype(String c_subtype) {
        this.c_subtype = c_subtype;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getPostalcode() {
        return postalcode;
    }

    public void setPostalcode(String postalcode) {
        this.postalcode = postalcode;
    }

    public String getLantitude() {
        return lantitude;
    }

    public void setLantitude(String lantitude) {
        this.lantitude = lantitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getDateofcomplaint() {
        return dateofcomplaint;
    }

    public void setDateofcomplaint(String dateofcomplaint) {
        this.dateofcomplaint = dateofcomplaint;
    }
}


