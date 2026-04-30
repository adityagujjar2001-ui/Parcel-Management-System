package projectBL.model;

public class User {
    private final int id;
    private final String customerName;
    private final String email;
    private final String countryCode;
    private final String mobileNumber;
    private final String address;
    private final String userName;
    private final String password;
    private final String preferences;

    public User(int id, String customerName, String email, String countryCode, String mobileNumber, String address,
            String userName, String password, String preferences) {
        this.id = id;
        this.customerName = customerName;
        this.email = email;
        this.countryCode = countryCode;
        this.mobileNumber = mobileNumber;
        this.address = address;
        this.userName = userName;
        this.password = password;
        this.preferences = preferences;
    }

    public User(String customerName, String email, String countryCode, String mobileNumber, String address,
            String userName, String password, String preferences) {
        this(0, customerName, email, countryCode, mobileNumber, address, userName, password,
                preferences == null ? "" : preferences);
    }

    public int getId() {
        return id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getEmail() {
        return email;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public String getAddress() {
        return address;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public String getPreferences() {
        return preferences;
    }
}
