package com.fitness.modules.booking.service.impl;

public record BookingReservationResult(Status status) {

    public enum Status {
        SUCCESS,
        FULL,
        ALREADY_BOOKED
    }

    public static BookingReservationResult success() {
        return new BookingReservationResult(Status.SUCCESS);
    }

    public static BookingReservationResult full() {
        return new BookingReservationResult(Status.FULL);
    }

    public static BookingReservationResult alreadyBooked() {
        return new BookingReservationResult(Status.ALREADY_BOOKED);
    }

    public boolean successResult() {
        return status == Status.SUCCESS;
    }
}
