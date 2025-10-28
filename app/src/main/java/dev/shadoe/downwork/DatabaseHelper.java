package dev.shadoe.downwork;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Patterns;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper {
    private final SQLiteDatabase db;

    public DatabaseHelper(SQLiteDatabase db) {
        this.db = db;
    }

    // User type constants
    public static final int USER_TYPE_CUSTOMER = 0;
    public static final int USER_TYPE_PROFESSIONAL = 1;

    // Hash password using SHA-256
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return password; // Fallback (not recommended in production)
        }
    }

    // Validate email format
    public boolean isValidEmail(String email) {
        return email != null && !email.trim().isEmpty()
                && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    // Validate password (at least 6 characters)
    public boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    // Validate username (at least 2 characters)
    public boolean isValidUsername(String username) {
        return username != null && username.trim().length() >= 2;
    }

    // Check if email already exists
    public boolean emailExists(String email) {
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM Users WHERE mail_id = ?",
                new String[]{email}
        );
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return count > 0;
    }

    // Get next user ID
    private int getNextUserId() {
        Cursor cursor = db.rawQuery("SELECT IFNULL(MAX(uid), 0) + 1 FROM Users", null);
        cursor.moveToFirst();
        int nextId = cursor.getInt(0);
        cursor.close();
        return nextId;
    }

    // Sign up a new user
    public int signUpUser(String email, String password, String username, int userType) {
        int uid = getNextUserId();
        ContentValues values = new ContentValues();
        values.put("uid", uid);
        values.put("mail_id", email.trim().toLowerCase());
        values.put("pwd", hashPassword(password));
        values.put("user_name", username.trim());
        values.put("user_type", userType);

        long result = db.insert("Users", null, values);

        if (result != -1 && userType == USER_TYPE_PROFESSIONAL) {
            // Create empty portfolio entry for professionals
            ContentValues portfolioValues = new ContentValues();
            portfolioValues.put("uid", uid);
            portfolioValues.put("about", "");
            portfolioValues.put("star_rating", 0);
            db.insert("Portfolio", null, portfolioValues);
        }

        return result != -1 ? uid : -1;
    }

    // Sign in user
    public User signInUser(String email, String password) {
        Cursor cursor = db.rawQuery(
                "SELECT uid, mail_id, user_name, user_type FROM Users WHERE mail_id = ? AND pwd = ?",
                new String[]{email.trim().toLowerCase(), hashPassword(password)}
        );

        User user = null;
        if (cursor.moveToFirst()) {
            user = new User(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getInt(3)
            );
        }
        cursor.close();
        return user;
    }

    // Add skill for professional
    public boolean addSkill(int uid, String skill) {
        if (skill == null || skill.trim().isEmpty()) return false;

        ContentValues values = new ContentValues();
        values.put("uid", uid);
        values.put("skill", skill.trim());
        return db.insert("Skills", null, values) != -1;
    }

    // Get skills for a user
    public List<String> getSkills(int uid) {
        List<String> skills = new ArrayList<>();
        Cursor cursor = db.rawQuery(
                "SELECT skill FROM Skills WHERE uid = ?",
                new String[]{String.valueOf(uid)}
        );
        while (cursor.moveToNext()) {
            skills.add(cursor.getString(0));
        }
        cursor.close();
        return skills;
    }

    // Add service for professional
    public boolean addService(int uid, String serviceName, String serviceDesc, double rate) {
        ContentValues values = new ContentValues();
        values.put("uid", uid);
        values.put("service_name", serviceName.trim());
        values.put("service_desc", serviceDesc.trim());
        values.put("rate", rate);
        return db.insert("Services", null, values) != -1;
    }

    // Get services for a user
    public List<Service> getServices(int uid) {
        List<Service> services = new ArrayList<>();
        Cursor cursor = db.rawQuery(
                "SELECT service_name, service_desc, rate FROM Services WHERE uid = ?",
                new String[]{String.valueOf(uid)}
        );
        while (cursor.moveToNext()) {
            services.add(new Service(
                    cursor.getString(0),
                    cursor.getString(1),
                    cursor.getDouble(2)
            ));
        }
        cursor.close();
        return services;
    }

    // Fuzzy search for professionals by keyword (searches in username and service names)
    public List<Professional> searchProfessionals(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllProfessionals();
        }

        List<Professional> professionals = new ArrayList<>();
        String searchPattern = "%" + keyword.trim() + "%";

        Cursor cursor = db.rawQuery(
                "SELECT DISTINCT u.uid, u.user_name, p.about, p.star_rating " +
                        "FROM Users u " +
                        "LEFT JOIN Portfolio p ON u.uid = p.uid " +
                        "LEFT JOIN Services s ON u.uid = s.uid " +
                        "WHERE u.user_type = ? AND (" +
                        "u.user_name LIKE ? OR s.service_name LIKE ?)",
                new String[]{
                        String.valueOf(USER_TYPE_PROFESSIONAL),
                        searchPattern,
                        searchPattern
                }
        );

        while (cursor.moveToNext()) {
            int uid = cursor.getInt(0);
            Professional prof = new Professional(
                    uid,
                    cursor.getString(1),
                    cursor.getString(2) != null ? cursor.getString(2) : "",
                    cursor.getInt(3),
                    getSkills(uid),
                    getServices(uid)
            );
            professionals.add(prof);
        }
        cursor.close();
        return professionals;
    }

    // Get all professionals with their services
    public List<Professional> getAllProfessionals() {
        List<Professional> professionals = new ArrayList<>();
        Cursor cursor = db.rawQuery(
                "SELECT DISTINCT u.uid, u.user_name, p.about, p.star_rating " +
                        "FROM Users u " +
                        "LEFT JOIN Portfolio p ON u.uid = p.uid " +
                        "WHERE u.user_type = ?",
                new String[]{String.valueOf(USER_TYPE_PROFESSIONAL)}
        );

        while (cursor.moveToNext()) {
            int uid = cursor.getInt(0);
            Professional prof = new Professional(
                    uid,
                    cursor.getString(1),
                    cursor.getString(2) != null ? cursor.getString(2) : "",
                    cursor.getInt(3),
                    getSkills(uid),
                    getServices(uid)
            );
            professionals.add(prof);
        }
        cursor.close();
        return professionals;
    }

    // Update portfolio
    public boolean updatePortfolio(int uid, String about) {
        ContentValues values = new ContentValues();
        values.put("about", about.trim());
        return db.update("Portfolio", values, "uid = ?",
                new String[]{String.valueOf(uid)}) > 0;
    }

    // Get portfolio
    public Portfolio getPortfolio(int uid) {
        Cursor cursor = db.rawQuery(
                "SELECT about, star_rating FROM Portfolio WHERE uid = ?",
                new String[]{String.valueOf(uid)}
        );

        Portfolio portfolio = null;
        if (cursor.moveToFirst()) {
            portfolio = new Portfolio(
                    cursor.getString(0) != null ? cursor.getString(0) : "",
                    cursor.getInt(1)
            );
        }
        cursor.close();
        return portfolio;
    }

    // Data classes
    public static class User {
        public final int uid;
        public final String email;
        public final String username;
        public final int userType;

        public User(int uid, String email, String username, int userType) {
            this.uid = uid;
            this.email = email;
            this.username = username;
            this.userType = userType;
        }
    }

    public static class Service {
        public final String name;
        public final String description;
        public final double rate;

        public Service(String name, String description, double rate) {
            this.name = name;
            this.description = description;
            this.rate = rate;
        }
    }

    public static class Professional {
        public final int uid;
        public final String username;
        public final String about;
        public final int rating;
        public final List<String> skills;
        public final List<Service> services;

        public Professional(int uid, String username, String about, int rating,
                            List<String> skills, List<Service> services) {
            this.uid = uid;
            this.username = username;
            this.about = about;
            this.rating = rating;
            this.skills = skills;
            this.services = services;
        }
    }

    public static class Portfolio {
        public final String about;
        public final int rating;

        public Portfolio(String about, int rating) {
            this.about = about;
            this.rating = rating;
        }
    }
}