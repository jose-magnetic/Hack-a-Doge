package com.magnetic.hackathon.dogeideasapp;

public class GridItem {
    private String imageURL;
    private String siteURL;
    private String title;
    private double price;

    public GridItem() {
        super();
    }

    public GridItem(String display, String anImageURL, String aSiteURL, double thePrice) {
        title = display;
        imageURL = anImageURL;
        siteURL = aSiteURL;
        price = thePrice;

    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSiteURL() {
        return siteURL;
    }

    public void setSiteURL(String siteURL) {
        this.siteURL = siteURL;
    }
}
