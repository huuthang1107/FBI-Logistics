package com.example.demoapp.model;

import java.io.Serializable;

public class FCLModel implements Serializable {
    private String stt;
    private String pol;
    private String pod;
    private String of20;
    private String of40;
    private String of45;
    private String su20;
    private String su40;
    private String line;
    private String notes;
    private String valid;
    private String note2;
    private String type;
    private String month;
    private String continent;
    private String createdDate;
    private String pTime;

    public FCLModel(){}

    public FCLModel(String stt, String pol, String pod, String of20, String of40, String of45, String su20,
                    String su40, String line, String notes, String valid, String note2, String type,
                    String month, String continent, String createdDate) {
        this.stt = stt;
        this.pol = pol;
        this.pod = pod;
        this.of20 = of20;
        this.of40 = of40;
        this.of45 = of45;
        this.su20 = su20;
        this.su40 = su40;
        this.line = line;
        this.notes = notes;
        this.valid = valid;
        this.note2 = note2;
        this.type = type;
        this.month = month;
        this.continent = continent;
        this.createdDate = createdDate;
    }

    public String getStt() {
        return stt;
    }

    public String getpTime() {
        return pTime;
    }

    public void setpTime(String pTime) {
        this.pTime = pTime;
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

    public String getSu20() {
        return su20;
    }

    public void setSu20(String su20) {
        this.su20 = su20;
    }

    public String getSu40() {
        return su40;
    }

    public void setSu40(String su40) {
        this.su40 = su40;
    }

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getValid() {
        return valid;
    }

    public void setValid(String valid) {
        this.valid = valid;
    }

    public String getNote2() {
        return note2;
    }

    public void setNote2(String note2) {
        this.note2 = note2;
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
