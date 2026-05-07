package fr.loot1.quill.managers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import fr.loot1.quill.Quill;
import fr.loot1.quill.objects.Application;
import fr.loot1.quill.objects.ApplicationList;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class DatabaseManager {

    private final Quill main;
    private volatile HikariDataSource dataSource;

    private static final Gson GSON = new Gson();
    private static final String LEGACY_SEPARATOR = "a&c@`-8a%";

    public DatabaseManager(Quill quill) {
        this.main = quill;
        try {
            this.dataSource = buildDataSource(quill.getConfigManager());
            initTables();
        } catch (Exception e) {
            main.getLogger().log(Level.SEVERE, "Database initialization failed", e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    private HikariDataSource buildDataSource(ConfigManager configManager) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl("jdbc:mysql://" + configManager.get("database.url") + "/" + configManager.get("database.name"));
        hikariConfig.setUsername(configManager.get("database.username"));
        hikariConfig.setPassword(configManager.get("database.password"));
        hikariConfig.setMaximumPoolSize(10);
        hikariConfig.setMinimumIdle(2);
        hikariConfig.setIdleTimeout(60000);
        hikariConfig.setMaxLifetime(1800000);
        hikariConfig.setLeakDetectionThreshold(5000);
        return new HikariDataSource(hikariConfig);
    }

    public void reload(ConfigManager configManager) {
        close();
        try {
            this.dataSource = buildDataSource(configManager);
            initTables();
            main.getLogger().info("Database connection reloaded successfully.");
        } catch (Exception e) {
            main.getLogger().log(Level.SEVERE, "Database reload failed", e);
            throw new RuntimeException("Database reload failed", e);
        }
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    private void initTables() throws SQLException {
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(
                "CREATE TABLE IF NOT EXISTS quill ("
                        + "id INT PRIMARY KEY AUTO_INCREMENT,"
                        + "uuid CHAR(36) NOT NULL,"
                        + "createdAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                        + "status INT(1) NOT NULL,"
                        + "title CHAR(32) NOT NULL,"
                        + "content MEDIUMTEXT"
                        + ")"
        )) {
            stmt.execute();
            main.getLogger().info("Database connection successful");
        } catch (SQLException e) {
            main.getLogger().log(Level.SEVERE, "Database table initialization error", e);
            throw e;
        }
    }

    public void close() {
        try {
            if (!dataSource.isClosed()) {
                dataSource.close();
                main.getLogger().info("Database connection closed");
            }
        } catch (Exception e) {
            main.getLogger().log(Level.SEVERE, "Database connection close failed", e);
        }
    }

    public boolean addApplication(final UUID uuid, final String title, final String content) {
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO quill(uuid, createdAt, status, title, content) VALUES (?, CURRENT_TIMESTAMP, 0, ?, ?)"
        )) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, title);
            stmt.setString(3, content);
            stmt.execute();
            return true;
        } catch (SQLException e) {
            main.getLogger().log(Level.SEVERE, "Error adding application", e);
        }
        return false;
    }

    private int countAllApplications() {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM quill");
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            main.getLogger().log(Level.SEVERE, "Error counting applications", e);
        }
        return 0;
    }

    private int countApplicationsByStatuses(List<Integer> statuses) {
        if (statuses.isEmpty()) return countAllApplications();
        String placeholders = statuses.stream().map(s -> "?").collect(Collectors.joining(","));
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT COUNT(*) FROM quill WHERE status IN (" + placeholders + ")")) {
            for (int i = 0; i < statuses.size(); i++) {
                stmt.setInt(i + 1, statuses.get(i));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            main.getLogger().log(Level.SEVERE, "Error counting applications by status", e);
        }
        return 0;
    }

    private int countPlayerApplications(UUID uuid) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT COUNT(*) FROM quill WHERE uuid = ?")) {
            stmt.setString(1, uuid.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            main.getLogger().log(Level.SEVERE, "Error counting player applications", e);
        }
        return 0;
    }

    public ApplicationList getApplications(final int limit, final int offset) {
        int count = countAllApplications();
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM quill ORDER BY createdAt DESC LIMIT ? OFFSET ?"
        )) {
            stmt.setInt(1, limit);
            stmt.setInt(2, offset);
            try (ResultSet rs = stmt.executeQuery()) {
                List<Application> applications = new ArrayList<>();
                while (rs.next()) {
                    applications.add(new Application(rs, main));
                }
                return new ApplicationList(applications, count);
            }
        } catch (SQLException e) {
            main.getLogger().log(Level.SEVERE, "Error getting applications", e);
        }
        return null;
    }

    public ApplicationList getApplicationsByStatus(final boolean waiting, final boolean accepted, final boolean refused, final boolean archived, final int limit, final int offset) {
        List<Integer> statuses = new ArrayList<>();
        if (waiting)  statuses.add(0);
        if (accepted) statuses.add(1);
        if (refused)  statuses.add(2);
        if (archived) statuses.add(3);

        int count = countApplicationsByStatuses(statuses);

        String whereClause = statuses.isEmpty()
                ? ""
                : "WHERE status IN (" + statuses.stream().map(s -> "?").collect(Collectors.joining(",")) + ")";
        String query = "SELECT * FROM quill " + whereClause + " ORDER BY createdAt DESC LIMIT ? OFFSET ?";

        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            int idx = 1;
            for (Integer s : statuses) {
                stmt.setInt(idx++, s);
            }
            stmt.setInt(idx++, limit);
            stmt.setInt(idx, offset);
            try (ResultSet rs = stmt.executeQuery()) {
                List<Application> applications = new ArrayList<>();
                while (rs.next()) {
                    applications.add(new Application(rs, main));
                }
                return new ApplicationList(applications, count);
            }
        } catch (SQLException e) {
            main.getLogger().log(Level.SEVERE, "Error getting applications by status", e);
        }
        return null;
    }

    public ApplicationList getPlayerApplications(final UUID uuid, final int limit, final int offset) {
        int count = countPlayerApplications(uuid);
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM quill WHERE uuid = ? ORDER BY createdAt DESC LIMIT ? OFFSET ?"
        )) {
            stmt.setString(1, uuid.toString());
            stmt.setInt(2, limit);
            stmt.setInt(3, offset);
            try (ResultSet rs = stmt.executeQuery()) {
                List<Application> applications = new ArrayList<>();
                while (rs.next()) {
                    applications.add(new Application(rs, main));
                }
                return new ApplicationList(applications, count);
            }
        } catch (SQLException e) {
            main.getLogger().log(Level.SEVERE, "Error getting player applications", e);
        }
        return null;
    }

    public Application getApplicationById(final Integer id) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM quill WHERE id = ?")) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Application(rs, main);
                }
            }
        } catch (SQLException e) {
            main.getLogger().log(Level.SEVERE, "Error getting application by id", e);
        }
        return null;
    }

    public boolean updateApplicationStatus(final int id, final int status) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement("UPDATE quill SET status = ? WHERE id = ?")) {
            stmt.setInt(1, status);
            stmt.setInt(2, id);
            stmt.execute();
            return true;
        } catch (SQLException e) {
            main.getLogger().log(Level.SEVERE, "Error updating application status", e);
        }
        return false;
    }

    public int countWaitingApplications() {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM quill WHERE status = 0");
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            main.getLogger().log(Level.SEVERE, "Error counting waiting applications", e);
        }
        return 0;
    }

    public String encode(final List<String> toEncode) {
        return GSON.toJson(toEncode);
    }

    public List<String> decode(final String toDecode) {
        if (toDecode == null || toDecode.isEmpty()) return List.of();
        try {
            if (toDecode.startsWith("[")) {
                return GSON.fromJson(toDecode, new TypeToken<List<String>>() {}.getType());
            }
            return Arrays.asList(toDecode.split(java.util.regex.Pattern.quote(LEGACY_SEPARATOR)));
        } catch (Exception e) {
            main.getLogger().warning("Failed to decode book content, returning empty pages: " + e.getMessage());
            return List.of();
        }
    }

}