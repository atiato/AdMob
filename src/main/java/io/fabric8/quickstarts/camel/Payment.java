/*
 * Copyright 2005-2016 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version
 * 2.0 (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */
package io.fabric8.quickstarts.camel;

import java.sql.Date;
import java.sql.Timestamp;

import org.joda.time.DateTime;

//create table Payment (
//op_code varchar(10),
//invoice_number varchar(60),
//ref_invoice varchar(60),
//amount double,
//currency varchar(10),
//status integer,
//payment_type integer,
//payment_date date,
//bank_code varchar(20)
//);

/* God bless lebanon*/

public class Payment {
	private int id;
    private int operator_id;
    private String invoice_record;
    private String ref_invoice;
    private double payment_amount;
    private String currency;
    private String status;
    private int payment_type_id;
    private Timestamp payment_date;
    private Timestamp log_date;
    private String username;
  
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    
    public int getOperator_id() {
        return operator_id;
    }

    public void setOperator_id(int operator_id) {
        this.operator_id = operator_id;
    }

    public String getInvoice_record() {
        return invoice_record;
    }

    public void setInvoice_record(String invoice_record) {
        this.invoice_record = invoice_record;
    }

    public double getPayment_amount() {
        return payment_amount;
    }

    public void setPayment_amount(double payment_amount) {
        this.payment_amount = payment_amount;
    }
    
    

    public String getRef_invoice() {
        return ref_invoice;
    }

    public void setRef_invoice(String ref_invoice) {
        this.ref_invoice = ref_invoice;
    }
    
    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    } 
    
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    
    public int getPayment_type_id() {
        return payment_type_id;
    }

    public void setPayment_type_id(int payment_type_id) {
        this.payment_type_id = payment_type_id;
    }

    public Timestamp getPayment_date() {
        return payment_date;
    }

    
    public void setPayment_date(Timestamp payment_date) {
        this.payment_date = payment_date;
    }
    
    //bank_code
    
    public Timestamp getLog_date() {
        return log_date;
    }

    public void setLog_date(Timestamp log_date) {
        this.log_date = log_date;
    }
    
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
