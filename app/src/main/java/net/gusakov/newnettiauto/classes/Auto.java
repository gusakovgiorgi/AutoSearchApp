package net.gusakov.newnettiauto.classes;

/**
 * Created by hasana on 4/3/2017.
 */

public class Auto {
    private int id;
    private String name;
    private String imageUrlString;
    private String description;
    private int price;
    private String yearAndMileage;
    private String seller;
    private String link;
    private String phoneNumberURI;
    private boolean isDealer;



    private long timestamp;

    public Auto(){
    }

    public Auto(int id,String name, String description, int price, String yearAndMileage, String seller,String link,String phoneNumberURI, boolean isDealer,long timestamp,String imageUrlString) {
        this.id=id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.yearAndMileage = yearAndMileage;
        this.seller = seller;
        this.link=link;
        this.phoneNumberURI =phoneNumberURI;
        this.isDealer = isDealer;
        this.timestamp=timestamp;
        this.imageUrlString=imageUrlString;
    }
    public long getTimestamp() {
        return timestamp;
    }
    public static boolean isValidId(int id){
        return id>0;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public void setYearAndMileage(String yearAndMileage) {
        this.yearAndMileage = yearAndMileage;
    }

    public void setSeller(String seller) {
        this.seller = seller;
    }

    public void setDealer(boolean dealer) {
        isDealer = dealer;
    }
    public int getId() {
        return id;
    }

    public String getLink() {
        return link;
    }

    public String getPhoneNumberURI() {
        return phoneNumberURI;
    }

    public void setPhoneNumberURI(String phoneNumberURI) {
        this.phoneNumberURI = phoneNumberURI;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getPrice() {
        return price;
    }

    public String getYearAndMileage() {
        return yearAndMileage;
    }

    public String getSeller() {
        return seller;
    }

    public boolean isDealer() {
        return isDealer;
    }

    public String getImageUrlString() {
        return imageUrlString;
    }

    public void setImageUrlString(String imageUrlString) {
        this.imageUrlString = imageUrlString;
    }
}

