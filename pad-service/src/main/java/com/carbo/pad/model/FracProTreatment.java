package com.carbo.pad.model;

import java.util.List;

public class FracProTreatment {
    private String name;
    private Integer wellId;
    private String formationName;
    private Float totalVolume;
    private Float pumpDownVolume;
    private Float averagePres;
    private Float maxPres;
    private Float avgSlurryReturnRate;
    private Float maxFluidRate;
    private Float mdFormationTop;
    private Float mdFormationBottom;
    private Integer totalPerfs;
    private Float breakDownPres;
    private Float initialShutinPres;
    private Float fractureGradient;
    private Float proppantLadenVol;
    private Long timeStart;
    private Integer timeZone;
    private Integer dayLight;

    private Float desCleanVol;
    private Float desSlurryVol;
    private Integer ballSeatTime;
    private Long baseTreatmentDateTime;
    private float producedWater;
    private List<Perforations> perforations;



    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getWellId() {
        return wellId;
    }

    public void setWellId(Integer name) {
        this.wellId = wellId;
    }

    public String getFormationName() {
        return formationName;
    }

    public void setFormationName(String formationName) {
        this.formationName = formationName;
    }

    public Float getTotalVolume() {
        return totalVolume;
    }

    public void setTotalVolume(Float totalVolume) {
        this.totalVolume = totalVolume;
    }

    public Float getPumpDownVolume() {
        return pumpDownVolume;
    }

    public void setPumpDownVolume(Float pumpDownVolume) {
        this.pumpDownVolume = pumpDownVolume;
    }

    public Float getAveragePres() {
        return averagePres;
    }

    public void setAveragePres(Float averagePres) {
        this.averagePres = averagePres;
    }

    public Float getMaxPres() {
        return maxPres;
    }

    public void setMaxPres(Float maxPres) {
        this.maxPres = maxPres;
    }

    public Float getAvgSlurryReturnRate() {
        return avgSlurryReturnRate;
    }

    public void setAvgSlurryReturnRate(Float avgSlurryReturnRate) {
        this.avgSlurryReturnRate = avgSlurryReturnRate;
    }

    public Float getMaxFluidRate() {
        return maxFluidRate;
    }

    public void setMaxFluidRate(Float maxFluidRate) {
        this.maxFluidRate = maxFluidRate;
    }

    public Float getMdFormationTop() {
        return mdFormationTop;
    }

    public void setMdFormationTop(Float mdFormationTop) {
        this.mdFormationTop = mdFormationTop;
    }

    public Float getMdFormationBottom() {
        return mdFormationBottom;
    }

    public void setMdFormationBottom(Float mdFormationBottom) {
        this.mdFormationBottom = mdFormationBottom;
    }

    public Integer getTotalPerfs() {
        return totalPerfs;
    }

    public void setTotalPerfs(Integer totalPerfs) {
        this.totalPerfs = totalPerfs;
    }

    public Float getBreakDownPres() {
        return breakDownPres;
    }

    public void setBreakDownPres(Float breakDownPres) {
        this.breakDownPres = breakDownPres;
    }

    public Float getInitialShutinPres() {
        return initialShutinPres;
    }

    public void setInitialShutinPres(Float initialShutinPres) {
        this.initialShutinPres = initialShutinPres;
    }

    public Float getFractureGradient() {
        return fractureGradient;
    }

    public void setFractureGradient(Float fractureGradient) {
        this.fractureGradient = fractureGradient;
    }

    public Float getProppantLadenVol() { return proppantLadenVol; }

    public void setProppantLadenVol(Float proppantLadenVol) { this.proppantLadenVol = proppantLadenVol; }

    public Long getTimeStart() { return timeStart; }

    public void setTimeStart(Long timeStart) { this.timeStart = timeStart; }

    public Integer getTimeZone() { return timeZone; }

    public void setTimeZone(Integer timeZone) { this.timeZone = timeZone; }

    public Integer getDayLight() { return dayLight; }

    public void setDayLight(Integer dayLight) { this.dayLight = dayLight; }



    public Float getDesCleanVol() { return desCleanVol; }

    public void setDesCleanVol(Float desCleanVol) { this.desCleanVol = desCleanVol; }

    public Float getDesSlurryVol() { return desSlurryVol; }

    public void setDesSlurryVol(Float desSlurryVol) { this.desSlurryVol = desSlurryVol; }

    public Integer getBallSeatTime() { return ballSeatTime; }

    public void setBallSeatTime(Integer ballSeatTime) { this.ballSeatTime = ballSeatTime; }

    public Long getBaseTreatmentDateTime() { return baseTreatmentDateTime; }

    public void setBaseTreatmentDateTime(Long baseTreatmentDateTime) { this.baseTreatmentDateTime = baseTreatmentDateTime; }


    public List<Perforations> getPerforations() {return perforations;}

    public void setPerforations(List<Perforations> perforations) {this.perforations = perforations;}



    public float getProducedWater() {
        return producedWater;
    }

    public void setProducedWater(float producedWater) {
        this.producedWater = producedWater;
    }

    @Override
    public String toString() {
        return "FracProTreatment{" +
                "name='" + name + '\'' +
                ", formationName='" + formationName + '\'' +
                ", totalVolume=" + totalVolume +
                ", pumpDownVolume=" + pumpDownVolume +
                ", averagePres=" + averagePres +
                ", maxPres=" + maxPres +
                ", avgSlurryReturnRate=" + avgSlurryReturnRate +
                ", maxFluidRate=" + maxFluidRate +
                ", mdFormationTop=" + mdFormationTop +
                ", mdFormationBottom=" + mdFormationBottom +
                ", totalPerfs=" + totalPerfs +
                ", breakDownPres=" + breakDownPres +
                ", initialShutinPres=" + initialShutinPres +
                ", fractureGradient=" + fractureGradient +
                ", proppantLadenVol=" + proppantLadenVol +
                ", timeStart=" + timeStart +
                ", timeZone=" + timeZone +
                ", dayLight=" + dayLight +
                ", producedWater=" + producedWater +
                ", producedWater=" + desCleanVol +
                ", producedWater=" + desSlurryVol +
                ", ballSeatTime=" + ballSeatTime +
                ", baseTreatmentDateTime=" + baseTreatmentDateTime +
                '}';
    }
}
