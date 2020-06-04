package com.rcoe.allindia;

public class Complaints
{
      private String c_type,c_subtype,url,dateofcomplaint,key;

      public Complaints()
      {

      }

      public Complaints(String c_type, String c_subtype, String url, String dateofcomplaint,String key) {
            this.c_type = c_type;
            this.c_subtype = c_subtype;
            this.url = url;
            this.dateofcomplaint = dateofcomplaint;
            this.key = key;
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

      public String getUrl() {
            return url;
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

      public String getKey() {
            return key;
      }

      public void setKey(String key) {
            this.key = key;
      }
}
