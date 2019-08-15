package io.fabric8.quickstarts.camel.pojo;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class Result {

private Date _time;
private String invoice_amount;
private String _ci;
private String _ft;
private String _hb;
private String _lowerinvoice_amount;
private String _predictedinvoice_amount;
private String _span;
private String _spandays;
private String _upperinvoice_amount;
private String _vars;
private String lower0Prediction;
private String prediction;
private String upper0Prediction;
private Map<String, Object> additionalProperties = new HashMap<String, Object>();

public Date get_time() {
return _time;
}

public void set_time(Date _time) {
//	String dateTime=_time.toString();
//	String date=dateTime.substring(0, 10);
//	Date d;
//	try {
//		d = (Date) new SimpleDateFormat("YYYY-MM-dd").parse(date);
		this._time = _time;
//	} catch (ParseException e) {
//		// TODO Auto-generated catch block
//		e.printStackTrace();
//	}

}

public String getInvoice_amount() {
return invoice_amount;
}

public void setInvoice_amount(String invoice_amount) {
this.invoice_amount = invoice_amount;
}

public String get_ci() {
return _ci;
}

public void set_ci(String _ci) {
this._ci = _ci;
}

public String get_ft() {
return _ft;
}

public void set_ft(String _ft) {
this._ft = _ft;
}

public String get_hb() {
return _hb;
}

public void set_hb(String _hb) {
this._hb = _hb;
}

public String get_lowerinvoice_amount() {
return _lowerinvoice_amount;
}

public void set_lowerinvoice_amount(String _lowerinvoice_amount) {
this._lowerinvoice_amount = _lowerinvoice_amount;
}

public String get_predictedinvoice_amount() {
return _predictedinvoice_amount;
}

public void set_predictedinvoice_amount(String _predictedinvoice_amount) {
this._predictedinvoice_amount = _predictedinvoice_amount;
}

public String get_span() {
return _span;
}

public void set_span(String _span) {
this._span = _span;
}

public String get_spandays() {
return _spandays;
}

public void set_spandays(String _spandays) {
this._spandays = _spandays;
}

public String get_upperinvoice_amount() {
return _upperinvoice_amount;
}

public void set_upperinvoice_amount(String _upperinvoice_amount) {
this._upperinvoice_amount = _upperinvoice_amount;
}

public String get_vars() {
return _vars;
}

public void set_vars(String _vars) {
this._vars = _vars;
}

//public String getLower0Prediction() {
//return lower0Prediction;
//}
//
//public void setLower0Prediction(String lower0Prediction) {
//this.lower0Prediction = lower0Prediction;
//}

public String getPrediction() {
return prediction;
}

public void setPrediction(String prediction) {
this.prediction = prediction;
}

//public String getUpper0Prediction() {
//return upper0Prediction;
//}
//
//public void setUpper0Prediction(String upper0Prediction) {
//this.upper0Prediction = upper0Prediction;
//}

public Map<String, Object> getAdditionalProperties() {
return this.additionalProperties;
}

public void setAdditionalProperty(String name, Object value) {
this.additionalProperties.put(name, value);
}

}