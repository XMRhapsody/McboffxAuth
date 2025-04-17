package cn.xmrhapsody.mcboffx.auth.storage;

import cn.xmrhapsody.mcboffx.auth.McboffxAuth;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class FileStorageProvider implements StorageProvider {

    private final McboffxAuth plugin;
    private final File dataFile;
    private YamlConfiguration playerData;
    
    public FileStorageProvider(McboffxAuth plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "playerdata.yml");
        this.playerData = new YamlConfiguration();
    }
    
    @Override
    public boolean connect() {
        if (dataFile.exists()) {
            try {
                playerData = YamlConfiguration.loadConfiguration(dataFile);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return createStorageFile();
        }
    }
    
    private boolean createStorageFile() {
        try {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }
            
            dataFile.createNewFile();
            playerData = YamlConfiguration.loadConfiguration(dataFile);
            playerData.options().header("McboffxAuth - 玩家数据文件\n" +
                    "警告: 请勿手动编辑此文件，除非你知道你在做什么！");
            playerData.createSection("players");
            saveData();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public void close() {
        saveData();
    }
    
    @Override
    public void createStorageStructure() {
        // 文件存储不需要额外创建结构，connect方法已经处理了
    }
    
    @Override
    public boolean isRegistered(UUID uuid) {
        return playerData.isConfigurationSection("players." + uuid.toString());
    }
    
    @Override
    public boolean isRegistered(String username) {
        ConfigurationSection playersSection = playerData.getConfigurationSection("players");
        if (playersSection == null) {
            return false;
        }
        
        for (String key : playersSection.getKeys(false)) {
            String storedUsername = playersSection.getString(key + ".username");
            if (username.equalsIgnoreCase(storedUsername)) {
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public void registerPlayer(Player player, String password) {
        String uuidString = player.getUniqueId().toString();
        String hashedPassword = hashPassword(password);
        String ip = player.getAddress().getAddress().getHostAddress();
        String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        
        ConfigurationSection playerSection = playerData.createSection("players." + uuidString);
        playerSection.set("username", player.getName());
        playerSection.set("password", hashedPassword);
        playerSection.set("registered_date", currentTime);
        playerSection.set("last_login", currentTime);
        playerSection.set("registered_ip", ip);
        playerSection.set("last_ip", ip);
        
        saveData();
    }
    
    @Override
    public boolean checkPassword(UUID uuid, String password) {
        String uuidString = uuid.toString();
        
        if (!isRegistered(uuid)) {
            return false;
        }
        
        String storedPassword = playerData.getString("players." + uuidString + ".password");
        return verifyPassword(password, storedPassword);
    }
    
    @Override
    public void updateLastLogin(Player player) {
        String uuidString = player.getUniqueId().toString();
        String ip = player.getAddress().getAddress().getHostAddress();
        String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        
        playerData.set("players." + uuidString + ".last_login", currentTime);
        playerData.set("players." + uuidString + ".last_ip", ip);
        
        saveData();
    }
    
    private void saveData() {
        try {
            playerData.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("无法保存玩家数据文件！");
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