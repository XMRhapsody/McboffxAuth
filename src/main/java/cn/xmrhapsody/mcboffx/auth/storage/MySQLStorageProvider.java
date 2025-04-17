package cn.xmrhapsody.mcboffx.auth.storage;

import org.bukkit.entity.Player;

import java.sql.*;
import java.util.UUID;

public class MySQLStorageProvider implements StorageProvider {

    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;
    private Connection connection;

    public MySQLStorageProvider(String host, int port, String database, String username, String password) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
    }

    @Override
    public boolean connect() {
        try {
            if (connection != null && !connection.isClosed()) {
                return true;
            }

            Class.forName("com.mysql.cj.jdbc.Driver");
            String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&autoReconnect=true";
            connection = DriverManager.getConnection(url, username, password);
            return true;
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void createStorageStructure() {
        try {
            if (connection == null || connection.isClosed()) {
                connect();
            }

            Statement statement = connection.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS auth_users (" +
                    "uuid VARCHAR(36) PRIMARY KEY, " +
                    "username VARCHAR(16) NOT NULL, " +
                    "password VARCHAR(128) NOT NULL, " +
                    "last_login TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "registered_ip VARCHAR(45), " +
                    "last_ip VARCHAR(45)" +
                    ")";
            statement.executeUpdate(sql);
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isRegistered(UUID uuid) {
        try {
            if (connection == null || connection.isClosed()) {
                connect();
            }

            PreparedStatement statement = connection.prepareStatement("SELECT * FROM auth_users WHERE uuid = ?");
            statement.setString(1, uuid.toString());
            ResultSet resultSet = statement.executeQuery();
            boolean exists = resultSet.next();
            resultSet.close();
            statement.close();
            return exists;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean isRegistered(String username) {
        try {
            if (connection == null || connection.isClosed()) {
                connect();
            }

            PreparedStatement statement = connection.prepareStatement("SELECT * FROM auth_users WHERE username = ?");
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            boolean exists = resultSet.next();
            resultSet.close();
            statement.close();
            return exists;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void registerPlayer(Player player, String password) {
        try {
            if (connection == null || connection.isClosed()) {
                connect();
            }

            String hashedPassword = hashPassword(password);
            String ip = player.getAddress().getAddress().getHostAddress();

            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO auth_users (uuid, username, password, registered_ip, last_ip) VALUES (?, ?, ?, ?, ?)");
            statement.setString(1, player.getUniqueId().toString());
            statement.setString(2, player.getName());
            statement.setString(3, hashedPassword);
            statement.setString(4, ip);
            statement.setString(5, ip);
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean checkPassword(UUID uuid, String password) {
        try {
            if (connection == null || connection.isClosed()) {
                connect();
            }

            PreparedStatement statement = connection.prepareStatement("SELECT password FROM auth_users WHERE uuid = ?");
            statement.setString(1, uuid.toString());
            ResultSet resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                String storedPassword = resultSet.getString("password");
                resultSet.close();
                statement.close();
                return verifyPassword(password, storedPassword);
            }
            
            resultSet.close();
            statement.close();
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void updateLastLogin(Player player) {
        try {
            if (connection == null || connection.isClosed()) {
                connect();
            }

            String ip = player.getAddress().getAddress().getHostAddress();
            
            PreparedStatement statement = connection.prepareStatement(
                    "UPDATE auth_users SET last_login = CURRENT_TIMESTAMP, last_ip = ? WHERE uuid = ?");
            statement.setString(1, ip);
            statement.setString(2, player.getUniqueId().toString());
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String hashPassword(String password) {
        // 实际应用中应使用更安全的哈希算法如BCrypt
        // 这里简单使用SHA-256以便示例
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private boolean verifyPassword(String password, String storedHash) {
        String hashedPassword = hashPassword(password);
        return hashedPassword.equals(storedHash);
    }
} 