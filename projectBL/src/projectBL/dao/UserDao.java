package projectBL.dao;

import projectBL.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDao {
    public User findByUserName(String userName) throws SQLException {
        try (Connection connection = Database.getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "SELECT id, customer_name, email, country_code, mobile_number, address, user_name, password, preferences FROM users WHERE user_name = ?")) {
            statement.setString(1, userName);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new User(resultSet.getInt("id"), resultSet.getString("customer_name"),
                            resultSet.getString("email"), resultSet.getString("country_code"),
                            resultSet.getString("mobile_number"), resultSet.getString("address"),
                            resultSet.getString("user_name"), resultSet.getString("password"),
                            resultSet.getString("preferences"));
                }
                return null;
            }
        }
    }

    public boolean existsByUserName(String userName) throws SQLException {
        return findByUserName(userName) != null;
    }

    public void save(User user) throws SQLException {
        try (Connection connection = Database.getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO users (customer_name, email, country_code, mobile_number, address, user_name, password, preferences) VALUES (?, ?, ?, ?, ?, ?, ?, ?)") ) {
            statement.setString(1, user.getCustomerName());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getCountryCode());
            statement.setString(4, user.getMobileNumber());
            statement.setString(5, user.getAddress());
            statement.setString(6, user.getUserName());
            statement.setString(7, user.getPassword());
            statement.setString(8, user.getPreferences());
            statement.executeUpdate();
        }
    }
}
