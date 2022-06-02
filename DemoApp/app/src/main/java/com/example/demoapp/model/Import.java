package com.example.demoapp.model;

import java.io.Serializable;

public class Import implements Serializable {
    private String stt;
    private String pol;
    private String pod;
    private String of20;
    private String of40;
    private String of45;
    private String sur20;
    private String sur40;
    private String sur45;
    private String totalFreight;
    private String carrier;
    private String schedule;
    private String transitTime;
    private String freeTime;
    private String valid;
    private String note;
    private String type;
    private String month;
    private String continent;
    private String createdDate;

    public Import(){}

    public Import(String stt, String pol, String pod, String of20, String of40, String of45,
                  String sur20, String sur40, String sur45, String totalFreight, String carrier,
                  String schedule, String transitTime, String freeTime, String valid, String note,
                  String type, String month, String continent, String createdDate) {
        this.stt = stt;
        this.pol = pol;
        this.pod = pod;
        this.of20 = of20;
        this.of40 = of40;
        this.of45 = of45;
        this.sur20 = sur20;
        this.sur40 = sur40;
        this.sur45 = sur45;
        this.totalFreight = totalFreight;
        this.carrier = carrier;
        this.schedule = schedule;
        this.transitTime = transitTime;
        this.freeTime = freeTime;
        this.valid = valid;
        this.note = note;
        this.type = type;
        this.month = month;
        this.continent = continent;
        this.createdDate = createdDate;
    }

    public Import(String pol, String pod, String of20, String of40, String of45, String sur20,
                  String sur40, String sur45, String totalFreight, String carrier, String schedule,
                  String transitTime, String freeTime, String valid, String note, String type,
                  String month, String continent, String createdDate) {
        this.pol = pol;
        this.pod = pod;
        this.of20 = of20;
        this.of40 = of40;
        this.of45 = of45;
        this.sur20 = sur20;
        this.sur40 = sur40;
        this.sur45 = sur45;
        this.totalFreight = totalFreight;
        this.carrier = carrier;
        this.schedule = schedule;
        this.transitTime = transitTime;
        this.freeTime = freeTime;
        this.valid = valid;
        this.note = note;
        this.type = type;
        this.month = month;
        this.continent = continent;
        this.createdDate = createdDate;
    }

    public String getStt() {
        return stt;
    }

    public void setStt(String stt) {
        this.stt = stt;
    }

    public String getPol() {
        return pol;
    }

    public void setPol(String pol) {
        this.pol = pol;
    }

    public String getPod() {
        return pod;
    }

    public void setPod(String pod) {
        this.pod = pod;
    }

    public String getOf20() {
        return of20;
    }

    public void setOf20(String of20) {
        this.of20 = of20;
    }

    public String getOf40() {
        return of40;
    }

    public void setOf40(String of40) {
        this.of40 = of40;
    }

    public String getOf45() {
        return of45;
    }

    public void setOf45(String of45) {
        this.of45 = of45;
    }

    public String getSur20() {
        return sur20;
    }

    public void setSur20(String sur20) {
        this.sur20 = sur20;
    }

    public String getSur40() {
        return sur40;
    }

    public void setSur40(String sur40) {
        this.sur40 = sur40;
    }

    public String getSur45() {
        return sur45;
    }

    public void setSur45(String sur45) {
        this.sur45 = sur45;
    }

    public String getTotalFreight() {
        return totalFreight;
    }

    public void setTotalFreight(String totalFreight) {
        this.totalFreight = totalFreight;
    }

    public String getCarrier() {
        return carrier;
    }

    public void setCarrier(String carrier) {
        this.carrier = carrier;
    }

    public String getSchedule() {
        return schedule;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }

    public String getTransitTime() {
        return transitTime;
    }

    public void setTransitTime(String transitTime) {
        this.transitTime = transitTime;
    }

    public String getFreeTime() {
        return freeTime;
    }

    public void setFreeTime(String freeTime) {
        this.freeTime = freeTime;
    }

    public String getValid() {
        return valid;
    }

    public void setValid(String valid) {
        this.valid = valid;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getContinent() {
        return continent;
    }

    public void setContinent(String continent) {
        this.continent = continent;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }
}
