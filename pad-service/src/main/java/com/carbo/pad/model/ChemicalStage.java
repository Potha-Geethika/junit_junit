package com.carbo.pad.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;

@Data
@NoArgsConstructor
public class ChemicalStage {
    @Id
    private String id;

    @Field("jobId")
    @NotNull
    @Indexed(unique = false)
    private String jobId;

    @Field("wellId")
    @Indexed(unique = false)
    @NotNull
    private String wellId;

    @Field("date")
    private Date date;

    @Field("well")
    private String well;

    @Field("stage")
    @NotNull
    private Float stage;

    @Field("CalculateByStraps")
    private Boolean calculateByStraps;

    @Field("cleanTotal")
    private Float cleanTotal;


    @Field("organizationId")
    @NotNull
    @Indexed(unique = false)
    private String organizationId;

    @Field("created")
    private Long created = new Date().getTime();

    @Field("modified")
    private Long modified  = new Date().getTime();

}
