package com.opencsv;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class product {
    @Id
    private String id;

    @Column(name = "brand")
    private String brand;

    @Column(name = "color")
    private String color;
    @Column(name = "gender")
    private String gender;

    @Column(name = "size")
    private String size;

    @Column(name = "price")
    private double price;

    @Column(name = "rating")
    private double rating;

    @Column(name = "availability")
    private String availability;

    public product() {
    }
    public product(String id, String brand, String color, String gender, String size, int price, double rating, String availability) {
        this.id = id;
        this.brand= brand;
        this.color = color;
        this.gender = gender;
        this.size = size;
        this.price = price;
        this.rating = rating;
        this.availability = availability;
    }

    public String getBrand() {
        return brand;
    }
}
