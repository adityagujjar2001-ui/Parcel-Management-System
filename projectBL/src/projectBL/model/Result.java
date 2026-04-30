package projectBL.model;

public class Result {
    private final boolean ok;
    private final String message;

    public Result(boolean ok, String message) {
        this.ok = ok;
        this.message = message;
    }

    public boolean isOk() {
        return ok;
    }

    public String getMessage() {
        return message;
    }

    public static class LoginResult extends Result {
        private final boolean officer;

        public LoginResult(boolean ok, String message, boolean officer) {
            super(ok, message);
            this.officer = officer;
        }

        public boolean isOfficer() {
            return officer;
        }
    }

    public static class BookingResult extends Result {
        private final int bookingId;

        public BookingResult(boolean ok, String message, int bookingId) {
            super(ok, message);
            this.bookingId = bookingId;
        }

        public int getBookingId() {
            return bookingId;
        }
    }

    public static class TrackResult extends Result {
        private final Booking booking;

        public TrackResult(boolean ok, String message, Booking booking) {
            super(ok, message);
            this.booking = booking;
        }

        public Booking getBooking() {
            return booking;
        }
    }

    public static class DashboardResult extends Result {
        private final User user;
        private final Booking booking;

        public DashboardResult(boolean ok, String message, User user, Booking booking) {
            super(ok, message);
            this.user = user;
            this.booking = booking;
        }

        public User getUser() {
            return user;
        }

        public Booking getBooking() {
            return booking;
        }
    }

    public static class InvoiceResult extends Result {
        private final User user;
        private final Booking booking;

        public InvoiceResult(boolean ok, String message, User user, Booking booking) {
            super(ok, message);
            this.user = user;
            this.booking = booking;
        }

        public User getUser() {
            return user;
        }

        public Booking getBooking() {
            return booking;
        }
    }
}
