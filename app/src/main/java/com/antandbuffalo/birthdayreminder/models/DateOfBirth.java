package com.antandbuffalo.birthdayreminder.models;

import androidx.annotation.Keep;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by i677567 on 23/9/15.
 */
@Keep
public class DateOfBirth implements Serializable {
    private long dobId;
    private String name;
    private Date dobDate;
    private String description;
    private Boolean isRemoveYear;
    private int age;

    public long getDobId() {
        return dobId;
    }

    public void setDobId(long dobId) {
        this.dobId = dobId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDobDate() {
        return dobDate;
    }

    public void setDobDate(Date dobDate) {
        this.dobDate = dobDate;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public Boolean getRemoveYear() {
        return isRemoveYear;
    }

    public void setRemoveYear(Boolean removeYear) {
        isRemoveYear = removeYear;
    }

    public static DateOfBirth getInstance() {
        return new DateOfBirth();
    }
}
