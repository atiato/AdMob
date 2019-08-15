package io.fabric8.quickstarts.camel;

import java.sql.Date;

public class CurrentProfile {

	  private int  id;
	  private String imei_id;
      private int countryid;
      private String mobilenumber;
      private String name;
      private int optout;
      private int genderid;
      private int userid;
      private int cityid;

//UserID,GenderID,OptOut,CityID
	   
	  //  private int test;
	  //SERVICE_TYPE, CALL_DATE, REVENUE_MONTH, CALL_CLASS, sum(CHARGED_AMOUNT) as CHARGED_AMOUNT
	    
	    public int getID() {
	        return id;
	    }

	    public void setID(int id) {
	        this.id = id;
        }
        
        public int getOptout() {
	        return optout;
	    }

	    public void setOptout(int optout) {
	        this.optout = optout;
        }
        
        public int getUserid() {
	        return userid;
	    }

	    public void setUserid(int userid) {
	        this.userid = userid;
        }
        
        public int getCityid() {
	        return cityid;
	    }

	    public void setcityid(int cityid) {
	        this.cityid = cityid;
		}
		
		public int getCountryid() {
	        return countryid;
	    }

	    public void setCountryid(int countryid) {
	        this.countryid = countryid;
	    }
	    
	    public String getImei_id() {
	        return imei_id;
	    }

	    public void setImei(String imei_id) {
	        this.imei_id = imei_id;
	    }
	

        public String getMobilenumber() {
	        return mobilenumber;
	    }

	    public void setMobilenumber(String mobilenumber) {
	        this.mobilenumber = mobilenumber;
	    }
	
        public String getName() {
	        return name;
	    }

	    public void setName(String name) {
	        this.name = name;
	    }
	
	
	
}
