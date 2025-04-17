package cn.xmrhapsody.mcboffx.auth.storage;

import org.bukkit.entity.Player;

import java.util.UUID;

public interface StorageProvider {
    
    /**
     * 连接到存储
     * @return 是否连接成功
     */
    boolean connect();
    
    /**
     * 关闭存储连接
     */
    void close();
    
    /**
     * 创建必要的存储结构（表或文件）
     */
    void createStorageStructure();
    
    /**
     * 检查玩家是否已注册
     * @param uuid 玩家UUID
     * @return 是否已注册
     */
    boolean isRegistered(UUID uuid);
    
    /**
     * 根据用户名检查玩家是否已注册
     * @param username 玩家用户名
     * @return 是否已注册
     */
    boolean isRegistered(String username);
    
    /**
     * 注册玩家
     * @param player 玩家对象
     * @param password 密码
     */
    void registerPlayer(Player player, String password);
    
    /**
     * 验证玩家密码
     * @param uuid 玩家UUID
     * @param password 密码
     * @return 密码是否正确
     */
    boolean checkPassword(UUID uuid, String password);
    
    /**
     * 更新玩家最后登录信息
     * @param player 玩家对象
     */
    void updateLastLogin(Player player);
} 