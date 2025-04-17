package cn.xmrhapsody.mcboffx.auth.listeners;

import cn.xmrhapsody.mcboffx.auth.McboffxAuth;
import cn.xmrhapsody.mcboffx.auth.utils.AuthManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.GameMode;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerListener implements Listener {

    private final McboffxAuth plugin;
    private final Map<UUID, BukkitTask> timeoutTasks = new HashMap<>();

    public PlayerListener(McboffxAuth plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        
        // 检查玩家是否有权限使用此插件
        if (!player.hasPermission("mcboffx.admin")) {
            player.kickPlayer(ChatColor.RED + "[Mcboffx] 你没有权限加入此服务器");
            return;
        }
        
        // 从配置文件获取登录位置和视角设置
        FileConfiguration config = plugin.getConfig();
        boolean useFixedLocation = config.getBoolean("login.use-fixed-location", true);
        
        if (useFixedLocation) {
            String worldName = config.getString("login.location.world", player.getWorld().getName());
            World world = plugin.getServer().getWorld(worldName);
            if (world == null) world = player.getWorld();
            
            // 读取坐标和视角
            double x = config.getDouble("login.location.x", 0.0);
            double y = config.getDouble("login.location.y", 70.0);
            double z = config.getDouble("login.location.z", 0.0);
            float yaw = (float) config.getDouble("login.location.yaw", 0.0);
            float pitch = (float) config.getDouble("login.location.pitch", 0.0);
            
            // 传送玩家到指定位置
            Location loginLocation = new Location(world, x, y, z, yaw, pitch);
            player.teleport(loginLocation);
            
            // 设置玩家视角
            String viewMode = config.getString("login.view.mode", "FIRST_PERSON");
            if (viewMode.equalsIgnoreCase("THIRD_PERSON")) {
                // 在Bukkit API中没有直接设置视角的方法，这里使用数据包或其他方式可以实现
                // 由于实现复杂，这里只注释说明可以通过发送数据包实现
            }
        }
        
        // 设置登录超时
        int loginTimeout = plugin.getConfig().getInt("settings.login-timeout", 60);
        
        // 如果玩家未注册，提示注册
        if (!plugin.getStorageProvider().isRegistered(uuid)) {
            player.sendMessage(ChatColor.YELLOW + "[Mcboffx] 请使用 /register <密码> <确认密码> 或 /reg <密码> <确认密码> 进行注册！");
        } else {
            player.sendMessage(ChatColor.YELLOW + "[Mcboffx] 请使用 /login <密码> 或 /l <密码> 进行登录！");
        }
        
        // 设置登录超时
        BukkitTask task = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (!AuthManager.isAuthenticated(uuid) && player.isOnline()) {
                player.kickPlayer(ChatColor.RED + "[Mcboffx] 登录超时，请重新连接！");
                timeoutTasks.remove(uuid);
            }
        }, 20 * loginTimeout); // 使用配置的超时时间
        
        timeoutTasks.put(uuid, task);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        
        // 清除认证状态
        AuthManager.deauthenticate(uuid);
        
        // 取消超时任务
        if (timeoutTasks.containsKey(uuid)) {
            timeoutTasks.get(uuid).cancel();
            timeoutTasks.remove(uuid);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        
        if (!AuthManager.isAuthenticated(player.getUniqueId())) {
            Location from = event.getFrom();
            Location to = event.getTo();
            
            // 允许玩家旋转视角，但不允许移动位置
            if (from.getBlockX() != to.getBlockX() || from.getBlockY() != to.getBlockY() || from.getBlockZ() != to.getBlockZ()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (!AuthManager.isAuthenticated(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "[Mcboffx] 你必须先登录才能聊天！");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String command = event.getMessage().split(" ")[0].toLowerCase();
        
        if (!AuthManager.isAuthenticated(player.getUniqueId())) {
            // 允许使用登录和注册命令（包括简写命令）
            if (!command.equals("/login") && !command.equals("/register") && 
                !command.equals("/l") && !command.equals("/reg")) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "[Mcboffx] 你必须先登录才能使用命令！");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!AuthManager.isAuthenticated(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!AuthManager.isAuthenticated(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (event.getPlayer() instanceof Player && !AuthManager.isAuthenticated(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!AuthManager.isAuthenticated(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player && !AuthManager.isAuthenticated(((Player) event.getEntity()).getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityTarget(EntityTargetEvent event) {
        if (event.getTarget() instanceof Player && !AuthManager.isAuthenticated(((Player) event.getTarget()).getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (!AuthManager.isAuthenticated(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        if (!AuthManager.isAuthenticated(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }
} 