package cn.xmrhapsody.mcboffx.auth;

import cn.xmrhapsody.mcboffx.auth.commands.LoginCommand;
import cn.xmrhapsody.mcboffx.auth.commands.RegisterCommand;
import cn.xmrhapsody.mcboffx.auth.commands.SetLoginSpawnCommand;
import cn.xmrhapsody.mcboffx.auth.listeners.PlayerListener;
import cn.xmrhapsody.mcboffx.auth.storage.FileStorageProvider;
import cn.xmrhapsody.mcboffx.auth.storage.MySQLStorageProvider;
import cn.xmrhapsody.mcboffx.auth.storage.StorageProvider;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class McboffxAuth extends JavaPlugin {

    private static McboffxAuth instance;
    private StorageProvider storageProvider;
    
    @Override
    public void onEnable() {
        instance = this;
        
        // 保存默认配置
        saveDefaultConfig();
        reloadConfig();
        
        // 初始化存储
        initStorage();
        
        // 注册命令
        registerCommands();
        
        // 注册事件监听器
        registerListeners();
        
        getLogger().info("[Mcboffx] 登录插件已启用!-Power by XMRhapsody");
    }
    
    @Override
    public void onDisable() {
        // 关闭存储连接
        if (storageProvider != null) {
            storageProvider.close();
        }
        
        getLogger().info("[Mcboffx] 登录插件已禁用!-Power by XMRhapsody");
    }
    
    private void initStorage() {
        FileConfiguration config = getConfig();
        String storageType = config.getString("storage.type", "mysql").toLowerCase();
        
        if (storageType.equals("mysql")) {
            String host = config.getString("mysql.host", "localhost");
            int port = config.getInt("mysql.port", 3306);
            String database = config.getString("mysql.database", "minecraft");
            String username = config.getString("mysql.username", "root");
            String password = config.getString("mysql.password", "");
            
            storageProvider = new MySQLStorageProvider(host, port, database, username, password);
            getLogger().info("使用MySQL存储数据");
        } else if (storageType.equals("file")) {
            storageProvider = new FileStorageProvider(this);
            getLogger().info("使用本地文件存储数据");
        } else {
            getLogger().warning("未知的存储类型: " + storageType + "，将使用默认的文件存储");
            storageProvider = new FileStorageProvider(this);
        }
        
        // 连接到存储
        if (storageProvider.connect()) {
            storageProvider.createStorageStructure();
        } else {
            getLogger().severe("无法连接到存储！插件功能可能无法正常工作");
        }
    }
    
    private void registerCommands() {
        getCommand("login").setExecutor(new LoginCommand(this));
        getCommand("register").setExecutor(new RegisterCommand(this));
        getCommand("setloginspawn").setExecutor(new SetLoginSpawnCommand(this));
    }
    
    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
    }
    
    public static McboffxAuth getInstance() {
        return instance;
    }
    
    public StorageProvider getStorageProvider() {
        return storageProvider;
    }
} 