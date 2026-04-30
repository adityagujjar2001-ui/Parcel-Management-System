package projectBL.model;

import java.time.LocalTime;

public class Booking {
    private final int bookingId;
    private final String userName;
    private final String recipientName;
    private final String recipientAddress;
    private final int recipientPin;
    private final String recipientMobile;
    private final float parcelWeightGram;
    private final String parcelContentsDescription;
    private final String parcelDeliveryType;
    private final String parcelPackingPreference;
    private final String pickupTime;
    private final String dropoffTime;
    private final double serviceCost;
    private final String status;
    private final LocalTime paymentTime;

    public Booking(int bookingId, String userName, String recipientName, String recipientAddress, int recipientPin,
            String recipientMobile, float parcelWeightGram, String parcelContentsDescription,
            String parcelDeliveryType, String parcelPackingPreference, String pickupTime, String dropoffTime,
            double serviceCost, String status, LocalTime paymentTime) {
        this.bookingId = bookingId;
        this.userName = userName;
        this.recipientName = recipientName;
        this.recipientAddress = recipientAddress;
        this.recipientPin = recipientPin;
        this.recipientMobile = recipientMobile;
        this.parcelWeightGram = parcelWeightGram;
        this.parcelContentsDescription = parcelContentsDescription;
        this.parcelDeliveryType = parcelDeliveryType;
        this.parcelPackingPreference = parcelPackingPreference;
        this.pickupTime = pickupTime;
        this.dropoffTime = dropoffTime;
        this.serviceCost = serviceCost;
        this.status = status;
        this.paymentTime = paymentTime;
    }

    public int getBookingId() {
        return bookingId;
    }

    public String getUserName() {
        return userName;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public String getRecipientAddress() {
        return recipientAddress;
    }

    public int getRecipientPin() {
        return recipientPin;
    }

    public String getRecipientMobile() {
        return recipientMobile;
    }

    public float getParcelWeightGram() {
        return parcelWeightGram;
    }

    public String getParcelContentsDescription() {
        return parcelContentsDescription;
    }

    public String getParcelDeliveryType() {
        return parcelDeliveryType;
    }

    public String getParcelPackingPreference() {
        return parcelPackingPreference;
    }

    public String getPickupTime() {
        return pickupTime;
    }

    public String getDropoffTime() {
        return dropoffTime;
    }

    public double getServiceCost() {
        return serviceCost;
    }

    public String getStatus() {
        return status;
    }

    public LocalTime getPaymentTime() {
        return paymentTime;
    }
}
