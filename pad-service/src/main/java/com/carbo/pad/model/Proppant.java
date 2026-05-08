package com.carbo.pad.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigInteger;
import java.util.Date;

@Document(collection = "proppants")

public class Proppant {
    @Id
    private String id;

    @Field("name")
    private String name;

    @Field("volumePerStage")
    private Float volumePerStage;

    @Field("totalCleanVolume")
    private Float totalCleanVolume;

    @Field("totalCleanVolumeRound")
    private BigInteger totalCleanVolumeRound;

    @Field("uom")
    private String uom;

    @Field("description")
    private String description;

    @Field("code")
    private String code;

    @Field("price")
    private Float price;

    @Field("discount")
    private Float discount;

    @Field("maxCapacity")
    private Float maxCapacity;

    @Field("created")
    private Long created = new Date().getTime();

    @Field("modified")
    private Long modified  = new Date().getTime();
    @Field("organizationId")
    private String organizationId;

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Float getVolumePerStage() {
        return volumePerStage;
    }

    public void setVolumePerStage(Float volumePerStage) {
        this.volumePerStage = volumePerStage;
    }

    public Float getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(Float maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public String getUom() {
        return uom;
    }

    public void setUom(String uom) {
        this.uom = uom;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Float getPrice() {
        return price;
    }

    public void setPrice(Float price) {
        this.price = price;
    }

    public Float getTotalCleanVolume() {
        return totalCleanVolume;
    }

    public void setTotalCleanVolume(Float totalCleanVolume) {
        this.totalCleanVolume = totalCleanVolume;
    }

    public Float getDiscount() {
        return discount;
    }

    public void setDiscount(Float discount) {
        this.discount = discount;
    }

    public BigInteger getTotalCleanVolumeRound() {
        return totalCleanVolumeRound;
    }

    public void setTotalCleanVolumeRound(BigInteger totalCleanVolumeRound) {
        this.totalCleanVolumeRound = totalCleanVolumeRound;
    }


    public Proppant(Proppant p ) {
        this.id = p.id;
        this.code = p.code;
        this.description = p.description;
        this.created = p.created;
        this.name = p.name;
        this.volumePerStage = p.volumePerStage;
        this.totalCleanVolume = p.totalCleanVolume;
        this.totalCleanVolumeRound = p.totalCleanVolumeRound;
        this.uom = p.uom;
        this.description = p.description;
        this.price = p.price;
        this.discount = p.discount;
        this.maxCapacity = p.maxCapacity;

    }

    public Proppant() {

    }
}