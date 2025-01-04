package com.flappybird.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    // Define database URL, username, and password
    private static final String URL = "jdbc:mysql://localhost:3306/flappybird_leaderboard";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    // Method to establish a connection to the database
    public Connection connect() {
        try {
            // Load the database driver (for MySQL in this case)
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Establish and return the connection
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Error establishing database connection: " + e.getMessage());
            return null;
        }
    }
}
