
package io.fabric8.quickstarts.camel;

import java.sql.Date;

public class CarouselItems {

    private int  CarousselID;
    private String Layout;
    private String ButtonName;
    private String ButtonLink;
    private String Title;
    private String ButtonDestination;
    private String Description;
    private String Image;

      public int getCarousselID() {
          return CarousselID;
      }

      public void setCarousselID(int CarousselID) {
          this.CarousselID = CarousselID;
      }
  //adlayoutid

  public String  getImage() {
      return Image;
  }

  public void setImage(String Image) {
      this.Image = Image;
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

 
 
    
	
}