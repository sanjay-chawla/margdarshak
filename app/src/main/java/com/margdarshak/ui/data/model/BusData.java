package com.margdarshak.ui.data.model;



//import java.sql.Array;
import java.util.ArrayList;
import java.util.List;

public class BusData {
/**"errorcode":"0","errormessage":"","numberofresults":9455,"timestamp":"21\/04\/2020 14:41:44","results":**/
    String errorcode;
    String errormessage;
    int numberofresults;
    String timestamp;
    List<Results> results;



   public BusData(){}
    public BusData(String errorcode, String errormessage, int numberofresults, String timestamp, List<Results> results) {
        this.errorcode = errorcode ;
        this.errormessage = errormessage;
        this.numberofresults = numberofresults;
        this.timestamp = timestamp;
        this.results = results;


    }


    public String getErrorcode() {
        return errorcode;
    }

    public void setErrorcode(String errorcode) {
        this.errorcode = errorcode;
    }

    public String getErrormessage() {
        return errormessage;
    }

    public void setErrormessage(String errormessage) {
        this.errormessage = errormessage;
    }

    public int getNumberofresults() {
        return numberofresults;
    }

    public void setNumberofresults(int numberofresults) {
        this.numberofresults = numberofresults;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public List<Results> getResults() {
        return results;
    }

    public void setResults(List<Results> results) {
        this.results=results;
    }



    public static class Results{
        /**"stopid":"2",
            "displaystopid":"2",
            "shortname":"Parnell Square","
        shortnamelocalized":"Cearnóg Parnell",
        "fullname":"Parnell Square",
            "fullnamelocalized":"",
            "latitude":"53.35224111",
            "longitude":"-6.263695",
            "lastupdated":"20\/04\/2020 09:44:13",
            "operators":[{"name":"bac","operatortype":1,"routes":["38","38A","38B","38D","46A","46E"]}]}
         */

        String stopid;
        String displaystopid;
        String shortname;
        String shortnamelocalized;
        String fullname;
        String fullnamelocalized;
        double latitude;
        double longitude;
        String lastupdated;
        List<Operators> operators;

   // public Results() {

   // }

    // operators routes;

       public static class Operators {
        String name;
        int operatortype;
        List<String> routes;

        Operators(String name ,int operatortype,List<String> routes) {
            this.name = name;
            this.operatortype = operatortype;
            this.routes=routes;

        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int  getOperatortype() {
            return operatortype;
        }

        public void setOperatortype(int operatortype) {
            this.operatortype = operatortype;
        }

           public List<String> getRoutes() {
               return routes;
           }

           public void setRoutes(List<String> routes) {
               this.routes=routes;
           }



       }

   public Results(String stopid,String displaystopid,String shortname,String shortnamelocalized,String fullname,String fullnamelocalized,double  latitude,double longitude,String lastupdated,List<Operators> operators) {
        this.stopid = stopid;
        this.displaystopid = displaystopid;
        this.shortname = shortname;
        this.shortnamelocalized = shortnamelocalized;
        this.fullname = fullname;
        this.fullnamelocalized = fullnamelocalized;
        this.latitude = latitude;
        this.longitude = longitude;
        this.lastupdated = lastupdated;
        this.operators = operators;


    }

    public String getStopid() {
        return stopid;
    }

    public void setStopid(String stopid) {
        this.stopid = stopid;
    }

    public String getDisplaystopid() {
        return displaystopid;
    }

    public void setDisplaystopid(String displaystopid) {
        this.displaystopid = displaystopid;
    }

    public String getShortname() {
        return shortname;
    }

    public void setShortname(String shortname) {
        this.shortname = shortname;
    }

    public String getShortnamelocalized() { return shortnamelocalized;
    }

    public void setShortnamelocalized(String shortnamelocalized) {this.shortnamelocalized = shortnamelocalized;
    }
    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String Lastupdated() {
        return lastupdated;
    }

    public void setLastupdated(String lastupdated) {
        this.lastupdated = lastupdated;
    }

    public List<Operators> getOperators() {
        return operators;
    }

    public void setOperators(List<Operators> operators) {
        this.operators = operators;
    }



   }
}
