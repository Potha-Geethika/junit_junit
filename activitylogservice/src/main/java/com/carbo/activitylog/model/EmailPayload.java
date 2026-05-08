package com.carbo.activitylog.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EmailPayload {
    private String to;
    private String cc;
    private String subject;
    private String body;
    private String by;

    public String getTo() {return to;}

    public void setTo(String to) {this.to = to;}

    public String getCc() {return cc;}

    public void setCc(String cc) {this.cc = cc;}

    public String getSubject() {return subject;}

    public void setSubject(String subject) {this.subject = subject;}

    public String getBody() {return body;}

    public void setBody(String body) {this.body = body;}

    public String getBy() {return by;}

    public void setBy(String by) {this.by = by;}
}
