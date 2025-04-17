package cn.xmrhapsody.mcboffx.auth.listeners;

import cn.xmrhapsody.mcboffx.auth.McboffxAuth;
import cn.xmrhapsody.mcboffx.auth.utils.AuthManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
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
        
        // 将玩家传送到指定坐标
        Location spawnLocation = new Location(player.getWorld(), 0, 100, 0);
        player.teleport(spawnLocation);
        
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
        }, 20 * 60); // 60秒超时
        
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