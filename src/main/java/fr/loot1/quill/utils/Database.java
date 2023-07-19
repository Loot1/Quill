package fr.loot1.quill.utils;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import fr.loot1.quill.Quill;
import fr.loot1.quill.objects.Application;
import fr.loot1.quill.objects.ApplicationList;

public class Database {

    final static Quill main = Quill.getInstance();

    final static String username = Config.get("database.username");
    final static String password = Config.get("database.password");
    final static String url = "jdbc:mysql://" + Config.get("database.url") + "/" + Config.get("database.name");

    static Connection connection;

    public static void connect() {
        try {
            connection = DriverManager.getConnection(url, username, password);
            main.getLogger().info("Database connection successful");

            PreparedStatement stmt = connection.prepareStatement("CREATE TABLE IF NOT EXISTS quill ("
                    + "id INT PRIMARY KEY AUTO_INCREMENT,"
                    + "uuid CHAR(36) NOT NULL,"
                    + "createdAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                    + "status INT(1) NOT NULL,"
                    + "title CHAR(32) NOT NULL,"
                    + "content TEXT"
                    + ")");

            stmt.execute();
        } catch (SQLException e) {
            main.getLogger().severe("Database connection error :");
            e.printStackTrace();
            main.getLogger().severe("Database connection failed. Plugin shutdown.");
            main.getServer().getPluginManager().disablePlugin(main);
        }
    }

    public static void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                main.getLogger().info("Database connection closed");
            }
        } catch(Exception e) {
            main.getLogger().severe("Database connection close failed :");
            e.printStackTrace();
        }
    }

    public static boolean addApplication(final UUID uuid, final String title, final String content) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO quill(id, uuid, createdAt, status, title, content) VALUES (?, ?, CURRENT_TIMESTAMP, 0, ?, ?)"
        )) {
            stmt.setNull(1, Types.NULL);
            stmt.setString(2, uuid.toString());
            stmt.setString(3, title);
            stmt.setString(4, content);
            stmt.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static ApplicationList getApplications(final int limit, final int offset) {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT *, (SELECT COUNT(*) FROM quill) AS bookcount FROM quill ORDER BY createdAt DESC LIMIT " + limit + " OFFSET " + offset)) {
            ResultSet rs = stmt.executeQuery();
            List<Application> applications = new ArrayList<>();
            int count = 0;
            while (rs.next()) {
                count = rs.getInt("bookcount");
                applications.add(new Application(rs));
            }
            return new ApplicationList(applications, count);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ApplicationList getApplicationsByStatus(final boolean waiting, final boolean accepted, final boolean refused, final boolean archived, final int limit, final int offset) {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT *, (SELECT COUNT(*) FROM quill WHERE (status IN (?, ?, ?, ?) OR NOT ? AND NOT ? AND NOT ? AND NOT ?)) AS bookcount FROM quill WHERE (status IN (?, ?, ?, ?) OR NOT ? AND NOT ? AND NOT ? AND NOT ?) ORDER BY createdAt DESC LIMIT " + limit + " OFFSET " + offset)) {
            stmt.setInt(1, waiting ? 0 : 9);
            stmt.setInt(2, accepted ? 1 : 9);
            stmt.setInt(3, refused ? 2 : 9);
            stmt.setInt(4, archived ? 3 : 9);
            stmt.setBoolean(5, waiting);
            stmt.setBoolean(6, accepted);
            stmt.setBoolean(7, refused);
            stmt.setBoolean(8, archived);
            stmt.setInt(9, waiting ? 0 : 9);
            stmt.setInt(10, accepted ? 1 : 9);
            stmt.setInt(11, refused ? 2 : 9);
            stmt.setInt(12, archived ? 3 : 9);
            stmt.setBoolean(13, waiting);
            stmt.setBoolean(14, accepted);
            stmt.setBoolean(15, refused);
            stmt.setBoolean(16, archived);
            ResultSet rs = stmt.executeQuery();
            List<Application> applications = new ArrayList<>();
            int count = 0;
            while (rs.next()) {
                count = rs.getInt("bookcount");
                applications.add(new Application(rs));
            }
            return new ApplicationList(applications, count);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ApplicationList getPlayerApplications(final UUID uuid, final int limit, final int offset) {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT *, (SELECT COUNT(*) FROM quill WHERE uuid = ?) AS bookcount FROM quill WHERE uuid = ? ORDER BY createdAt DESC LIMIT " + limit + " OFFSET " + offset)) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, uuid.toString());
            ResultSet rs = stmt.executeQuery();
            List<Application> applications = new ArrayList<>();
            int count = 0;
            while (rs.next()) {
                count = rs.getInt("bookcount");
                applications.add(new Application(rs));
            }
            return new ApplicationList(applications, count);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Application getApplicationById(final Integer id) {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM quill WHERE id = ?")) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if(rs.next()) {
                return new Application(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean updateApplicationStatus(final int id, final int status) {
        try (PreparedStatement stmt = connection.prepareStatement("UPDATE quill SET status = ? WHERE id = ?")) {
            stmt.setInt(1, status);
            stmt.setInt(2, id);
            stmt.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static final String encodingValue = "a&c@`-8a%";

    public static String encode(final List<String> toEncode) {
        return String.join(encodingValue, toEncode);
    }

    public static List<String> decode(final String toDecode) {
        return Arrays.asList(toDecode.split(encodingValue));
    }

}