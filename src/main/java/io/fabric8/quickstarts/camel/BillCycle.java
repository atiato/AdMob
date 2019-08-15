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



public class BillCycle {
    private int id;
    private Date predict_start_date;
    private int operator_id;
  //  private int test;
    
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
    
    public Date getPredict_start_date() {
        return predict_start_date;
    }

    
    public void setPredict_start_date(Date predict_start_date) {
        this.predict_start_date = predict_start_date;
    }
    
    //bank_code
    
   
}
