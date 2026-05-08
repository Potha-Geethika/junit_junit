package com.carbo.pad.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.*;
import java.util.stream.Collectors;

@Document(collection = "jobs")
@JsonIgnoreProperties(ignoreUnknown = true)
@CompoundIndex(def = "{'_id': 1, 'users._id': 1}", name = "job_id_user_id_index", unique = true)
@CompoundIndex(def = "{'organizationId': 1, 'jobNumber': 1}", name = "unique_organizationid_jobnumber_index", unique = true)
@Data
public class Job {
    @Id
    private String id;

    @Field("name")
    @NotEmpty(message = "name can not be empty")
    @Size(max = 100, message = "name can not be more than 100 characters.")
    private String name;

    @Field("jobNumber")
    @NotEmpty(message = "jobNumber can not be empty")
    @Size(max = 14, message = "jobNumber can not be more than 14 characters.")
    private String jobNumber;

    @Field("fleet")
    private String fleet;

    @Field("operator")
    private String operator;

    @Field("pad")
    private String pad;

    @Field("location")
    private String location;

    @Field("zipper")
    private Boolean zipper;

    @Field("refrac")
    private boolean refrac;

    @Field("wells")
    private List<Well> wells = new ArrayList<>();

    @Field("targetStagesPerDay")
    private int targetStagesPerDay;

    @Field("targetDailyPumpTime")
    private float targetDailyPumpTime;

    @Field("proppantSchematicType")
    private String proppantSchematicType = "silos";

    @Field("numberOfUnits")
    private Integer numberOfUnits = 3;

    @Field("coneLbs")
    private Float coneLbs = 1400.0f;

    @Field("curWellId")
    private String curWellId;

    @Field("curStage")
    private String curStage;

    @Field("startDate")
    private Long startDate;

    @Field("endDate")
    private Long endDate;

    @Field("expectedStartDate")
    private Long expectedStartDate;

    @Field("expectedEndDate")
    private Long expectedEndDate;

    @Field("startDateStr")
    private String startDateStr;

    @Field("timezone")
    private String timezone;

    @Field("discounts")
    private Map<String, Float> discounts = new HashMap<>();

    @Field("organizationId")
    @Indexed
    private String organizationId;

    @Field("status")
    private String status;

    @Field("beltDirection")
    private String beltDirection = "left";

    @Field("mileageChargeDistance")
    private Integer mileageChargeDistance = 0;

    @Field("activityLogStartTime")
    private String activityLogStartTime = "00:00";

    @Field("wellheadCo")
    private String wellheadCo;

    @Field("wirelineCo")
    private String wirelineCo;

    @Field("waterTransferCo")
    private String waterTransferCo;

    @Field("goToMeetingId")
    private String goToMeetingId;

    @Field("includeToeStage")
    private Boolean includeToeStage;

    @Field("predefinedChannels")
    private List<String> predefinedChannels;

    @Field("sharedWithOrganizationId")
    private String sharedWithOrganizationId;

    @JsonIgnore
    private Integer padStageTotal;

    @Field("ts")
    private Long ts;

    @Field("rts")
    private Long rts;

    @Field("serviceCompany")
    private String serviceCompany;

    @Field("disableOffline")
    private Boolean disableOffline;

    @Field("created")
    private Long created = new Date().getTime();

    @Field("modified")
    private Long modified = new Date().getTime();

    @Field("startDateModified")
    private Long startDateModified;

    @Field("backupDate")
    @Indexed
    private Date backupDate;

    @Field("lastModifiedBy")
    private String lastModifiedBy;

    @Field("connectJobTime")
    private boolean connectJobTime;

    @Field("automatize")
    private boolean automatize;

    @Field("additionalJobsFieldTicket")
    private List<String> additionalJobsFieldTicket = new ArrayList<>();

    @Field("hpp")
    private String hpp;

    @Field("mpn")
    private String mpn;

    @Field("connector")
    private String connector;

    @Field("singleOrDouble")
    private String singleOrDouble;

    @Field("length")
    private String length;

    @Field("manufacturer")
    private String manufacturer;


    @Field("swapOverTime")
    private int swapOverTime;

    @Field("targetWirelineTimePerStage")
    private float targetWirelineTimePerStage;

    @Field("targetMaintenanceTimePerDay")
    private float targetMaintenanceTimePerDay;

    @Field("latestBtu")
    private float latestBtu;

    @Field("btu")
    private float btu;

    @Field("hideDiscounts ")
    private boolean hideDiscounts = false;

    @Field("districtId")
    private String districtId;

    @Field("proposalId")
    private String proposalId;

    @Field("priceBookId")
    private String priceBookId;

    @Field("districtWhenCompleted")
    private String districtWhenCompleted;

    @Field("isNewWorkflow")
    private boolean isNewWorkflow;

    @Field("receiveEBol")
    private Boolean receiveEBol;

    @Field("fitToPage")
    private Boolean fitToPage = false;
    @Field("fleetType")
    private String fleetType;
    @Field("dualFuelPumpCount")
    private int dualFuelPumpCount;
    @Field("taxFlag")
    private boolean taxFlag = false;
    @Field("taxPercentage")
    private float taxPercentage;

    @Field("splitStream")
    private boolean splitStream;

    @Field("producedWater")
    private boolean producedWater=false;

    @Field("dirtyMixed")
    private boolean dirtyMixed = false;

    @Field("cng")
    private boolean cng;

    @Field("lng")
    private boolean lng;

    @Field("fieldGas")
    private boolean fieldGas;

    @NotBlank(message = "operationsType can't be blank")
    @Field("operationsType")
    private String operationsType;

    @NotBlank(message = "padEnergyType can't be blank")
    @Field("padEnergyType")
    private String padEnergyType;

}
