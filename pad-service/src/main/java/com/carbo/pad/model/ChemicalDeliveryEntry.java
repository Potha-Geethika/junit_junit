package com.carbo.pad.model;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Document(collection = "chemical-delivery-entries")
public class ChemicalDeliveryEntry {
    @Id
    private String id;

    @Field("jobId")
    @Indexed(unique = false)
    private String jobId;

    @Field("date")
    @NotEmpty(message = "date can not be empty")
    private Date date;

    @Field("vendor")
    @NotEmpty(message = "vendor can not be empty")
    private String vendor;

    @Field("chemical")
    @NotEmpty(message = "chemical can not be empty")
    private String chemical;

    @Field("bol")
    @NotEmpty(message = "bol can not be empty")
    private String bol;

    @Field("po")
    @NotEmpty(message = "po can not be empty")
    private String po;

    @Field("received")
    private Float received;

    @Field("returned")
    private Float returned;

    @Field("transferredToJobId")
    private String transferredToJobId;

    @Field("transferredFromWell")
    private String transferredFromWell;

    @Field("transferredFromJobId")
    private String transferredFromJobId;

    @Field("transferredToJob")
    private Float transferredToJob;

    @Field("transferredToYard")
    private Float transferredToYard;

    @Field("writeOffBalance")
    private Float writeOffBalance;

    @Field("uom")
    private String uom;

    @Field("bolQuantity")
    private Float bolQuantity;

    @Field("strengthBaume")
    private String strengthBaume;

    @Field("gallons")
    private Float gallons;

    @Field("rawGallons")
    private Float rawGallons;

    @Field("usedIn")
    private List<ChemicalUsed> usedIn = new ArrayList<>();

    @Field("inventoryType")
    private String inventoryType = "";

    @Field("created")
    private Long created = new Date().getTime();

    @Field("modified")
    private Long modified  = new Date().getTime();

    @Field("organizationId")
    @Indexed(unique = false)
    private String organizationId;

    @Field("ts")
    private Long ts;

    @Field("status")
    private String status = "";

    @Field("lastModifiedBy")
    private String lastModifiedBy;

    @Field("isMigrated")
    private Boolean isMigrated;

    @Field("standBy")
    private boolean standBy = false;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getChemical() {
        return chemical;
    }

    public void setChemical(String chemical) {
        this.chemical = chemical;
    }

    public String getBol() {
        return bol;
    }

    public void setBol(String bol) {
        this.bol = bol;
    }

    public Float getReceived() {
        return received;
    }

    public void setReceived(Float received) {
        this.received = received;
    }

    public Float getBolQuantity() {
        return bolQuantity;
    }

    public void setBolQuantity(Float bolQuantity) {
        this.bolQuantity = bolQuantity;
    }

    public String getStrengthBaume() {
        return strengthBaume;
    }

    public void setStrengthBaume(String strengthBaume) {
        this.strengthBaume = strengthBaume;
    }

    public Float getGallons() {
        return gallons;
    }

    public void setGallons(Float gallons) {
        this.gallons = gallons;
    }

    public List<ChemicalUsed> getUsedIn() {
        return usedIn;
    }

    public void setUsedIn(List<ChemicalUsed> usedIn) {
        this.usedIn = usedIn;
    }

    public String getUom() {
        return uom;
    }

    public void setUom(String uom) {
        this.uom = uom;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getPo() {
        return po;
    }

    public void setPo(String po) {
        this.po = po;
    }

    public Float getReturned() {
        return returned;
    }

    public void setReturned(Float returned) {
        this.returned = returned;
    }

    public String getTransferredToJobId() {
        return transferredToJobId;
    }

    public void setTransferredToJobId(String transferredToJobId) {
        this.transferredToJobId = transferredToJobId;
    }


    public String getTransferredFromJobId() {
        return transferredFromJobId;
    }

    public void setTransferredFromJobId(String transferredFromJobId) {
        this.transferredFromJobId = transferredFromJobId;
    }


    public Float getTransferredToJob() {
        return transferredToJob;
    }

    public void setTransferredToJob(Float transferredToJob) {
        this.transferredToJob = transferredToJob;
    }


    public Float getTransferredToYard() {
        return transferredToYard;
    }

    public void setTransferredToYard(Float transferredToYard) {
        this.transferredToYard = transferredToYard;
    }


    public Float getWriteOffBalance() {
        return writeOffBalance;
    }

    public void setWriteOffBalance(Float writeOffBalance) {
        this.writeOffBalance = writeOffBalance;
    }

    public Float getRawGallons() {
        return rawGallons;
    }

    public void setRawGallons(Float rawGallons) {
        this.rawGallons = rawGallons;
    }

    public String getInventoryType() {
        return inventoryType;
    }

    public void setInventoryType(String inventoryType) {
        this.inventoryType = inventoryType;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public Long getTs() {
        return ts;
    }

    public void setTs(Long ts) {
        this.ts = ts;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public void updateModified() {
        this.modified = new Date().getTime();
    }

    public Long getCreated() {
        return created;
    }

    public void setMigrated(Boolean migrated) {
        isMigrated = migrated;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTransferredFromWell() {return transferredFromWell;}

    public void setTransferredFromWell(String transferredFromWell) {this.transferredFromWell = transferredFromWell;}

    public boolean isStandBy() {
        return standBy;
    }

    public void setStandBy(boolean standBy) {
        this.standBy = standBy;
    }
}
