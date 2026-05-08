package com.carbo.pad.model;

import java.util.Date;
import java.util.List;

public interface IMaterialDeliveryEntry {
    String getJobId();
    String getBol();
    List<IMaterialUsed> getUsedIn();
    Date getDate();
    String getMaterialName();
}