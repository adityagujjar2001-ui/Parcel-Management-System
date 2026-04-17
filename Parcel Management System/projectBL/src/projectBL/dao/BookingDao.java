package projectBL.dao;

import projectBL.model.Booking;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class BookingDao {
    public void save(Booking booking) throws SQLException {
        try (Connection connection = Database.getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO bookings (booking_id, user_name, recipient_name, recipient_address, recipient_pin, recipient_mobile, parcel_weight_gram, parcel_contents_description, parcel_delivery_type, parcel_packing_preference, pickup_time, dropoff_time, service_cost, status, payment_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)") ) {
            statement.setInt(1, booking.getBookingId());
            statement.setString(2, booking.getUserName());
            statement.setString(3, booking.getRecipientName());
            statement.setString(4, booking.getRecipientAddress());
            statement.setString(5, String.valueOf(booking.getRecipientPin()));
            statement.setString(6, booking.getRecipientMobile());
            statement.setFloat(7, booking.getParcelWeightGram());
            statement.setString(8, booking.getParcelContentsDescription());
            statement.setString(9, booking.getParcelDeliveryType());
            statement.setString(10, booking.getParcelPackingPreference());
            statement.setString(11, booking.getPickupTime());
            statement.setString(12, booking.getDropoffTime());
            statement.setDouble(13, booking.getServiceCost());
            statement.setString(14, booking.getStatus());
            statement.setString(15, booking.getPaymentTime().toString());
            statement.executeUpdate();
        }
    }

    public Optional<Booking> findByBookingId(int bookingId) throws SQLException {
        try (Connection connection = Database.getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "SELECT booking_id, user_name, recipient_name, recipient_address, recipient_pin, recipient_mobile, parcel_weight_gram, parcel_contents_description, parcel_delivery_type, parcel_packing_preference, pickup_time, dropoff_time, service_cost, status, payment_time FROM bookings WHERE booking_id = ?")) {
            statement.setInt(1, bookingId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(new Booking(resultSet.getInt("booking_id"), resultSet.getString("user_name"),
                            resultSet.getString("recipient_name"), resultSet.getString("recipient_address"),
                            Integer.parseInt(resultSet.getString("recipient_pin")),
                            resultSet.getString("recipient_mobile"), resultSet.getFloat("parcel_weight_gram"),
                            resultSet.getString("parcel_contents_description"),
                            resultSet.getString("parcel_delivery_type"),
                            resultSet.getString("parcel_packing_preference"),
                            resultSet.getString("pickup_time"), resultSet.getString("dropoff_time"),
                            resultSet.getDouble("service_cost"), resultSet.getString("status"),
                            java.time.LocalTime.parse(resultSet.getString("payment_time"))));
                }
                return Optional.empty();
            }
        }
    }

    public Optional<Booking> findLatestByUserName(String userName) throws SQLException {
        try (Connection connection = Database.getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "SELECT booking_id, user_name, recipient_name, recipient_address, recipient_pin, recipient_mobile, parcel_weight_gram, parcel_contents_description, parcel_delivery_type, parcel_packing_preference, pickup_time, dropoff_time, service_cost, status, payment_time FROM bookings WHERE user_name = ? ORDER BY created_at DESC LIMIT 1")) {
            statement.setString(1, userName);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(new Booking(resultSet.getInt("booking_id"), resultSet.getString("user_name"),
                            resultSet.getString("recipient_name"), resultSet.getString("recipient_address"),
                            Integer.parseInt(resultSet.getString("recipient_pin")),
                            resultSet.getString("recipient_mobile"), resultSet.getFloat("parcel_weight_gram"),
                            resultSet.getString("parcel_contents_description"),
                            resultSet.getString("parcel_delivery_type"),
                            resultSet.getString("parcel_packing_preference"),
                            resultSet.getString("pickup_time"), resultSet.getString("dropoff_time"),
                            resultSet.getDouble("service_cost"), resultSet.getString("status"),
                            java.time.LocalTime.parse(resultSet.getString("payment_time"))));
                }
                return Optional.empty();
            }
        }
    }

    public boolean existsByBookingId(int bookingId) throws SQLException {
        try (Connection connection = Database.getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "SELECT 1 FROM bookings WHERE booking_id = ?")) {
            statement.setInt(1, bookingId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public void updatePickupTime(int bookingId, String pickupTime, String dropoffTime) throws SQLException {
        try (Connection connection = Database.getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "UPDATE bookings SET pickup_time = ?, dropoff_time = ? WHERE booking_id = ?")) {
            statement.setString(1, pickupTime);
            statement.setString(2, dropoffTime);
            statement.setInt(3, bookingId);
            statement.executeUpdate();
        }
    }

    public void updateStatus(int bookingId, String status) throws SQLException {
        try (Connection connection = Database.getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "UPDATE bookings SET status = ? WHERE booking_id = ?")) {
            statement.setString(1, status);
            statement.setInt(2, bookingId);
            statement.executeUpdate();
        }
    }
}
