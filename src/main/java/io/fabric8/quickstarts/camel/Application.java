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
import java.util.List;
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
@ImportResource({ "classpath:spring/camel-context.xml" })
public class Application extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    ServletRegistrationBean servletRegistrationBean() {
        ServletRegistrationBean servlet = new ServletRegistrationBean(new CamelHttpTransportServlet(),
                "/camel-rest-sql/*");
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
                    // exchange.getIn().setBody("{Exception occured :"+ex.getMessage()+"}");
                    UnAuthorize test = new UnAuthorize();
                    test.setResult("You are UnAuthrized to access such API");
                    exchange.getIn().setBody(test);
                }
            });

            restConfiguration()

                    .contextPath("/api/mobile").apiContextPath("/api-doc").apiProperty("api.title", "Camel REST API")
                    .apiProperty("api.version", "1.0").apiProperty("cors", "true").apiContextRouteId("doc-api")
                    .component("servlet").bindingMode(RestBindingMode.json);

            rest("/").description("Exhange Rate REST service")

                    .get("/getAd").description("get add GET API.").param().name("id").type(RestParamType.query)
                    .dataType("String").endParam().param().name("Country").type(RestParamType.query).dataType("String")
                    .endParam().route().log("${header.id}" + "the country ${header.Country}")
                    .to("sql:select * from ACC_ProfileIMEI where IMEI =:#${header.id}?" + "dataSource=dataSource&"
                            + "outputClass=io.fabric8.quickstarts.camel.ProfileIMEI")
                    .log("Get profile ID" + "${body[0].getProfileID}")
                    .to("sql:select ID,GenderID,IMEI_ID,MobileNumber,OptOut,Name,CountryID from ACC_UserProfile where ID =:#${body[0].getProfileID}?"
                            + "dataSource=dataSource&" + "outputClass=io.fabric8.quickstarts.camel.CurrentProfile")
                    .log("Get ID" + "${body[0].getID}")
                    .to("sql-stored:ENG_GetAd(INTEGER ${body[0].getID})?" + "dataSource=dataSource")
                    .to("log:DEBUG?showBody=true&showHeaders=true").process(new Processor() {
                        public void process(Exchange exchange) throws Exception {
                            Map results = exchange.getIn().getBody(Map.class);
                            // ArrayList results = exchange.getIn().getHeader("op_id",ArrayList.class);

                            ArrayList out = (ArrayList) results.get("#result-set-1");
                            Map finalresult = (Map) out.get(0);
                            exchange.getIn().setHeader("AdID", finalresult.get("AdID"));

                        }
                    }).log("headerID" + "${header.AdID}")
                    .to("sql:select id,adposter,budget,reach,status,advertiserid,chargeonimpression,chargeonclick,adlayoutid,isinvasive from ENG_Ads where ID =:#${header.AdID}?"
                            + "dataSource=dataSource&" + "outputClass=io.fabric8.quickstarts.camel.ads")
                    .setHeader("adsbody", simple("${body}"))
                    .to("sql:select * from ENG_Advertisers where ID =:#${body[0].getAdvertiserid}?"
                            + "dataSource=dataSource")// +
                    .to("log:DEBUG?showBody=true&showHeaders=true")

                    .process(new Processor() {
                        public void process(Exchange exchange) throws Exception {
                            ArrayList results = exchange.getIn().getBody(ArrayList.class);
                            ads result = exchange.getIn().getHeader("adsbody", ads.class);
                            Map out = (Map) results.get(0);
                            result.setAdvertiserName((String) out.get("Name"));
                            result.setAdvertiserImage("" + exchange.getIn().getHeader("Host")
                                    + "/content/AdvertiserImages/" + (String) out.get("Image"));

                            exchange.getIn().setBody(result);

                        }
                    }).choice().when(simple("${header.adsbody[0].getAdlayoutid} == 1"))
                    .to("sql:select * from ENG_AdText where AdID =:#${header.AdID}?" + "dataSource=dataSource&"
                            + "outputClass=io.fabric8.quickstarts.camel.addlayout")
                    .process(new Processor() {
                        public void process(Exchange exchange) throws Exception {
                            addlayout results = exchange.getIn().getBody(addlayout.class);
                            results.setLayout("Text");
                            ads result = exchange.getIn().getHeader("adsbody", ads.class);

                            result.setText(results);
                            result.setImage(null);
                            result.setVideo(null);
                            result.setCarousel(null);

                            exchange.getIn().setBody(result);

                        }
                    }).when(simple("${header.adsbody[0].getAdlayoutid} == 2"))
                    .to("sql:select * from ENG_AdImage where AdID =:#${header.AdID}?" + "dataSource=dataSource&"
                            + "outputClass=io.fabric8.quickstarts.camel.addlayout")

                    .process(new Processor() {
                        public void process(Exchange exchange) throws Exception {
                            addlayout results = exchange.getIn().getBody(addlayout.class);
                            results.setLayout("Image");

                            ads result = exchange.getIn().getHeader("adsbody", ads.class);

                            result.setImage(results);
                            result.setText(null);
                            result.setVideo(null);
                            result.setCarousel(null);

                            exchange.getIn().setBody(result);

                        }
                    }).when(simple("${header.adsbody[0].getAdlayoutid} == 3"))
                    .to("sql:select * from ENG_AdVideo where AdID =:#${header.AdID}?" + "dataSource=dataSource&"
                            + "outputClass=io.fabric8.quickstarts.camel.addlayout")
                    .process(new Processor() {
                        public void process(Exchange exchange) throws Exception {
                            addlayout results = exchange.getIn().getBody(addlayout.class);
                            results.setLayout("Video");

                            ads result = exchange.getIn().getHeader("adsbody", ads.class);

                            result.setVideo(results);
                            result.setText(null);
                            result.setImage(null);
                            result.setCarousel(null);

                            exchange.getIn().setBody(result);

                        }
                    }).when(simple("${header.adsbody[0].getAdlayoutid} == 4"))
                    .to("sql:select * from ENG_AdCarousel where AdID =:#${header.AdID}?" + "dataSource=dataSource&"
                            + "outputClass=io.fabric8.quickstarts.camel.addlayout")
                    .process(new Processor() {
                        public void process(Exchange exchange) throws Exception {
                            addlayout results = exchange.getIn().getBody(addlayout.class);
                            exchange.getIn().setHeader("carousal", exchange.getIn().getBody(addlayout.class));
                            results.setLayout("Carousel");

                            ads result = exchange.getIn().getHeader("adsbody", ads.class);

                            result.setCarousel(results);
                            result.setText(null);
                            result.setImage(null);
                            result.setVideo(null);

                            exchange.getIn().setBody(result);
                            exchange.getIn().setHeader("ads", result);

                        }
                    }).log("testing-------" + "${header.carousal[0].getAdID}")
                    .to("sql:select * from ENG_CarouselItems where CarousselID =:#${header.carousal[0].getID}?"
                            + "dataSource=dataSource&" + "outputClass=io.fabric8.quickstarts.camel.CarouselItems")
                    .process(new Processor() {
                        public void process(Exchange exchange) throws Exception {
                            ArrayList<CarouselItems> results = (ArrayList) exchange.getIn().getBody(ArrayList.class);
                            ads out = exchange.getIn().getHeader("ads", ads.class);
                            out.setCarouselItems(results);

                            exchange.getIn().setBody(out);

                        }
                    }).end()

                    .endRest();

            from("sql:select ID from ENG_Ads where AdLayoutID=1?"
                    + "consumer.onConsume=update ENG_Ads set scoring=:#scoring where ID=:#Ad_ID&" + "consumer.delay=5s&"
                    + "dataSource=dataSource").process(new Processor() {

                        @Override
                        public void process(Exchange exchange) throws Exception {
                            // TODO Auto-generated method stub
                            Map results = exchange.getIn().getBody(Map.class);
                            System.out.println("output Clicked" + results.get("ID"));

                            exchange.getIn().setHeader("Ad_ID", results.get("ID"));
                        }

                    }).to("log:DEBUG?showBody=true&showHeaders=true")

                            .to("sql:select count(*) As counter_clicked from BIll_AdsBilling a, ENG_Ads b where a.Action='clicked' and b.AdLayoutID=1 and a.Ad_ID=:#Ad_ID?dataSource=dataSource")
                            .to("log:DEBUG?showBody=true&showHeaders=true").process(new Processor() {

                                @Override
                                public void process(Exchange exchange) throws Exception {
                                    // TODO Auto-generated method stub
                                    List results = exchange.getIn().getBody(List.class);
                                    Map test = (Map) results.get(0);
                                    System.out.println("output Clicked" + test.get("counter_clicked"));

                                    exchange.getIn().setHeader("count-clicked-text", test.get("counter_clicked"));
                                }

                            }).to("log:DEBUG?showBody=true&showHeaders=true")

                            .to("sql:select count(*) As counter_viewed from BIll_AdsBilling a, ENG_Ads b where a.Action='Viewed' and b.AdLayoutID=1 and a.Ad_ID=:#Ad_ID?dataSource=dataSource")
                            .to("log:DEBUG?showBody=true&showHeaders=true").process(new Processor() {

                                @Override
                                public void process(Exchange exchange) throws Exception {
                                    // TODO Auto-generated method stub
                                    List results = exchange.getIn().getBody(List.class);
                                    Map test = (Map) results.get(0);
                                    System.out.println("output Viewed" + test.get("counter_viewed"));

                                    exchange.getIn().setHeader("count-viewed-text", test.get("counter_viewed"));
                                }

                            }).to("log:DEBUG?showBody=true&showHeaders=true")

                            .to("sql:select count(*) As counter_Imp from BIll_AdsBilling a, ENG_Ads b where a.Action='Impression' and b.AdLayoutID=1?dataSource=dataSource")
                            .to("log:DEBUG?showBody=true&showHeaders=true").process(new Processor() {

                                @Override
                                public void process(Exchange exchange) throws Exception {
                                    // TODO Auto-generated method stub
                                    List results = exchange.getIn().getBody(List.class);
                                    Map test = (Map) results.get(0);
                                    System.out.println("output Impression" + test.get("counter_Imp"));

                                    exchange.getIn().setHeader("Total-Impressions-text", test.get("counter_Imp"));
                                }

                            }).to("log:DEBUG?showBody=true&showHeaders=true")

                            .process(new Processor() {

                                @Override
                                public void process(Exchange exchange) throws Exception {
                                    // TODO Auto-generated method stub

                                    int Imp = (int) exchange.getIn().getHeader("Total-Impressions-text");
                                    int viewed = (int) exchange.getIn().getHeader("count-viewed-text");
                                    int clicked = (int) exchange.getIn().getHeader("count-clicked-text");

                                    float image_scoring = (float) (viewed + clicked) / (2 * Imp);
                                    exchange.getIn().setHeader("scoring", image_scoring);
                                    System.out.println("Impressions" + Imp + "viewed " + viewed + "clicked " + clicked
                                            + " Scoring is " + image_scoring + " for the Ad_ID "
                                            + exchange.getIn().getHeader("Ad_ID") + "\n");
                                }

                            }).to("log:DEBUG?showBody=true&showHeaders=true");

            from("sql:select ID from ENG_Ads where AdLayoutID=2?"
                    + "consumer.onConsume=update ENG_Ads set scoring=:#scoring where ID=:#Ad_ID&" + "consumer.delay=5s&"
                    + "dataSource=dataSource").process(new Processor() {

                        @Override
                        public void process(Exchange exchange) throws Exception {
                            // TODO Auto-generated method stub
                            Map results = exchange.getIn().getBody(Map.class);
                            System.out.println("output Clicked" + results.get("ID"));

                            exchange.getIn().setHeader("Ad_ID", results.get("ID"));
                        }

                    }).to("log:DEBUG?showBody=true&showHeaders=true")

                            .to("sql:select count(*) As counter_clicked from BIll_AdsBilling a, ENG_Ads b where a.Action='clicked' and b.AdLayoutID=2 and a.Ad_ID=:#Ad_ID?dataSource=dataSource")
                            .to("log:DEBUG?showBody=true&showHeaders=true").process(new Processor() {

                                @Override
                                public void process(Exchange exchange) throws Exception {
                                    // TODO Auto-generated method stub
                                    List results = exchange.getIn().getBody(List.class);
                                    Map test = (Map) results.get(0);
                                    System.out.println("output Clicked" + test.get("counter_clicked"));

                                    exchange.getIn().setHeader("count-clicked-image", test.get("counter_clicked"));
                                }

                            }).to("log:DEBUG?showBody=true&showHeaders=true")

                            .to("sql:select count(*) As counter_viewed from BIll_AdsBilling a, ENG_Ads b where a.Action='Viewed' and b.AdLayoutID=2 and a.Ad_ID=:#Ad_ID?dataSource=dataSource")
                            .to("log:DEBUG?showBody=true&showHeaders=true").process(new Processor() {

                                @Override
                                public void process(Exchange exchange) throws Exception {
                                    // TODO Auto-generated method stub
                                    List results = exchange.getIn().getBody(List.class);
                                    Map test = (Map) results.get(0);
                                    System.out.println("output Viewed" + test.get("counter_viewed"));

                                    exchange.getIn().setHeader("count-viewed-image", test.get("counter_viewed"));
                                }

                            }).to("log:DEBUG?showBody=true&showHeaders=true")

                            .to("sql:select count(*) As counter_Imp from BIll_AdsBilling a, ENG_Ads b where a.Action='Impression' and b.AdLayoutID=2?dataSource=dataSource")
                            .to("log:DEBUG?showBody=true&showHeaders=true").process(new Processor() {

                                @Override
                                public void process(Exchange exchange) throws Exception {
                                    // TODO Auto-generated method stub
                                    List results = exchange.getIn().getBody(List.class);
                                    Map test = (Map) results.get(0);
                                    System.out.println("output Impression" + test.get("counter_Imp"));

                                    exchange.getIn().setHeader("Total-Impressions-image", test.get("counter_Imp"));
                                }

                            }).to("log:DEBUG?showBody=true&showHeaders=true")

                            .process(new Processor() {

                                @Override
                                public void process(Exchange exchange) throws Exception {
                                    // TODO Auto-generated method stub

                                    int Imp = (int) exchange.getIn().getHeader("Total-Impressions-image");
                                    int viewed = (int) exchange.getIn().getHeader("count-viewed-image");
                                    int clicked = (int) exchange.getIn().getHeader("count-clicked-image");

                                    float image_scoring = (float) (viewed + clicked) / (2 * Imp);
                                    exchange.getIn().setHeader("scoring", image_scoring);
                                    System.out.println("Impressions" + Imp + "viewed " + viewed + "clicked " + clicked
                                            + " Scoring is " + image_scoring + " for the Ad_ID "
                                            + exchange.getIn().getHeader("Ad_ID") + "\n");
                                }

                            }).to("log:DEBUG?showBody=true&showHeaders=true");

            from("sql:select ID from ENG_Ads where AdLayoutID=4?"
                    + "consumer.onConsume=update ENG_Ads set scoring=:#scoring where ID=:#Ad_ID&" + "consumer.delay=5s&"
                    + "dataSource=dataSource").process(new Processor() {

                        @Override
                        public void process(Exchange exchange) throws Exception {
                            // TODO Auto-generated method stub
                            Map results = exchange.getIn().getBody(Map.class);
                            System.out.println("output Clicked" + results.get("ID"));

                            exchange.getIn().setHeader("Ad_ID", results.get("ID"));
                        }

                    }).to("log:DEBUG?showBody=true&showHeaders=true")

                            .to("sql:select count(*) As counter_clicked from BIll_AdsBilling a, ENG_Ads b where a.Action='clicked' and b.AdLayoutID=4 and a.Ad_ID=:#Ad_ID?dataSource=dataSource")
                            .to("log:DEBUG?showBody=true&showHeaders=true").process(new Processor() {

                                @Override
                                public void process(Exchange exchange) throws Exception {
                                    // TODO Auto-generated method stub
                                    List results = exchange.getIn().getBody(List.class);
                                    Map test = (Map) results.get(0);
                                    System.out.println("output Clicked" + test.get("counter_clicked"));

                                    exchange.getIn().setHeader("count-clicked-Carousal", test.get("counter_clicked"));
                                }

                            }).to("log:DEBUG?showBody=true&showHeaders=true")

                            .to("sql:select count(*) As counter_viewed from BIll_AdsBilling a, ENG_Ads b where a.Action='Viewed' and b.AdLayoutID=4 and a.Ad_ID=:#Ad_ID?dataSource=dataSource")
                            .to("log:DEBUG?showBody=true&showHeaders=true").process(new Processor() {

                                @Override
                                public void process(Exchange exchange) throws Exception {
                                    // TODO Auto-generated method stub
                                    List results = exchange.getIn().getBody(List.class);
                                    Map test = (Map) results.get(0);
                                    System.out.println("output Viewed" + test.get("counter_viewed"));

                                    exchange.getIn().setHeader("count-viewed-Carousal", test.get("counter_viewed"));
                                }

                            }).to("log:DEBUG?showBody=true&showHeaders=true")

                            .to("sql:select count(*) As counter_Imp from BIll_AdsBilling a, ENG_Ads b where a.Action='Impression' and b.AdLayoutID=4?dataSource=dataSource")
                            .to("log:DEBUG?showBody=true&showHeaders=true").process(new Processor() {

                                @Override
                                public void process(Exchange exchange) throws Exception {
                                    // TODO Auto-generated method stub
                                    List results = exchange.getIn().getBody(List.class);
                                    Map test = (Map) results.get(0);
                                    System.out.println("output Impression" + test.get("counter_Imp"));

                                    exchange.getIn().setHeader("Total-Impressions-Carousal", test.get("counter_Imp"));
                                }

                            }).to("log:DEBUG?showBody=true&showHeaders=true")

                            .process(new Processor() {

                                @Override
                                public void process(Exchange exchange) throws Exception {
                                    // TODO Auto-generated method stub

                                    int Imp = (int) exchange.getIn().getHeader("Total-Impressions-Carousal");
                                    int viewed = (int) exchange.getIn().getHeader("count-viewed-Carousal");
                                    int clicked = (int) exchange.getIn().getHeader("count-clicked-Carousal");

                                    float image_scoring = (float) (viewed + clicked) / (2 * Imp);
                                    exchange.getIn().setHeader("scoring", image_scoring);
                                    System.out.println("Impressions" + Imp + "viewed " + viewed + "clicked " + clicked
                                            + " Scoring is " + image_scoring + " for the Ad_ID "
                                            + exchange.getIn().getHeader("Ad_ID") + "\n");
                                }

                            }).to("log:DEBUG?showBody=true&showHeaders=true");

            from("sql:select ID from ENG_Ads where AdLayoutID=3?"
                    + "consumer.onConsume=update ENG_Ads set scoring=:#scoring where ID=:#Ad_ID&" + "consumer.delay=5s&"
                    + "dataSource=dataSource").process(new Processor() {

                        @Override
                        public void process(Exchange exchange) throws Exception {
                            // TODO Auto-generated method stub
                            Map results = exchange.getIn().getBody(Map.class);
                            System.out.println("output Clicked" + results.get("ID"));

                            exchange.getIn().setHeader("Ad_ID", results.get("ID"));
                        }

                    }).to("log:DEBUG?showBody=true&showHeaders=true")

                            .to("sql:select count(*) As counter_playedall from BIll_AdsBilling a, ENG_Ads b where a.Action='PlayedAll' and b.AdLayoutID=3 and a.Ad_ID=:#Ad_ID?dataSource=dataSource")
                            .to("log:DEBUG?showBody=true&showHeaders=true").process(new Processor() {

                                @Override
                                public void process(Exchange exchange) throws Exception {
                                    // TODO Auto-generated method stub
                                    List results = exchange.getIn().getBody(List.class);
                                    Map test = (Map) results.get(0);
                                    System.out.println("output PlayedAll" + test.get("counter_playedall"));

                                    exchange.getIn().setHeader("count-PlayedAll-Video", test.get("counter_playedall"));
                                }

                            }).to("log:DEBUG?showBody=true&showHeaders=true")

                            .to("sql:select count(*) As counter_play5sec from BIll_AdsBilling a, ENG_Ads b where a.Action='Played5Sec' and b.AdLayoutID=3 and a.Ad_ID=:#Ad_ID?dataSource=dataSource")
                            .to("log:DEBUG?showBody=true&showHeaders=true").process(new Processor() {

                                @Override
                                public void process(Exchange exchange) throws Exception {
                                    // TODO Auto-generated method stub
                                    List results = exchange.getIn().getBody(List.class);
                                    Map test = (Map) results.get(0);
                                    System.out.println("output Viewed" + test.get("counter_play5sec"));

                                    exchange.getIn().setHeader("count-Played5Sec-Video", test.get("counter_play5sec"));
                                }

                            }).to("log:DEBUG?showBody=true&showHeaders=true")

                            .to("sql:select count(*) As counter_clicked from BIll_AdsBilling a, ENG_Ads b where a.Action='clicked' and b.AdLayoutID=3 and a.Ad_ID=:#Ad_ID?dataSource=dataSource")
                            .to("log:DEBUG?showBody=true&showHeaders=true").process(new Processor() {

                                @Override
                                public void process(Exchange exchange) throws Exception {
                                    // TODO Auto-generated method stub
                                    List results = exchange.getIn().getBody(List.class);
                                    Map test = (Map) results.get(0);
                                    System.out.println("output Clicked" + test.get("counter_clicked"));

                                    exchange.getIn().setHeader("count-Clicked-Video", test.get("counter_clicked"));
                                }

                            }).to("log:DEBUG?showBody=true&showHeaders=true")

                            .to("sql:select count(*) As counter_Imp from BIll_AdsBilling a, ENG_Ads b where a.Action='Impression' and b.AdLayoutID=3?dataSource=dataSource")
                            .to("log:DEBUG?showBody=true&showHeaders=true").process(new Processor() {

                                @Override
                                public void process(Exchange exchange) throws Exception {
                                    // TODO Auto-generated method stub
                                    List results = exchange.getIn().getBody(List.class);
                                    Map test = (Map) results.get(0);
                                    System.out.println("output Impression" + test.get("counter_Imp"));

                                    exchange.getIn().setHeader("Total-Impressions-Video", test.get("counter_Imp"));
                                }

                            }).to("log:DEBUG?showBody=true&showHeaders=true")

                            .process(new Processor() {

                                @Override
                                public void process(Exchange exchange) throws Exception {
                                    // TODO Auto-generated method stub

                                    int Imp = (int) exchange.getIn().getHeader("Total-Impressions-Video");
                                    int played5sec = (int) exchange.getIn().getHeader("count-Played5Sec-Video");
                                    int clicked = (int) exchange.getIn().getHeader("count-Clicked-Video");
                                    int playedall=(int) exchange.getIn().getHeader("count-PlayedAll-Video");

                                    float image_scoring = (float) (played5sec + clicked) / (2 * Imp);
                                    float image_scoring_all = (float) (2*playedall + clicked) / (3 * Imp);
                                    float total_scoring =image_scoring+image_scoring_all;

                                    exchange.getIn().setHeader("scoring", total_scoring);
                                    System.out.println("Impressions" + Imp + "Played5sec " + played5sec + "playedall " + playedall
                                            + " Scoring is " + total_scoring + " for the Ad_ID "
                                            + exchange.getIn().getHeader("Ad_ID") + "\n");
                                }

                            }).to("log:DEBUG?showBody=true&showHeaders=true");

            from("sql:select ID from ENG_Agency_Ads where AdLayoutID=1?"
                    + "consumer.onConsume=update ENG_Agency_Ads set scoring=:#scoring where ID=:#Ad_ID&" + "consumer.delay=5s&"
                    + "dataSource=dataSource").process(new Processor() {

                        @Override
                        public void process(Exchange exchange) throws Exception {
                            // TODO Auto-generated method stub
                            Map results = exchange.getIn().getBody(Map.class);
                            System.out.println("output Clicked" + results.get("ID"));

                            exchange.getIn().setHeader("Ad_ID", results.get("ID"));
                        }

                    }).to("log:DEBUG?showBody=true&showHeaders=true")

                            .to("sql:select count(*) As counter_clicked from BIll_AdsBilling a, ENG_Agency_Ads b where a.Action='clicked' and b.AdLayoutID=1 and a.Ad_ID=:#Ad_ID?dataSource=dataSource")
                            .to("log:DEBUG?showBody=true&showHeaders=true").process(new Processor() {

                                @Override
                                public void process(Exchange exchange) throws Exception {
                                    // TODO Auto-generated method stub
                                    List results = exchange.getIn().getBody(List.class);
                                    Map test = (Map) results.get(0);
                                    System.out.println("output Clicked" + test.get("counter_clicked"));

                                    exchange.getIn().setHeader("count-clicked-text", test.get("counter_clicked"));
                                }

                            }).to("log:DEBUG?showBody=true&showHeaders=true")

                            .to("sql:select count(*) As counter_viewed from BIll_AdsBilling a, ENG_Agency_Ads b where a.Action='Viewed' and b.AdLayoutID=1 and a.Ad_ID=:#Ad_ID?dataSource=dataSource")
                            .to("log:DEBUG?showBody=true&showHeaders=true").process(new Processor() {

                                @Override
                                public void process(Exchange exchange) throws Exception {
                                    // TODO Auto-generated method stub
                                    List results = exchange.getIn().getBody(List.class);
                                    Map test = (Map) results.get(0);
                                    System.out.println("output Viewed" + test.get("counter_viewed"));

                                    exchange.getIn().setHeader("count-viewed-text", test.get("counter_viewed"));
                                }

                            }).to("log:DEBUG?showBody=true&showHeaders=true")

                            .to("sql:select count(*) As counter_Imp from BIll_AdsBilling a, ENG_Agency_Ads b where a.Action='Impression' and b.AdLayoutID=1?dataSource=dataSource")
                            .to("log:DEBUG?showBody=true&showHeaders=true").process(new Processor() {

                                @Override
                                public void process(Exchange exchange) throws Exception {
                                    // TODO Auto-generated method stub
                                    List results = exchange.getIn().getBody(List.class);
                                    Map test = (Map) results.get(0);
                                    System.out.println("output Impression" + test.get("counter_Imp"));

                                    exchange.getIn().setHeader("Total-Impressions-text", test.get("counter_Imp"));
                                }

                            }).to("log:DEBUG?showBody=true&showHeaders=true")

                            .process(new Processor() {

                                @Override
                                public void process(Exchange exchange) throws Exception {
                                    // TODO Auto-generated method stub

                                    int Imp = (int) exchange.getIn().getHeader("Total-Impressions-text");
                                    int viewed = (int) exchange.getIn().getHeader("count-viewed-text");
                                    int clicked = (int) exchange.getIn().getHeader("count-clicked-text");

                                    float image_scoring = (float) (viewed + clicked) / (2 * Imp);
                                    exchange.getIn().setHeader("scoring", image_scoring);
                                    System.out.println("Impressions" + Imp + "viewed " + viewed + "clicked " + clicked
                                            + " Scoring is " + image_scoring + " for the Ad_ID "
                                            + exchange.getIn().getHeader("Ad_ID") + "\n");
                                }

                            }).to("log:DEBUG?showBody=true&showHeaders=true");

            from("sql:select ID from ENG_Agency_Ads where AdLayoutID=2?"
                    + "consumer.onConsume=update ENG_Agency_Ads set scoring=:#scoring where ID=:#Ad_ID&" + "consumer.delay=5s&"
                    + "dataSource=dataSource").process(new Processor() {

                        @Override
                        public void process(Exchange exchange) throws Exception {
                            // TODO Auto-generated method stub
                            Map results = exchange.getIn().getBody(Map.class);
                            System.out.println("output Clicked" + results.get("ID"));

                            exchange.getIn().setHeader("Ad_ID", results.get("ID"));
                        }

                    }).to("log:DEBUG?showBody=true&showHeaders=true")

                            .to("sql:select count(*) As counter_clicked from BIll_AdsBilling a, ENG_Agency_Ads b where a.Action='clicked' and b.AdLayoutID=2 and a.Ad_ID=:#Ad_ID?dataSource=dataSource")
                            .to("log:DEBUG?showBody=true&showHeaders=true").process(new Processor() {

                                @Override
                                public void process(Exchange exchange) throws Exception {
                                    // TODO Auto-generated method stub
                                    List results = exchange.getIn().getBody(List.class);
                                    Map test = (Map) results.get(0);
                                    System.out.println("output Clicked" + test.get("counter_clicked"));

                                    exchange.getIn().setHeader("count-clicked-image", test.get("counter_clicked"));
                                }

                            }).to("log:DEBUG?showBody=true&showHeaders=true")

                            .to("sql:select count(*) As counter_viewed from BIll_AdsBilling a, ENG_Agency_Ads b where a.Action='Viewed' and b.AdLayoutID=2 and a.Ad_ID=:#Ad_ID?dataSource=dataSource")
                            .to("log:DEBUG?showBody=true&showHeaders=true").process(new Processor() {

                                @Override
                                public void process(Exchange exchange) throws Exception {
                                    // TODO Auto-generated method stub
                                    List results = exchange.getIn().getBody(List.class);
                                    Map test = (Map) results.get(0);
                                    System.out.println("output Viewed" + test.get("counter_viewed"));

                                    exchange.getIn().setHeader("count-viewed-image", test.get("counter_viewed"));
                                }

                            }).to("log:DEBUG?showBody=true&showHeaders=true")

                            .to("sql:select count(*) As counter_Imp from BIll_AdsBilling a, ENG_Agency_Ads b where a.Action='Impression' and b.AdLayoutID=2?dataSource=dataSource")
                            .to("log:DEBUG?showBody=true&showHeaders=true").process(new Processor() {

                                @Override
                                public void process(Exchange exchange) throws Exception {
                                    // TODO Auto-generated method stub
                                    List results = exchange.getIn().getBody(List.class);
                                    Map test = (Map) results.get(0);
                                    System.out.println("output Impression" + test.get("counter_Imp"));

                                    exchange.getIn().setHeader("Total-Impressions-image", test.get("counter_Imp"));
                                }

                            }).to("log:DEBUG?showBody=true&showHeaders=true")

                            .process(new Processor() {

                                @Override
                                public void process(Exchange exchange) throws Exception {
                                    // TODO Auto-generated method stub

                                    int Imp = (int) exchange.getIn().getHeader("Total-Impressions-image");
                                    int viewed = (int) exchange.getIn().getHeader("count-viewed-image");
                                    int clicked = (int) exchange.getIn().getHeader("count-clicked-image");

                                    float image_scoring = (float) (viewed + clicked) / (2 * Imp);
                                    exchange.getIn().setHeader("scoring", image_scoring);
                                    System.out.println("Impressions" + Imp + "viewed " + viewed + "clicked " + clicked
                                            + " Scoring is " + image_scoring + " for the Ad_ID "
                                            + exchange.getIn().getHeader("Ad_ID") + "\n");
                                }

                            }).to("log:DEBUG?showBody=true&showHeaders=true");

            from("sql:select ID from ENG_Agency_Ads where AdLayoutID=4?"
                    + "consumer.onConsume=update ENG_Agency_Ads set scoring=:#scoring where ID=:#Ad_ID&" + "consumer.delay=5s&"
                    + "dataSource=dataSource").process(new Processor() {

                        @Override
                        public void process(Exchange exchange) throws Exception {
                            // TODO Auto-generated method stub
                            Map results = exchange.getIn().getBody(Map.class);
                            System.out.println("output Clicked" + results.get("ID"));

                            exchange.getIn().setHeader("Ad_ID", results.get("ID"));
                        }

                    }).to("log:DEBUG?showBody=true&showHeaders=true")

                            .to("sql:select count(*) As counter_clicked from BIll_AdsBilling a, ENG_Agency_Ads b where a.Action='clicked' and b.AdLayoutID=4 and a.Ad_ID=:#Ad_ID?dataSource=dataSource")
                            .to("log:DEBUG?showBody=true&showHeaders=true").process(new Processor() {

                                @Override
                                public void process(Exchange exchange) throws Exception {
                                    // TODO Auto-generated method stub
                                    List results = exchange.getIn().getBody(List.class);
                                    Map test = (Map) results.get(0);
                                    System.out.println("output Clicked" + test.get("counter_clicked"));

                                    exchange.getIn().setHeader("count-clicked-Carousal", test.get("counter_clicked"));
                                }

                            }).to("log:DEBUG?showBody=true&showHeaders=true")

                            .to("sql:select count(*) As counter_viewed from BIll_AdsBilling a, ENG_Agency_Ads b where a.Action='Viewed' and b.AdLayoutID=4 and a.Ad_ID=:#Ad_ID?dataSource=dataSource")
                            .to("log:DEBUG?showBody=true&showHeaders=true").process(new Processor() {

                                @Override
                                public void process(Exchange exchange) throws Exception {
                                    // TODO Auto-generated method stub
                                    List results = exchange.getIn().getBody(List.class);
                                    Map test = (Map) results.get(0);
                                    System.out.println("output Viewed" + test.get("counter_viewed"));

                                    exchange.getIn().setHeader("count-viewed-Carousal", test.get("counter_viewed"));
                                }

                            }).to("log:DEBUG?showBody=true&showHeaders=true")

                            .to("sql:select count(*) As counter_Imp from BIll_AdsBilling a, ENG_Agency_Ads b where a.Action='Impression' and b.AdLayoutID=4?dataSource=dataSource")
                            .to("log:DEBUG?showBody=true&showHeaders=true").process(new Processor() {

                                @Override
                                public void process(Exchange exchange) throws Exception {
                                    // TODO Auto-generated method stub
                                    List results = exchange.getIn().getBody(List.class);
                                    Map test = (Map) results.get(0);
                                    System.out.println("output Impression" + test.get("counter_Imp"));

                                    exchange.getIn().setHeader("Total-Impressions-Carousal", test.get("counter_Imp"));
                                }

                            }).to("log:DEBUG?showBody=true&showHeaders=true")

                            .process(new Processor() {

                                @Override
                                public void process(Exchange exchange) throws Exception {
                                    // TODO Auto-generated method stub

                                    int Imp = (int) exchange.getIn().getHeader("Total-Impressions-Carousal");
                                    int viewed = (int) exchange.getIn().getHeader("count-viewed-Carousal");
                                    int clicked = (int) exchange.getIn().getHeader("count-clicked-Carousal");

                                    float image_scoring = (float) (viewed + clicked) / (2 * Imp);
                                    exchange.getIn().setHeader("scoring", image_scoring);
                                    System.out.println("Impressions" + Imp + "viewed " + viewed + "clicked " + clicked
                                            + " Scoring is " + image_scoring + " for the Ad_ID "
                                            + exchange.getIn().getHeader("Ad_ID") + "\n");
                                }

                            }).to("log:DEBUG?showBody=true&showHeaders=true");

            from("sql:select ID from ENG_Agency_Ads where AdLayoutID=3?"
                    + "consumer.onConsume=update ENG_Agency_Ads set scoring=:#scoring where ID=:#Ad_ID&" + "consumer.delay=5s&"
                    + "dataSource=dataSource").process(new Processor() {

                        @Override
                        public void process(Exchange exchange) throws Exception {
                            // TODO Auto-generated method stub
                            Map results = exchange.getIn().getBody(Map.class);
                            System.out.println("output Clicked" + results.get("ID"));

                            exchange.getIn().setHeader("Ad_ID", results.get("ID"));
                        }

                    }).to("log:DEBUG?showBody=true&showHeaders=true")

                            .to("sql:select count(*) As counter_playedall from BIll_AdsBilling a, ENG_Agency_Ads b where a.Action='PlayedAll' and b.AdLayoutID=3 and a.Ad_ID=:#Ad_ID?dataSource=dataSource")
                            .to("log:DEBUG?showBody=true&showHeaders=true").process(new Processor() {

                                @Override
                                public void process(Exchange exchange) throws Exception {
                                    // TODO Auto-generated method stub
                                    List results = exchange.getIn().getBody(List.class);
                                    Map test = (Map) results.get(0);
                                    System.out.println("output PlayedAll" + test.get("counter_playedall"));

                                    exchange.getIn().setHeader("count-PlayedAll-Video", test.get("counter_playedall"));
                                }

                            }).to("log:DEBUG?showBody=true&showHeaders=true")

                            .to("sql:select count(*) As counter_play5sec from BIll_AdsBilling a, ENG_Agency_Ads b where a.Action='Played5Sec' and b.AdLayoutID=3 and a.Ad_ID=:#Ad_ID?dataSource=dataSource")
                            .to("log:DEBUG?showBody=true&showHeaders=true").process(new Processor() {

                                @Override
                                public void process(Exchange exchange) throws Exception {
                                    // TODO Auto-generated method stub
                                    List results = exchange.getIn().getBody(List.class);
                                    Map test = (Map) results.get(0);
                                    System.out.println("output Viewed" + test.get("counter_play5sec"));

                                    exchange.getIn().setHeader("count-Played5Sec-Video", test.get("counter_play5sec"));
                                }

                            }).to("log:DEBUG?showBody=true&showHeaders=true")

                            .to("sql:select count(*) As counter_clicked from BIll_AdsBilling a, ENG_Agency_Ads b where a.Action='clicked' and b.AdLayoutID=3 and a.Ad_ID=:#Ad_ID?dataSource=dataSource")
                            .to("log:DEBUG?showBody=true&showHeaders=true").process(new Processor() {

                                @Override
                                public void process(Exchange exchange) throws Exception {
                                    // TODO Auto-generated method stub
                                    List results = exchange.getIn().getBody(List.class);
                                    Map test = (Map) results.get(0);
                                    System.out.println("output Clicked" + test.get("counter_clicked"));

                                    exchange.getIn().setHeader("count-Clicked-Video", test.get("counter_clicked"));
                                }

                            }).to("log:DEBUG?showBody=true&showHeaders=true")

                            .to("sql:select count(*) As counter_Imp from BIll_AdsBilling a, ENG_Agency_Ads b where a.Action='Impression' and b.AdLayoutID=3?dataSource=dataSource")
                            .to("log:DEBUG?showBody=true&showHeaders=true").process(new Processor() {

                                @Override
                                public void process(Exchange exchange) throws Exception {
                                    // TODO Auto-generated method stub
                                    List results = exchange.getIn().getBody(List.class);
                                    Map test = (Map) results.get(0);
                                    System.out.println("output Impression" + test.get("counter_Imp"));

                                    exchange.getIn().setHeader("Total-Impressions-Video", test.get("counter_Imp"));
                                }

                            }).to("log:DEBUG?showBody=true&showHeaders=true")

                            .process(new Processor() {

                                @Override
                                public void process(Exchange exchange) throws Exception {
                                    // TODO Auto-generated method stub

                                    int Imp = (int) exchange.getIn().getHeader("Total-Impressions-Video");
                                    int played5sec = (int) exchange.getIn().getHeader("count-Played5Sec-Video");
                                    int clicked = (int) exchange.getIn().getHeader("count-Clicked-Video");
                                    int playedall=(int) exchange.getIn().getHeader("count-PlayedAll-Video");

                                    float image_scoring = (float) (played5sec + clicked) / (2 * Imp);
                                    float image_scoring_all = (float) (2*playedall + clicked) / (3 * Imp);
                                    float total_scoring =image_scoring+image_scoring_all;

                                    exchange.getIn().setHeader("scoring", total_scoring);
                                    System.out.println("Impressions" + Imp + "Played5sec " + played5sec + "playedall " + playedall
                                            + " Scoring is " + total_scoring + " for the Ad_ID "
                                            + exchange.getIn().getHeader("Ad_ID") + "\n");
                                }

                            }).to("log:DEBUG?showBody=true&showHeaders=true");

        }
    }

    @Bean(name = "dataSource")
    @ConfigurationProperties(prefix = "spring.datasource")
    @Primary
    public DataSource dataSource2() {

        return DataSourceBuilder.create().build();
    }

}
