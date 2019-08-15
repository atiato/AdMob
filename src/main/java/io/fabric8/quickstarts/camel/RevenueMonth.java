package io.fabric8.quickstarts.camel;

import java.sql.Date;

public class RevenueMonth {

	  private String service_type;
	  private String call_date;
	  private String revenue_month;
	  private String call_class;
	  private double charged_amount;


	   
	  //  private int test;
	  //SERVICE_TYPE, CALL_DATE, REVENUE_MONTH, CALL_CLASS, sum(CHARGED_AMOUNT) as CHARGED_AMOUNT
	    
	    public String getService_type() {
	        return service_type;
	    }

	    public void setService_type(String service_type) {
	        this.service_type = service_type;
		}
		
		public double getCharged_amount() {
	        return charged_amount;
	    }

	    public void setCharged_amount(double charged_amount) {
	        this.charged_amount = charged_amount;
	    }
	    
	    public String getCall_class() {
	        return call_class;
	    }

	    public void setCall_class(String call_class) {
	        this.call_class = call_class;
	    }
	

	    public String getRevenue_month() {
	        return revenue_month;
	    }

	    public void setRevenue_month(String revenue_month) {
	        this.revenue_month = revenue_month;
		}
		

	    public String getCall_date() {
	        return call_date;
	    }

	    public void setCall_date(String call_date) {
	        this.call_date = call_date;
	    }
	
	
	
}
