package projectBL.service;

import projectBL.dao.BookingDao;
import projectBL.dao.UserDao;
import projectBL.model.Booking;
import projectBL.model.Result;
import projectBL.model.User;

import java.sql.SQLException;
import java.time.LocalTime;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AppService {
    private final UserDao userDao = new UserDao();
    private final BookingDao bookingDao = new BookingDao();
    private static final String OFFICER_USER = "officer";
    private static final String OFFICER_PASS = "Pass@123";

    public Result register(String customerName, String email, String countryCode, String mobileNumber,
            String address, String userName, String password, String confirmPassword, String preferences) {
        if (customerName == null || customerName.isBlank()) {
            return new Result(false, "Customer name is required.");
        }
        if (customerName.length() > 50) {
            return new Result(false, "Customer name must be 50 characters or fewer.");
        }
        if (email == null || email.isBlank() || !email.contains("@")) {
            return new Result(false, "Please provide a valid email address.");
        }
        if (countryCode == null || countryCode.isBlank()) {
            return new Result(false, "Country code is required.");
        }
        if (mobileNumber == null || !mobileNumber.matches("\\d{10}")) {
            return new Result(false, "Mobile number must be 10 digits.");
        }
        if (address == null || address.isBlank()) {
            return new Result(false, "Address is required.");
        }
        if (userName == null || userName.length() < 5 || userName.length() > 20) {
            return new Result(false, "User name must be 5 to 20 characters.");
        }
        if (!validatePassword(password)) {
            return new Result(false,
                    "Password must be 8-30 characters, include uppercase, lowercase, number, and special character.");
        }
        if (!password.equals(confirmPassword)) {
            return new Result(false, "Passwords do not match.");
        }

        try {
            if (userDao.existsByUserName(userName)) {
                return new Result(false, "User name already exists.");
            }
            User user = new User(customerName, email, countryCode, mobileNumber, address, userName, password,
                    preferences == null ? "" : preferences);
            userDao.save(user);
            return new Result(true, "Registration completed successfully. Please log in.");
        } catch (SQLException e) {
            return new Result(false, "Registration failed: " + e.getMessage());
        }
    }

    public Result.LoginResult login(String userName, String password) {
        if (userName == null || password == null) {
            return new Result.LoginResult(false, "Username and password are required.", false);
        }
        if (OFFICER_USER.equals(userName) && OFFICER_PASS.equals(password)) {
            return new Result.LoginResult(true, "Officer login successful.", true);
        }
        try {
            User user = userDao.findByUserName(userName);
            if (user == null || !user.getPassword().equals(password)) {
                return new Result.LoginResult(false, "Invalid username or password.", false);
            }
            return new Result.LoginResult(true, "User login successful.", false);
        } catch (SQLException e) {
            return new Result.LoginResult(false, "Login failed: " + e.getMessage(), false);
        }
    }

    public Result.BookingResult book(String userName, String recipientName, String recipientAddress, String recipientPin,
            String recipientMobile, String parcelWeightGram, String parcelContentsDescription,
            String parcelDeliveryType, String parcelPackingPreference, String pickupTime, String dropoffTime,
            String serviceCost) {
        if (userName == null || userName.isBlank()) {
            return new Result.BookingResult(false, "Session not found. Please log in.", -1);
        }
        if (recipientName == null || recipientName.isBlank()) {
            return new Result.BookingResult(false, "Recipient name is required.", -1);
        }
        if (recipientAddress == null || recipientAddress.isBlank()) {
            return new Result.BookingResult(false, "Recipient address is required.", -1);
        }
        if (recipientPin == null || !recipientPin.matches("\\d{5,6}")) {
            return new Result.BookingResult(false, "Recipient pin must be 5 or 6 digits.", -1);
        }
        if (recipientMobile == null || !recipientMobile.matches("\\d{10}")) {
            return new Result.BookingResult(false, "Recipient mobile must be 10 digits.", -1);
        }
        if (parcelWeightGram == null || !parcelWeightGram.matches("\\d+(\\.\\d+)?")) {
            return new Result.BookingResult(false, "Parcel weight must be a valid number.", -1);
        }
        if (serviceCost == null || !serviceCost.matches("\\d+(\\.\\d+)?")) {
            return new Result.BookingResult(false, "Service cost must be a valid number.", -1);
        }
        try {
            User user = userDao.findByUserName(userName);
            if (user == null) {
                return new Result.BookingResult(false, "User session invalid. Please log in again.", -1);
            }
            int id = generateUniqueBookingId();
            float weight = Float.parseFloat(parcelWeightGram);
            double cost = Double.parseDouble(serviceCost);
            String status = "Booked";
            LocalTime paymentTime = LocalTime.now();
            Booking booking = new Booking(id, userName, recipientName, recipientAddress, Integer.parseInt(recipientPin),
                    recipientMobile, weight, parcelContentsDescription, parcelDeliveryType, parcelPackingPreference,
                    pickupTime, dropoffTime, cost, status, paymentTime);
            bookingDao.save(booking);
            return new Result.BookingResult(true, "Booking confirmed.", id);
        } catch (SQLException e) {
            return new Result.BookingResult(false, "Booking failed: " + e.getMessage(), -1);
        }
    }

    public Result.TrackResult track(String bookingIdString) {
        if (bookingIdString == null || !bookingIdString.matches("\\d+")) {
            return new Result.TrackResult(false, "A valid booking ID is required.", null);
        }
        try {
            Optional<Booking> booking = bookingDao.findByBookingId(Integer.parseInt(bookingIdString));
            if (booking.isEmpty()) {
                return new Result.TrackResult(false, "Booking ID not found.", null);
            }
            return new Result.TrackResult(true, "Booking found.", booking.get());
        } catch (SQLException e) {
            return new Result.TrackResult(false, "Tracking failed: " + e.getMessage(), null);
        }
    }

    public Result.DashboardResult dashboard(String userName) {
        if (userName == null || userName.isBlank()) {
            return new Result.DashboardResult(false, "No registered user found.", null, null);
        }
        try {
            User user = userDao.findByUserName(userName);
            if (user == null) {
                return new Result.DashboardResult(false, "User not found.", null, null);
            }
            Optional<Booking> booking = bookingDao.findLatestByUserName(userName);
            return new Result.DashboardResult(true, "Dashboard loaded.", user, booking.orElse(null));
        } catch (SQLException e) {
            return new Result.DashboardResult(false, "Dashboard failed: " + e.getMessage(), null, null);
        }
    }

    public Result.InvoiceResult invoice(String bookingIdString) {
        if (bookingIdString == null || !bookingIdString.matches("\\d+")) {
            return new Result.InvoiceResult(false, "A valid booking ID is required.", null, null);
        }
        try {
            Optional<Booking> booking = bookingDao.findByBookingId(Integer.parseInt(bookingIdString));
            if (booking.isEmpty()) {
                return new Result.InvoiceResult(false, "Booking ID not found.", null, null);
            }
            User user = userDao.findByUserName(booking.get().getUserName());
            return new Result.InvoiceResult(true, "Invoice generated.", user, booking.get());
        } catch (SQLException e) {
            return new Result.InvoiceResult(false, "Invoice failed: " + e.getMessage(), null, null);
        }
    }

    public Result updatePickup(String bookingIdString, String pickupTime, String dropoffTime) {
        if (bookingIdString == null || !bookingIdString.matches("\\d+")) {
            return new Result(false, "A valid booking ID is required.");
        }
        try {
            Optional<Booking> booking = bookingDao.findByBookingId(Integer.parseInt(bookingIdString));
            if (booking.isEmpty()) {
                return new Result(false, "Booking ID not found.");
            }
            bookingDao.updatePickupTime(booking.get().getBookingId(), pickupTime, dropoffTime);
            return new Result(true, "Pickup and dropoff times updated successfully.");
        } catch (SQLException e) {
            return new Result(false, "Update failed: " + e.getMessage());
        }
    }

    public Result changeParcelStatus(String bookingIdString, String newStatus) {
        if (bookingIdString == null || !bookingIdString.matches("\\d+")) {
            return new Result(false, "A valid booking ID is required.");
        }
        if (newStatus == null || newStatus.isBlank()) {
            return new Result(false, "Parcel status cannot be empty.");
        }
        try {
            Optional<Booking> booking = bookingDao.findByBookingId(Integer.parseInt(bookingIdString));
            if (booking.isEmpty()) {
                return new Result(false, "Booking ID not found.");
            }
            bookingDao.updateStatus(booking.get().getBookingId(), newStatus);
            return new Result(true, "Parcel status updated to " + newStatus + ".");
        } catch (SQLException e) {
            return new Result(false, "Status update failed: " + e.getMessage());
        }
    }

    private boolean validatePassword(String password) {
        if (password == null) {
            return false;
        }
        String regex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,30}$";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(password);
        return m.matches();
    }

    private int generateUniqueBookingId() throws SQLException {
        int id;
        do {
            id = new Random().nextInt(9000) + 1000;
        } while (bookingDao.existsByBookingId(id));
        return id;
    }
}
