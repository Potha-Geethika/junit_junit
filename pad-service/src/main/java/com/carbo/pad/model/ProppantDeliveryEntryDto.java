package com.carbo.pad.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
public class ProppantDeliveryEntryDto {

    private String id;

    @Field("jobId")
    private String jobId;

    @Field("date")
    private Date date;

    @Field("proppant")
    private String proppant;

    @Field("bol")
    private String bol;

    @Field("po")
    private String po;

    @Field("vendor")
    private String vendor;

    @Field("wtAmount")
    private float wtAmount;

    @Field("uom")
    private String uom;

    @Field("silo")
    private String silo;

    @Field("usedIn")
    private List<ProppantUsed> usedIn = new ArrayList<>();

    @Field("boxNumber")
    private Integer boxNumber;

    @Field("padStage")
    private String padStage;

    @Field("truckNumber")
    private String truckNumber;

    @Field("timeLoadout")
    private String timeLoadout;

    @Field("returned")
    private Float returned;

    @Field("transferredToJobId")
    private String transferredToJobId;

    @Field("transferredFromJobId")
    private String transferredFromJobId;

    @Field("transferredToJob")
    private Float transferredToJob;

    @Field("transferredToYard")
    private Float transferredToYard;

    @Field("writeOffBalance")
    private Float writeOffBalance;

    @Field("organizationId")
    @Indexed(unique = false)
    private String organizationId;

    @Field("ts")
    private Long ts;

    @Field("created")
    private Long created = new Date().getTime();

    @Field("modified")
    private Long modified  = new Date().getTime();

    @Field("lastModifiedBy")
    private String lastModifiedBy;

    @Field("isMigrated")
    private Boolean isMigrated;

    @Field("status")
    private String status;

    @Field("orderStatusID")
    private int orderStatusID;

    @Field("delivered")
    private boolean delivered;
}
