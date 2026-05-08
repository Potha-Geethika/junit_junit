package com.carbo.pad.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.*;

@Data
@NoArgsConstructor
public class ProppantStage {

    @Id
    private String id;

    @Field("jobId")
    @NotNull
    @Indexed(unique = false)
    private String jobId;

    @Field("wellId")
    @NotNull
    @Indexed(unique = false)
    private String wellId;

    @Field("date")
    private Date date;

    @Field("well")
    private String well;

    @Field("stage")
    @NotNull
    private Float stage;

    @Field("blender")
    private String blender;

    @Field("diverter")
    private String diverter;

    @Field("diverterAmount")
    private Float diverterAmount;

    @Field("currentInSilos")
    private Map<String, Float> currentInSilos = new HashMap<>();

    @Field("organizationId")
    @NotNull
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
}
