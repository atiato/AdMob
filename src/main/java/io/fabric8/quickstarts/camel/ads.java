package io.fabric8.quickstarts.camel;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public class ads {

	  private int  id;
	  private String adposter;
      private int budget;
      private int reach;
      private String status;
      private int advertiserid;
      private Boolean chargeonimpression;
      private Boolean chargeonclick;
      private Boolean isinvasive;
      private int adlayoutid;
      private String advertisername;
      private String advertiserimage;
      private addlayout Text;
      private addlayout Video;
      private addlayout Image;
      private addlayout Carousel;




//  ResultData.Add("AdvertiserName", CurrAd.ENG_Advertisers.Name);
//ResultData.Add("AdvertiserImage", HttpContext.Current.Request.Url.Authority + "/content/AdvertiserImages/" + CurrAd.ENG_Advertisers.Image);
	  
    //   COLUMN_NAME                                                                                                                      data_type
    //   -------------------------------------------------------------------------------------------------------------------------------- --------------------------------------------------------------------------------------------------------------------------------
    //   ID                                                                                                                               int
    //   AdPoster                                                                                                                         nvarchar
    //   Budget                                                                                                                           int
    //   PotentialReachMin                                                                                                                int
    //   Reach                                                                                                                            int
    //   PotentialReachMax                                                                                                                int
    //   Status                                                                                                                           nvarchar
    //   AdvertiserID                                                                                                                     int
    //   MarketingObjectiveID                                                                                                             int
    //   Name                                                                                                                             nvarchar
    //   DriveTrafficID                                                                                                                   int
    //   DailyBudget                                                                                                                      int
    //   ChargeOnImpression                                                                                                               bit
    //   ChargeOnClick                                                                                                                    bit
    //   Schedule_StartDate                                                                                                               datetime
    //   Schedule_EndDate                                                                                                                 datetime
    //   AdLayoutID                                                                                                                       int
    //   SeenByPublisher                                                                                                                  bit
    //   IsInvasive                                                                                                                       bit
    //   created_at                                                                                                                       datetime
    //   updated_at                                                                                                                       datetime
    //   deleted_at                                                                                                                       datetime
    //   CAP                                                                                                                              int

	   
	  //  private int test;
	  //SERVICE_TYPE, CALL_DATE, REVENUE_MONTH, CALL_CLASS, sum(CHARGED_AMOUNT) as CHARGED_AMOUNT
	    
	    public int getId() {
	        return id;
	    }

	    public void setId(int id) {
	        this.id = id;
        }
        
        public addlayout getText() {
	        return Text;
	    }

	    public void setText(addlayout Text) {
	        this.Text = Text;
        }
        
        public addlayout getVideo() {
	        return Video;
	    }

	    public void setVideo(addlayout Video) {
	        this.Video = Video;
        }
        
        public addlayout getImage() {
	        return Image;
	    }

	    public void setImage(addlayout Image) {
	        this.Image = Image;
        }
        
        public addlayout getCarousel() {
	        return Carousel;
	    }

	    public void setCarousel(addlayout Carousel) {
	        this.Carousel = Carousel;
		}
    //adlayoutid

    public int getAdlayoutid() {
        return adlayoutid;
    }

    public void setAdlayoutid(int adlayoutid) {
        this.adlayoutid = adlayoutid;
    }


    //chargeonimpression
    public Boolean getChargeonimpression() {
        return chargeonimpression;
    }

    public void setChargeonimpression(Boolean chargeonimpression) {
        this.chargeonimpression = chargeonimpression;
    }

    public Boolean getChargeonclick() {
        return chargeonclick;
    }

    public void setChargeonclick(Boolean chargeonclick) {
        this.chargeonclick = chargeonclick;
    }
 //isinvasive   

 public Boolean getIsinvasive() {
    return isinvasive;
}

public void setIsinvasive(Boolean isinvasive) {
    this.isinvasive = isinvasive;
}
        //advertiserid   
    
       
    public int getAdvertiserid() {
        return advertiserid;
    }

    public void setAdvertiserid(int advertiserid) {
        this.advertiserid = advertiserid;
    }
        public int getReach() {
	        return reach;
	    }

	    public void setReach(int reach) {
	        this.reach = reach;
        }
        
		public int getBudget() {
	        return budget;
	    }

	    public void setBudget(int budget) {
	        this.budget = budget;
        }
        
    //     private String AdvertiserName;
    //   private String AdvertiserImage;

    public String getAdvertiserName() {
        return advertisername;
    }

    public void setAdvertiserName(String advertisername) {
        this.advertisername = advertisername;
    }

    public String getAdvertiserImage() {
        return advertiserimage;
    }

    public void setAdvertiserImage(String advertiserimage) {
        this.advertiserimage = advertiserimage;
    }
	    public String getAdposter() {
	        return adposter;
	    }

	    public void setAdposter(String adposter) {
	        this.adposter = adposter;
	    }
	
//status

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

	  
	
	
	
}
