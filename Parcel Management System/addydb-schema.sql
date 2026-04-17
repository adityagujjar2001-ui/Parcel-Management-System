-- Create database and tables for projectBL
-- Run this in MySQL as root or a user with CREATE DATABASE permissions.

CREATE DATABASE IF NOT EXISTS addydb;
USE addydb;

CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    customer_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL,
    country_code VARCHAR(10) NOT NULL,
    mobile_number VARCHAR(20) NOT NULL,
    address VARCHAR(255) NOT NULL,
    user_name VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    preferences TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS bookings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    booking_id INT NOT NULL UNIQUE,
    user_name VARCHAR(50) NOT NULL,
    recipient_name VARCHAR(100) NOT NULL,
    recipient_address VARCHAR(255) NOT NULL,
    recipient_pin VARCHAR(10) NOT NULL,
    recipient_mobile VARCHAR(20) NOT NULL,
    parcel_weight_gram FLOAT NOT NULL,
    parcel_contents_description TEXT NOT NULL,
    parcel_delivery_type VARCHAR(50) NOT NULL,
    parcel_packing_preference VARCHAR(50) NOT NULL,
    pickup_time VARCHAR(50),
    dropoff_time VARCHAR(50),
    service_cost DOUBLE NOT NULL,
    status VARCHAR(50) NOT NULL,
    payment_time VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;
