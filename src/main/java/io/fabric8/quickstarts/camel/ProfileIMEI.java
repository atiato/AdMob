
package io.fabric8.quickstarts.camel;

import java.sql.Date;

public class ProfileIMEI {

	  private int  id;
	  private String imei;
	  private int profileID;
	  


	   
	  //  private int test;
	  //SERVICE_TYPE, CALL_DATE, REVENUE_MONTH, CALL_CLASS, sum(CHARGED_AMOUNT) as CHARGED_AMOUNT
	    
	    public int getID() {
	        return id;
	    }

	    public void setID(int id) {
	        this.id = id;
		}
		
		public int getProfileID() {
	        return profileID;
	    }

	    public void setProfileiD(int profileID) {
	        this.profileID = profileID;
	    }
	    
	    public String getImei() {
	        return imei;
	    }

	    public void setImei(String imei) {
	        this.imei = imei;
	    }
	

	  
	
	
	
}