package com.thievingstallcounter;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ThievingStallCounterData {
    ThievingStallCounterData(int stallsThieved, Double petChanceOfBeingDry) {
        this.stallsThieved = stallsThieved;
        this.petChanceOfBeingDry = petChanceOfBeingDry;
    }

    ThievingStallCounterData() {
        this.stallsThieved = 0;
        this.petChanceOfBeingDry = 1.0;
    }

    @Expose
    @SerializedName("stalls-thieved")
    private final int stallsThieved;

    @Expose
    @SerializedName("pet-chance-of-being-dry")
    private final Double petChanceOfBeingDry;

    public int getStallsThieved() {
        return stallsThieved;
    }

    public Double getPetChanceOfBeingDry() {
        return petChanceOfBeingDry;
    }
}
