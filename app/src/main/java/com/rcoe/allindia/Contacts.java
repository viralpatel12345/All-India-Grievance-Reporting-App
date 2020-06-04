package com.rcoe.allindia;

public class Contacts
{
    String fname,lname,email,url;

    public Contacts()
    {

    }

    public Contacts(String fname, String lname, String email, String url) {
        this.fname = fname;
        this.lname = lname;
        this.email = email;
        this.url = url;
    }

    public String getFname() {
        return fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public String getLname() {
        return lname;
    }

    public void setLname(String lname) {
        this.lname = lname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
