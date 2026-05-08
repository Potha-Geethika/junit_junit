package com.carbo.pad.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Flush {
    @JsonProperty ("isFlush")
    boolean isFlush;

    @JsonProperty ("timeStamp")
    Long timeStamp;

    private boolean isFlush() {
        return isFlush;
    }

    private void setFlush(boolean flush) {
        isFlush = flush;
    }

    private Long getTimeStamp() {
        return timeStamp;
    }

    private void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }
}
