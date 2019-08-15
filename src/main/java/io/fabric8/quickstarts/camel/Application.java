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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Map;

import javax.activation.FileDataSource;
import javax.sql.DataSource;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.component.servlet.CamelHttpTransportServlet;
import org.apache.camel.http.common.HttpOperationFailedException;
import org.apache.camel.impl.DefaultAttachment;
import org.apache.camel.impl.SimpleRegistry;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.model.rest.RestParamType;
import org.apache.camel.spi.Registry;
import org.apache.camel.spring.spi.SpringTransactionPolicy;
import org.apache.camel.util.jsse.KeyManagersParameters;
import org.apache.camel.util.jsse.KeyStoreParameters;
import org.apache.camel.util.jsse.SSLContextParameters;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;

import io.fabric8.quickstarts.camel.pojo.Root;
import io.fabric8.quickstarts.camel.pojo.UnAuthorize;

import org.apache.camel.util.jsse.SSLContextParameters;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;

@SpringBootApplication
@ImportResource({"classpath:spring/camel-context.xml"})
public class Application extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    ServletRegistrationBean servletRegistrationBean() {
        ServletRegistrationBean servlet = new ServletRegistrationBean(
            new CamelHttpTransportServlet(), "/camel-rest-sql/*");
        servlet.setName("CamelServlet");
        return servlet;
    }

    @Component
    class RestApi extends RouteBuilder {

        @Override
        public void configure() {

            onException(HttpOperationFailedException.class).handled(true).process(new Processor() {
                @Override
                public void process(Exchange exchange) throws Exception {
                  //  exchange.getIn().setBody("{Exception occured :"+ex.getMessage()+"}");
                    UnAuthorize test=new UnAuthorize();
                    test.setResult("You are UnAuthrized to access such API");
                    exchange.getIn().setBody(test);
                }
            });

        	
            restConfiguration()
         
                .contextPath("/api/mobile").apiContextPath("/api-doc")
                    .apiProperty("api.title", "Camel REST API")
                    .apiProperty("api.version", "1.0")
                    .apiProperty("cors", "true")
                    .apiContextRouteId("doc-api")
                .component("servlet")
                .bindingMode(RestBindingMode.json);

            rest("/").description("Exhange Rate REST service")
                .get("exchangerate/{currency}/{from_date}/{to_date}").description("The exchange for specified currency")
                    .route().routeId("exchange-rate-api")
                    .to("sql:select exchange_rate from Currency where currency =:#${header.currency} and from_date=:#${header.from_date} and to_date=:#${header.to_date}?" +
                        "dataSource=dataSource&" +
                        "outputClass=io.fabric8.quickstarts.camel.Currency")
                    .endRest()
                .get("sumrevenue").description("The sum revenu for specified currency")
                    .route().routeId("get-revenue-month")
                    .to("direct:Auth")
                    .to("sql:SELECT SERVICE_TYPE, CALL_DATE, REVENUE_MONTH, CALL_CLASS, sum(CHARGED_AMOUNT) as CHARGED_AMOUNT FROM factin_operator1d WHERE CALL_CLASS IN('INTERNATIONAL', 'LOCAL', 'ROAMING' )  group by CALL_CLASS,SERVICE_TYPE?" +
                        "dataSource=dataSource&" +
                        "outputClass=io.fabric8.quickstarts.camel.RevenueMonth")
                    .endRest()
                .post("exchangerate/insert/{currency}/{exchange_rate}/{from_date}/{to_date}/{description}").description("insert The exchange for specified currency")
                    .route().routeId("insert-exchange-rate-api")
                    .setHeader("CamelSqlRetrieveGeneratedKeys",simple("true", Boolean.class))

                    .to("sql:insert into Currency (id,Currency,Exchange_rate,from_date,to_date,description) values " +
                            "(:#${header.id} , :#${header.currency},:#${header.exchange_rate}, :#${header.from_date},:#${header.to_date}, :#${header.description})?" +
                            "dataSource=dataSource")
                    .log("all headers"+"${header.CamelSqlGeneratedKeyRows}"+"again"+"${header.CamelSqlGeneratedKeysRowCount}")
                   
                    .log("Inserted new order ${header.CamelSqlGeneratedKeyRows}")
                    .setBody(simple("${header.CamelSqlGeneratedKeyRows}"))
                    .endRest()
                .post("payment/").description("insert The payment for specified invoice")
                    .route().routeId("insert-payment-rate-api")
                    .setHeader("CamelSqlRetrieveGeneratedKeys",simple("true", Boolean.class))

                    .log("${body}")
                    .marshal().json(JsonLibrary.Jackson,Payment.class)
                    .log("${body}")
                    .unmarshal().json(JsonLibrary.Jackson,Payment.class)
                    .log("${body}")
                    .log("${body.getPayment_amount}")

                    .to("sql:insert into payment (operator_id,invoice_record,ref_invoice,currency,payment_amount,status,payment_type_id,payment_date,log_date,username) values " +
                            "(:#${body.getOperator_id} , :#${body.getInvoice_record},:#${body.getRef_invoice},:#${body.getCurrency},:#${body.getPayment_amount},:#${body.getStatus}, :#${body.getPayment_type_id},:#${body.getPayment_date},:#${body.getLog_date},:#${body.getUsername})?" +
                            "dataSource=dataSource")
                    
                    .setBody(simple("${header.CamelSqlGeneratedKeyRows}"))
                    .endRest() 
                .get("QueryPayment/{operator_id}/{ref_invoice}").description("Query The payment for specified operator & invoice")
                    .route().routeId("Query-payment-rate-api-opperinvoice")
                    .to("sql:select * from Payment where operator_id =:#${header.operator_id} and ref_invoice=:#${header.ref_invoice}?" +
                            "dataSource=dataSource&" +
                            "outputClass=io.fabric8.quickstarts.camel.Payment")
                    .endRest()    
                .get("QueryPayments/{op_code}/").description("Query The payment for specified operator")
                    .route().routeId("Query-payment-rate-api-opcode")
                    .to("sql:select * from Payment where op_code =:#${header.op_code}?" +
                            "dataSource=dataSource&" +
                            "outputClass=io.fabric8.quickstarts.camel.Payment")
                    .endRest()
        //         .post("sendemail").description("send file by email")
        //             .route().routeId("send-file-by-email")
        //             .setHeader("json",simple("${body}"))
        //       //   .to("direct:Auth")
        //             // .choice()
        //             //    .when(simple("${body.getResult} != 'You are UnAuthrized to access such API'"))
        //              //  .log("${body.getResult}")
        //            .setBody(simple("${header.json}"))
        //            .process(new Processor() {
        //             public void process(Exchange exchange) throws Exception {
                        
                
                        
        //              DefaultAttachment att = new DefaultAttachment(new FileDataSource("/data/backup/omar.pdf"));
     
                        
        //                 att.addHeader("Content-Description", "splunkattached");
        //                 exchange.getIn().addAttachmentObject("omar.pdf", att);
     
                        
                       
        //            }
        //         })
        //         .setBody(constant(""))
        //         .setHeader("subject", constant("Invoice to operator"))
        //         .setHeader("To", constant("omar.atia@immovate.net;ahmad.muslimany@immovate.net"))
        //   //      .setBody(simple("Diode status : ${header.Status}"))
        //      //   .setHeader("Body",simple("Diode status : ${header.Status}"))
        //         .log("Before sending email :  ${body}")
        //         .to("smtps://smtp.gmail.com:465?password=Oa03216287@&username=atiaomar1978@gmail.com&From=acs@immovate.com")
        //         .log("Inserted values ${body}")
        //         .log("Inserted values ${body}")
        //             .endRest()
                .get("/getAd").description("get add GET API.")
                    .param().name("id").type(RestParamType.query)
                    .dataType("String").endParam()
                    .param().name("Country").type(RestParamType.query)
                    .dataType("String").endParam()
                    .route()
                    .log("${header.id}"+"the country ${header.Country}")
                    .to("sql:select * from ACC_ProfileIMEI where IMEI =:#${header.id}?" +
                    "dataSource=dataSource&" +
                    "outputClass=io.fabric8.quickstarts.camel.ProfileIMEI")
                    .log("Get profile ID"+"${body[0].getProfileID}")
                //   //  .marshal().json(JsonLibrary.Jackson,ProfileIMEI.class)
                //     .unmarshal().json(JsonLibrary.Jackson,ProfileIMEI.class)
                    .to("sql:select ID,GenderID,IMEI_ID,MobileNumber,OptOut,Name,CountryID from ACC_UserProfile where ID =:#${body[0].getProfileID}?" +
                    "dataSource=dataSource&" +
                    "outputClass=io.fabric8.quickstarts.camel.CurrentProfile")
                    .log("Get ID"+"${body[0].getID}")
                    .to("sql-stored:ENG_GetAd(INTEGER ${body[0].getID})?"+"dataSource=dataSource")
                    .to("log:DEBUG?showBody=true&showHeaders=true")
                                .process(new Processor() {
                                    public void process(Exchange exchange) throws Exception {
                                       Map results = exchange.getIn().getBody(Map.class);
                                    //   ArrayList results = exchange.getIn().getHeader("op_id",ArrayList.class);
                                             
                                       ArrayList out= (ArrayList) results.get("#result-set-1");
                                       Map finalresult=(Map)out.get(0);
                                       exchange.getIn().setHeader("AdID",finalresult.get("AdID"));
                                       
                                   }
                                })
                                .log("headerID"+"${header.AdID}")
                                .to("sql:select id,adposter,budget,reach,status,advertiserid,chargeonimpression,chargeonclick,adlayoutid,isinvasive from ENG_Ads where ID =:#${header.AdID}?" +
                                "dataSource=dataSource&" +
                                "outputClass=io.fabric8.quickstarts.camel.ads")
                                .setHeader("adsbody",simple("${body}"))
                                .to("sql:select * from ENG_Advertisers where ID =:#${body[0].getAdvertiserid}?" +
                                "dataSource=dataSource")// +
                                .to("log:DEBUG?showBody=true&showHeaders=true")

                              //  "outputClass=io.fabric8.quickstarts.camel.ads")
                                .process(new Processor() {
                                                      public void process(Exchange exchange) throws Exception {
                                                          ArrayList results = exchange.getIn().getBody(ArrayList.class);
                                                          ads result=exchange.getIn().getHeader("adsbody",ads.class);
                                                          Map out = (Map) results.get(0);
                                                      
                                                     //     exchange.getIn().setHeader("Name", out.get("Name"));
                                                   
                                                      //    exchange.getIn().setHeader("Image", out.get("Image"));
                                                          
                                                          result.setAdvertiserName((String) out.get("Name"));
                                                         // result.setIsinvasive((Boolean)out.get("isinvasive"));
                                                          result.setAdvertiserImage(""+exchange.getIn().getHeader("Host")+"/content/AdvertiserImages/"+(String) out.get("Image"));

                                                          exchange.getIn().setBody(result);

                                    //DTM_DATE
                                                                      
                                                     }
                                                  })   
                                .choice()
                                .when(simple("${header.adsbody[0].getAdlayoutid} == 1"))
                                        .to("sql:select * from ENG_AdText where AdID =:#${header.AdID}?" +
                                        "dataSource=dataSource&"+"outputClass=io.fabric8.quickstarts.camel.addlayout")
                                        .process(new Processor() {
                                            public void process(Exchange exchange) throws Exception {
                                                addlayout results = exchange.getIn().getBody(addlayout.class);
                                                results.setLayout("Text");
                                                ads result=exchange.getIn().getHeader("adsbody",ads.class);
                                            
                                                result.setText(results);
                                                result.setImage(null);
                                                result.setVideo(null);
                                                result.setCarousel(null);
                                               
                                                exchange.getIn().setBody(result);
        
                          //DTM_DATE
                                                            
                                           }
                                        })   
                                .when(simple("${header.adsbody[0].getAdlayoutid} == 2"))
                                        .to("sql:select * from ENG_AdImage where AdID =:#${header.AdID}?" +
                                        "dataSource=dataSource&"+"outputClass=io.fabric8.quickstarts.camel.addlayout")

                                        .process(new Processor() {
                                            public void process(Exchange exchange) throws Exception {
                                                addlayout results = exchange.getIn().getBody(addlayout.class);
                                                results.setLayout("Image");

                                                ads result=exchange.getIn().getHeader("adsbody",ads.class);
                                            
                                                result.setImage(results);
                                                result.setText(null);
                                                result.setVideo(null);
                                                result.setCarousel(null);
                                               
                                                exchange.getIn().setBody(result);
        
                          //DTM_DATE
                                                            
                                           }
                                        })   
                                .when(simple("${header.adsbody[0].getAdlayoutid} == 3"))
                                        .to("sql:select * from ENG_AdVideo where AdID =:#${header.AdID}?" +
                                        "dataSource=dataSource&"+"outputClass=io.fabric8.quickstarts.camel.addlayout")
                                        .process(new Processor() {
                                            public void process(Exchange exchange) throws Exception {
                                                addlayout results = exchange.getIn().getBody(addlayout.class);
                                                results.setLayout("Video");

                                                ads result=exchange.getIn().getHeader("adsbody",ads.class);
                                            
                                                result.setVideo(results);
                                                result.setText(null);
                                                result.setImage(null);
                                                result.setCarousel(null);
                                               
                                                exchange.getIn().setBody(result);
        
                          //DTM_DATE
                                                            
                                           }
                                        })   
                                .when(simple("${header.adsbody[0].getAdlayoutid} == 4"))
                                        .to("sql:select * from ENG_AdCarousel where AdID =:#${header.AdID}?" +
                                        "dataSource=dataSource&"+"outputClass=io.fabric8.quickstarts.camel.addlayout")
                                        .process(new Processor() {
                                            public void process(Exchange exchange) throws Exception {
                                                addlayout results = exchange.getIn().getBody(addlayout.class);
                                                results.setLayout("Carousel");

                                                ads result=exchange.getIn().getHeader("adsbody",ads.class);
                                            
                                                result.setCarousel(results);
                                                result.setText(null);
                                                result.setImage(null);
                                                result.setVideo(null);
                                               
                                                exchange.getIn().setBody(result);
        
                          //DTM_DATE
                                                            
                                           }
                                        })   
                             
                                .end()
              




                                // private int  id;
                                // private String adposter;
                                // private int budget;
                                // private int reach;
                                // private String status;
                                // private int advertiserid;
                                // private Boolean chargeonimpression;
                                // private Boolean chargeonclick;
                                // private int adlayoutid;
                    .endRest()
                .post("op-configuration").description("Query The payment for specified operator")
                    .route().routeId("operator-configuration-insertion--api-opcode")
                    .setHeader("json",simple("${body}"))
                 .to("direct:Auth")
                    // .choice()
                    //    .when(simple("${body.getResult} != 'You are UnAuthrized to access such API'"))
                     //  .log("${body.getResult}")
                   .setBody(simple("${header.json}"))
                    .to("direct:start")             
                    .endRest()
                .get("QueryOpConfig/{op_code}/").description("Query The operator config for specified operator")
                    .route().routeId("operator-config-rate-api-opcode")
                    .to("sql:select * from op_config where op_code =:#${header.op_code}?" +
                            "dataSource=dataSource&" +
                            "outputClass=io.fabric8.quickstarts.camel.Opconfig")
                    .endRest();

                    from("direct:start").routeId("rollback2")
                    .transacted("PROPAGATION_REQUIRED")
                    .setHeader("CamelSqlRetrieveGeneratedKeys",simple("true", Boolean.class))
                      .doTry()
                          .marshal().json(JsonLibrary.Jackson,Opconfig.class)
                              .unmarshal().json(JsonLibrary.Jackson,Opconfig.class)
                              .setHeader("ops",simple("${body}"))
                              .to("sql:INSERT INTO op_config ( operator_code, operator_name, phone, email, cmc_coe_ratio_local, "
                              + "cmc_coe_ratio_roaming, operator_tax, payment_type_id, username, note) "
                              +"values(:#${body.getOperator_code},:#${body.getOperator_name},:#${body.getPhone},:#${body.getEmail},:#${body.getCmc_coe_ratio_local},:#${body.getCmc_coe_ratio_roaming},:#${body.getOperator_tax}"+
                              ",:#${body.getPayment_type_id},:#${body.getUsername},:#${body.getNote})?" +
                                      "dataSource=dataSource1")
                          .setBody(simple("${header.ops}"))
                          .setHeader("op_id",simple("${header.CamelSqlGeneratedKeyRows}"))
                          .process(new Processor() {
                                          public void process(Exchange exchange) throws Exception {
                                              ArrayList results = exchange.getIn().getHeader("op_id",ArrayList.class);
                                              Map test = (Map)results.get(0);
                                              exchange.getIn().setHeader("op_id_decode", test.get("GENERATED_KEY"));
                                              
                                          }
                                      })
                          .log("ya salam"+"${header.op_id_decode}")
                              .to("sql:INSERT INTO bank_details ( bank_code, bank_name, branch, phone, email, username)"                   
                              +"values(:#${body.getBank_code},:#${body.getBank_name},:#${body.getBranch},:#${body.getPhone},:#${body.getEmail},:#${body.getUsername}"+
                              ")?" +
                                      "dataSource=dataSource1")
                              .setHeader("bank_id",simple("${header.CamelSqlGeneratedKeyRows}"))
                              .setBody(simple("${header.ops}"))
                              .process(new Processor() {
                                  public void process(Exchange exchange) throws Exception {
                                      ArrayList results = exchange.getIn().getHeader("bank_id",ArrayList.class);
                                      Map test = (Map)results.get(0);
                                      exchange.getIn().setHeader("bank_id_decode", test.get("GENERATED_KEY"));
                                      
                              }
                              })

                              .to("sql:insert into operator_banking_accounts (operator_id, bank_id, account_number,currency, username)"                   
                              +"values(:#${header.op_id_decode},:#${header.bank_id_decode},:#${body.getAccount_number},:#${body.getCurrency},:#${body.getUsername}"+
                              ")?" +
                                      "dataSource=dataSource1")

                              .setBody(simple("${header.CamelSqlGeneratedKeyRows}"))
                      .doCatch(Exception.class)
                              .process(new Processor() {
                                  @Override
                                  public void process(Exchange exchange) throws Exception {
                                      final Throwable ex = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Throwable.class);
                                    //  exchange.getIn().setBody("{Exception occured :"+ex.getMessage()+"}");
                                      throw new Exception("{Exception occured :"+ex.getMessage()+"}");


                                  }
                              })
                      .end();

                from("direct:Auth").routeId("Auth")
                      .to("log:DEBUG?showBody=true&showHeaders=true")
                      .setHeader(Exchange.HTTP_METHOD, constant("GET")) 
                      .setHeader("Content-Type", constant("application/json"))
                      .setHeader("Accept", constant("application/json"))
                      .setHeader("CamelHttpUrl",constant("http://localhost:5000/api/user"))
                      .setHeader("CamelServletContextPath",constant(""))
                      .setHeader("host",constant("localhost"))
                      .setHeader("proto",constant("http"))
                      .setHeader("Host",constant("localhost"))
                      .setBody(constant(""))
  
                      .to("http://localhost:5000/api/user"+"?bridgeEndpoint=true");
                
                    //   .log("atiato"+"${body}")
                    //   .log("log:DEBUG?showBody=true&showHeaders=true");
                
        }
    }	

    
    
    // @Bean(name = "OracledataSource")
    // @ConfigurationProperties(prefix="test.datasource")
    // public DataSource dataSource() {
    //     return DataSourceBuilder.create().build();
    // }
    
    @Bean(name = "mySQLdataSource")
    @Primary
    @ConfigurationProperties(prefix="spring.datasource")
    public DataSource testdataSource() {
        return DataSourceBuilder.create().build();
    }
    
   
   
    @Bean(name = "dataSource")
    @ConfigurationProperties(prefix="spring.datasource")
    public DataSource dataSource2() {
    
       

        return DataSourceBuilder.create().build();
    }
   
    
    @Component
    class Backend extends RouteBuilder {

        @Override
        public void configure() {
        	JacksonDataFormat df = new JacksonDataFormat(Root.class);
            df.disableFeature(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            df.disableFeature(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES);
            
            
        	//df.setUseList(true);

        	
//            from("sql:select * from bill_cycle where is_predicted = 'no'?" +
//                    "consumer.onConsume=update bill_cycle set is_predicted = 'yes' where id = :#tid&" +
//                    "consumer.delay={{quickstart.processOrderPeriod:5s}}&" +
//                    "dataSource=dataSource&"+"outputClass=io.fabric8.quickstarts.camel.BillCycle")
//                .routeId("processing-prediction-for-billcycle").log("${body.id}")
//                .setHeader("tid",simple("${body.id}"))
//                .setHeader("QueryResult",simple("${body}"))
//                .setHeader("Content-Type", constant("application/x-www-form-urlencoded"))
//                .setHeader("Accept", constant("application/x-www-form-urlencoded"))
//                .setHeader(Exchange.HTTP_METHOD, constant("POST")) 
//            
//
//                .to("log:DEBUG?showBody=true&showHeaders=true")
//                .setBody(constant("username=admin&password=Oa03216287@"))
//                .log("${body}")
//                .to("http://localhost:8089/services/auth/login")
//                .split().xpath("//response/sessionKey/text()")
//                .log("Inserted new order ${body}")
//                .setHeader("Content-Type", constant("application/x-www-form-urlencoded"))
//                .setHeader("Accept", constant("application/x-www-form-urlencoded"))
//                .setHeader(Exchange.HTTP_METHOD, constant("POST")) 
//                .setHeader("Authorization",simple("Splunk ${body}"))
//                .log("${header.Authorization}")
//                .setBody(constant("search=search%20index%3Dinvoices%20%7C%20eval%20_time%3Dstrptime(from_date%2C%20%22%25Y-%25m-%25d%22)%20%7C%20timechart%20span%3D1mon%20values(invoice_amount)%20as%20invoice_amount%20%7C%20predict%20%22invoice_amount%22%20as%20prediction%20algorithm%3DLLP5%20holdback%3D0%20future_timespan%3D5%20upper0%3Dupper0%20lower0%3Dlower0%20%7C%20%60forecastviz(5%2C%200%2C%20%22invoice_amount%22%2C%200)%60"))
//            .log("${body}")
//            .to("http://localhost:8089/servicesNS/admin/Splunk_ML_Toolkit/search/jobs")
//            .split().xpath("//response/sid/text()")
//            .log("Inserted new sid ${body}")
//            .setHeader("sid",simple("${body}"))
//            .setHeader("Content-Type", constant("application/x-www-form-urlencoded"))
//            .setHeader("Accept", constant("application/x-www-form-urlencoded"))
//            .setHeader(Exchange.HTTP_METHOD, constant("GET")) 
//            .delay(10000)
//            .toD("http://localhost:8089/servicesNS/admin/Splunk_ML_Toolkit/search/jobs/${header.sid}/results/?output_mode=json")
//            .unmarshal(df)
//            
//            .process(new Processor() {
//                public void process(Exchange exchange) throws Exception {
//                    Root output = (Root)exchange.getIn().getBody(Root.class);
//                    BillCycle predictdate = (BillCycle)exchange.getIn().getHeader("QueryResult");
//                          
//                    for (int i=0;i<output.getResults().size();i++) {
//                   
//                    	if(output.getResults().get(i).get_time().toString().equals(predictdate.getPredict_start_date().toString()))
//                    	{
//                    		log.info("result"+output.getResults().get(i).get_time().toString()+"prediction"+output.getResults().get(i).getPrediction()+"span"+output.getResults().get(i).get_spandays());
//                            exchange.getIn().setHeader("_time",predictdate.getPredict_start_date());
//                            exchange.getIn().setHeader("prediction",Double.parseDouble(output.getResults().get(i).getPrediction()));
//                            if(output.getResults().get(i).get_spandays()!=null)
//                            {
//                                exchange.getIn().setHeader("future_span", Integer.parseInt(output.getResults().get(i).get_spandays()));
//
//	
//                            }else exchange.getIn().setHeader("future_span", Integer.parseInt("0"));
//
//
//                    		break;	
//                    	}
//                    	
//                    	
//                    
//                    }
//
//                    exchange.getIn().setHeader("op_code",predictdate.getOperator_id());
//
//                    
//               }
//            })
//            .to("sql:insert into predict_revenue (id,operator_id,from_date,to_date,future_span,predicted_amount,username) values " +
//                    "(1,:#${header.op_code} , :#${header._time},sysdate, :#${header.future_span}, :#${header.prediction},'atiato')?" +
//                    "dataSource=dataSource")
//            
//            .log("inserted time ${header._time}")
//
//            ;
//

//             from("sql:select rowid ,CALL_DATE ,ADJUSTMENT_IND ,REVENUE_MONTH ,SUB_TYPE ,CALL_TYPE ,SERVICE_TYPE ,CALL_CLASS ,CNT ,DURATION ,DATA_VOLUME ,CHARGED_AMOUNT , PARTNUM ,DTM_DATE ,DTM_DAY_NO , IS_APPROVED  from FACT$IN_OPERATOR1$D where revenue_month='201908' and is_approved = 'no'?" +
//                 "consumer.onConsume=update FACT$IN_OPERATOR1$D set is_approved = 'yes' where ROWID = :#oraclerowid&" +
//                 "consumer.delay={{quickstart.processOrderPeriod:5s}}&" +
//                 "dataSource=OracledataSource")//&"+"outputClass=io.fabric8.quickstarts.camel.RevenueMonth")
//                 .routeId("generate-csv-for-indexing-in-splunk")
//                 .process(new Processor() {
//                   public void process(Exchange exchange) throws Exception {
//                   	Map results = exchange.getIn().getBody(Map.class);
//                   	exchange.getIn().setHeader("oraclerowid", results.get("ROWID"));
//                   	exchange.getIn().setHeader("CALL_DATE", results.get("CALL_DATE"));
//                   	exchange.getIn().setHeader("ADJUSTMENT_IND", results.get("ADJUSTMENT_IND"));
//                   	exchange.getIn().setHeader("REVENUE_MONTH", results.get("REVENUE_MONTH"));
//                   	exchange.getIn().setHeader("SUB_TYPE", results.get("SUB_TYPE"));
//                   	exchange.getIn().setHeader("CALL_TYPE", results.get("CALL_TYPE"));
//                   	exchange.getIn().setHeader("SERVICE_TYPE", results.get("SERVICE_TYPE"));
//                   	exchange.getIn().setHeader("CALL_CLASS", results.get("CALL_CLASS"));
//                   	exchange.getIn().setHeader("CNT", results.get("CNT"));
//                   	exchange.getIn().setHeader("DURATION", results.get("DURATION"));
//                   	exchange.getIn().setHeader("DATA_VOLUME", results.get("DATA_VOLUME"));
//                   	exchange.getIn().setHeader("CHARGED_AMOUNT", results.get("CHARGED_AMOUNT"));
//                   	exchange.getIn().setHeader("PARTNUM", results.get("PARTNUM"));
//                   	exchange.getIn().setHeader("DTM_DAY_NO", results.get("DTM_DAY_NO"));
// //DTM_DATE
//                   	exchange.getIn().setHeader("DTM_DATE", results.get("DTM_DATE"));                      
//                  }
//               })      
//                 .to("sql:insert into factin_operator1d (CALL_DATE,ADJUSTMENT_IND,REVENUE_MONTH,SUB_TYPE,CALL_TYPE,SERVICE_TYPE,CALL_CLASS,CNT,DURATION ,DATA_VOLUME ,CHARGED_AMOUNT , PARTNUM ,DTM_DATE ,DTM_DAY_NO) values " +
//                      "(:#${header.CALL_DATE} , :#${header.ADJUSTMENT_IND},:#${header.REVENUE_MONTH}, :#${header.SUB_TYPE}, :#${header.CALL_TYPE},:#${header.SERVICE_TYPE},:#${header.CALL_CLASS},:#${header.CNT},:#${header.DURATION},:#${header.DATA_VOLUME},:#${header.CHARGED_AMOUNT},:#${header.PARTNUM},:#${header.DTM_DATE},:#${header.DTM_DAY_NO})?" +
//                      "dataSource=mySQLdataSource")
            
//                  .to("log:DEBUG?showBody=true&showHeaders=true");

              //  .setHeader("rowid",simple("${header.oraclerowid}"))
                //.log("Processed order #id ${body.id}")
           //     .marshal().csv() 
           //     .to("file:target/reports/?fileName=oracle.txt&fileExist=Append");
            
//            from("timer:new-order?delay=1s&period={{quickstart.generateOrderPeriod:2s}}")
//            .routeId("atiato-proocedure")
//            .to("sql-stored:tryitfromcamel(OUT DOUBLE span_out)")
//            .to("log:DEBUG?showBody=true&showHeaders=true")
//            .process(new Processor() {
//                public void process(Exchange exchange) throws Exception {
//                	Map results = exchange.getIn().getBody(Map.class);
//                	exchange.getIn().setHeader("span_out", results.get("span_out"));
//                    
//               }
//            })
//        
//   
//            .log("Inserted out of proecedure order ${header.span_out}");

        }
    }
}
