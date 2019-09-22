package net.gusakov.newnettiauto.classes;

import android.support.annotation.WorkerThread;

public class Auto {
    private AutoInfoExtractor autoInfoExtractor;
    private Integer id;
    private String name;
    private String imageUrlString;
    private String description;
    private Integer price;
    private String yearAndMileage;
    private String seller;
    private String link;
    private String phoneNumberURI;
    private Boolean isDealer;
    private long timestamp;

    public Auto(String autoRelatedData) {
        autoInfoExtractor = new AutoInfoExtractor(autoRelatedData);
        timestamp = System.currentTimeMillis();
    }

    public Auto(Integer id, String name, String description, Integer price, String yearAndMileage, String seller, String link, String phoneNumberURI, Boolean isDealer, long timestamp,String imageUrlString) {
        this.autoInfoExtractor = autoInfoExtractor;
        this.id = id;
        this.name = name;
        this.imageUrlString = imageUrlString;
        this.description = description;
        this.price = price;
        this.yearAndMileage = yearAndMileage;
        this.seller = seller;
        this.link = link;
        this.phoneNumberURI = phoneNumberURI;
        this.isDealer = isDealer;
        this.timestamp = timestamp;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public static boolean isValidId(int id) {
        return id > 0;
    }

    public Integer getId() {
        if (id == null) {
            id = autoInfoExtractor.getId();
        }
        return id;
    }

    public String getName() {
        if (name == null) {
            name = autoInfoExtractor.getName();
        }
        return name;
    }

    public String getImageUrlString() {
        if (imageUrlString == null) {
            imageUrlString = autoInfoExtractor.getImageUrlString();
        }
        return imageUrlString;
    }

    public String getDescription() {
        if (description == null) {
            description = autoInfoExtractor.getDescription();
        }
        return description;
    }

    public Integer getPrice() {
        if (price == null) {
            price = autoInfoExtractor.getPrice();
        }
        return price;
    }

    public String getYearAndMileage() {
        if (yearAndMileage == null) {
            yearAndMileage = autoInfoExtractor.getYear() + " " + autoInfoExtractor.getMileage();
        }
        return yearAndMileage;
    }

    public String getSeller() {
        if (seller == null) {
            seller = autoInfoExtractor.getSeller();
        }
        return seller;
    }

    public String getLink() {
        if (link == null) {
            link = autoInfoExtractor.getLink();
        }
        return link;
    }

    @WorkerThread
    public String getPhoneNumberURI() {
        if (phoneNumberURI == null) {
            phoneNumberURI = autoInfoExtractor.getPhoneNumberUri(getLink());
        }
        return phoneNumberURI;
    }

    public Boolean isDealer() {
        if (isDealer == null) {
            isDealer = autoInfoExtractor.isDealer();
        }
        return isDealer;
    }
}

