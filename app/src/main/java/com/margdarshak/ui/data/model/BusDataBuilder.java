package com.margdarshak.ui.data.model;
import com.margdarshak.ui.data.model.BusData.Results;
import com.margdarshak.ui.data.model.BusData.Results.Operators;


import java.util.List;

public class BusDataBuilder {
    private String errorcode;
    private String errormessage;
    private int numberofresults;
    private String timestamp;
     private List<Results> results;
     private List<Operators> operators;
    private List<String> routes;


    public BusDataBuilder setErrorcode(String errorcode) {
        this.errorcode=errorcode;
        return this;
    }

    public BusDataBuilder setErrormessage(String errormessage) {
        this.errormessage=errormessage;
        return this;
    }

    public BusDataBuilder setNumberofresults(int numberofresults) {
        this.numberofresults=numberofresults;
        return this;
    }

    public BusDataBuilder setTimestamp(String timestamp) {
        this.timestamp=timestamp;
        return this;
    }

   // public BusDataBuilder setResults (List<Results> results){
        //  (String stopid, String displaystopid,String shortname, String shortnamelocalized,String fullname,String fullnamelocalized,double latitude,double longitude,long lastupdated,Operators operators){
     //   this.results=results;
        //(stopid,displaystopid,shortname, shortnamelocalized,fullname,fullnamelocalized,latitude,longitude,lastupdated,operators);
     //   return this;
  //  }

    public BusDataBuilder setResults(String stopid, String displaystopid, String shortname, String shortnamelocalized, String fullname, String fullnamelocalized, double latitude, double longitude, String lastupdated,List<Operators> operators) {
       this.results=(List<Results>) new Results(stopid, displaystopid, shortname, shortnamelocalized, fullname, fullnamelocalized, latitude, longitude, lastupdated, operators);
        //Results results=new Results(this,Operators.class);
        return this;
    }

    public BusDataBuilder setOperators(String name , int operatortype, List<String> routes){
        this.operators=(List<Operators>)new Operators(name, operatortype,  routes);
        //Results results=new Results(this,Operators.class);
        return this;
    }

   // public BusDataBuilder setRoutes(List<String> routes ){
      //  this.routes= routes;
     //   //Results results=new Results(this,Operators.class);
      //  return this;
   // }
    public class ResultsBuilder {
        private String stopid;
        private String displaystopid;
        private String shortname;
        private String shortnamelocalized;
        private String fullname;
        private String fullnamelocalized;
        private double latitude;
        private double longitude;
        private long lastupdated;
        private List<Operators> operators;
        private List<String> routes;

        public ResultsBuilder setStopid(String stopid) {
            this.stopid=stopid;
            return this;
        }

        public ResultsBuilder setDisplaystopid(String displaystopid) {
            this.displaystopid=displaystopid;
            return this;
        }

        public ResultsBuilder setShortname(String shortname) {
            this.shortname=shortname;
            return this;
        }

        public ResultsBuilder Shortnamelocalized(String shortnamelocalized) {
            this.shortnamelocalized=shortnamelocalized;
            return this;
        }

        public ResultsBuilder Fullname(String fullname) {
            this.fullname=fullname;
            return this;
        }

        public ResultsBuilder Fullnamelocalized(String fullnamelocalized) {
            this.fullnamelocalized=fullnamelocalized;
            return this;
        }

        public ResultsBuilder latitude(double latitude) {
            this.latitude=latitude;
            return this;
        }

        public ResultsBuilder longitude(double longitude) {
            this.longitude=longitude;
            return this;
        }


        public ResultsBuilder setLastupdated(long lastupdated) {
            this.lastupdated=lastupdated;
            return this;
        }



        public ResultsBuilder setOperators(String name , int operatortype, List<String> routes){
            this.operators=(List<Operators>)new Operators(name, operatortype,  routes);
            //Results results=new Results(this,Operators.class);
            return this;
        }

        public class OperatorsBuilders {
            private String name;
            private int operatortype;
            private List<String> routes;


            public OperatorsBuilders setName(String name) {
                this.name=name;
                return this;
            }


            public OperatorsBuilders setOperatortype(int operatortype) {
                this.operatortype=operatortype;
                return this;
            }


            public OperatorsBuilders setRoutes(List<String> routes) {
                this.routes=routes;
                //Results results=new Results(this,Operators.class);
                return this;
            }

            public class RoutesBuilder {
                private String routes;


                public RoutesBuilder setRoutes(String routes) {
                    this.routes=routes;
                    return this;
                }
            }
        }

    }


    //public Results createresult() {
   //     return new Results(stopid,displaystopid,shortname, shortnamelocalized,fullname,fullnamelocalized,latitude,longitude,lastupdated,operators);
  //  }

    public BusData createBusData(){
        return new BusData(errorcode,errormessage,numberofresults,timestamp,results);

    }
}
