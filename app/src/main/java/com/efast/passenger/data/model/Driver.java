package com.efast.passenger.data.model;

/** The driver assigned to a ride. */
public final class Driver {
    public final String name;
    public final String vehicle;
    public final String plate;
    public final double rating;
    public final int etaMinutes;
    /** 4-digit code the passenger tells the driver to start the trip. */
    public final String rideOtp;

    public Driver(String name, String vehicle, String plate, double rating, int etaMinutes, String rideOtp) {
        this.name = name;
        this.vehicle = vehicle;
        this.plate = plate;
        this.rating = rating;
        this.etaMinutes = etaMinutes;
        this.rideOtp = rideOtp;
    }
}
