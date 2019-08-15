// ResultData.Add("Layout", "Image");
// ResultData.Add("Title", ssImage.Title);
// ResultData.Add("Description", ssImage.Description);
// ResultData.Add("Path", HttpContext.Current.Request.Url.Authority + "/content/ad_images/"+  ssImage.Path);
// ResultData.Add("ADID", ssImage.AdID);            
// ResultData.Add("ButtonName", ssImage.ButtonName);
// ResultData.Add("ButtonLink", ssImage.ButtonLink);
// ResultData.Add("ButtonDestination", ssImage.ButtonDestination);


package io.fabric8.quickstarts.camel;


public class addlayout {

	  private int  AdID;
	  private String Layout;
      private String ButtonName;
      private String ButtonLink;
      private String Title;
      private String ButtonDestination;
      private String Description;
      private String Path;

	    public int getAdID() {
	        return AdID;
	    }

	    public void setAdID(int AdID) {
	        this.AdID = AdID;
		}
    //adlayoutid

    public String  getLayout() {
        return Layout;
    }

    public void setLayout(String Layout) {
        this.Layout = Layout;
    }


    //chargeonimpression
    public String getButtonName() {
        return ButtonName;
    }

    public void setButtonName(String ButtonName) {
        this.ButtonName = ButtonName;
    }

    public String  getButtonLink() {
        return ButtonLink;
    }

    public void setButtonLink(String ButtonLink) {
        this.ButtonLink = ButtonLink;
    }
 //isinvasive   

 public String getTitle() {
    return Title;
}

public void setTitle(String Title) {
    this.Title = Title;
}
        //advertiserid   
    
       
    public String getButtonDestination() {
        return ButtonDestination;
    }

    public void setButtonDestination(String ButtonDestination) {
        this.ButtonDestination = ButtonDestination;
    }
        public String getDescription() {
	        return Description;
	    }

	    public void setDescription(String Description) {
	        this.Description = Description;
        }
        
		
        
    //     private String AdvertiserName;
    //   private String AdvertiserImage;

    public String getPath() {
        return Path;
    }

    public void setPath(String Path) {
        this.Path = Path;
    }

   
	  
	
	
	
}
