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



public class Opconfig {
    private String operator_code;
   
    private double cmc_coe_ratio_local;
    private double cmc_coe_ratio_roaming;
    private String operator_name;
    private int payment_type_id;
    private double operator_tax;
    private String username;
    private String bank_code;
    private String bank_name;
    private String branch;
    private String phone;
    private String email;
    private String account_number;
    private Timestamp stopping_date;
    private String note;
    private String currency;


    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
    public Timestamp getStopping_date() {
        return stopping_date;
    }

    public void setStopping_date(Timestamp stopping_date) {
        this.stopping_date = stopping_date;
    }

    public String getAccount_number() {
        return account_number;
    }

    public void setAccount_number(String account_number) {
        this.account_number = account_number;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getBank_name() {
        return bank_name;
    }

    public void setBank_name(String bank_name) {
        this.bank_name = bank_name;
    }

    public String getBank_code() {
        return bank_code;
    }

    public void setBank_code(String bank_code) {
        this.bank_code = bank_code;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
  
    public String getOperator_code() {
        return operator_code;
    }

    public void setOperator_code(String operator_code) {
        this.operator_code = operator_code;
    }

    public String getOperator_name() {
        return operator_name;
    }

    public void setOperator_name(String operator_name) {
        this.operator_name = operator_name;
    }

   

    public double getCmc_coe_ratio_local() {
        return cmc_coe_ratio_local;
    }

    public void setCmc_coe_ratio_local(double cmc_coe_ratio_local) {
        this.cmc_coe_ratio_local = cmc_coe_ratio_local;
    }
    
    public double getCmc_coe_ratio_roaming() {
        return cmc_coe_ratio_roaming;
    }

    public void setCmc_coe_ratio_roaming(double cmc_coe_ratio_roaming) {
        this.cmc_coe_ratio_roaming = cmc_coe_ratio_roaming;
    }
    

  
    
    
    
    public int getPayment_type_id() {
        return payment_type_id;
    }

    public void setPayment_type_id(int payment_type_id) {
        this.payment_type_id = payment_type_id;
    }
    
   
    public double getOperator_tax() {
        return operator_tax;
    }

    public void setOperator_tax(double operator_tax) {
        this.operator_tax = operator_tax;
    }
}
