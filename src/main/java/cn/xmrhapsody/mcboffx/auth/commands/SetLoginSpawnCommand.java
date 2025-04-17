package cn.xmrhapsody.mcboffx.auth.commands;

import cn.xmrhapsody.mcboffx.auth.McboffxAuth;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetLoginSpawnCommand implements CommandExecutor {

    private final McboffxAuth plugin;

    public SetLoginSpawnCommand(McboffxAuth plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "[Mcboffx] 只有玩家可以使用此命令！");
            return true;
        }

        Player player = (Player) sender;
        
        // 检查玩家是否有权限使用此命令
        if (!player.hasPermission("mcboffx.admin")) {
            player.sendMessage(ChatColor.RED + "[Mcboffx] 你没有权限使用此命令");
            return true;
        }

        // 获取玩家当前位置
        Location location = player.getLocation();
        
        // 更新配置
        plugin.getConfig().set("login.use-fixed-location", true);
        plugin.getConfig().set("login.location.world", location.getWorld().getName());
        plugin.getConfig().set("login.location.x", location.getX());
        plugin.getConfig().set("login.location.y", location.getY());
        plugin.getConfig().set("login.location.z", location.getZ());
        plugin.getConfig().set("login.location.yaw", location.getYaw());
        plugin.getConfig().set("login.location.pitch", location.getPitch());
        
        // 保存配置
        plugin.saveConfig();
        
        // 发送确认消息
        player.sendMessage(ChatColor.GREEN + "[Mcboffx] 成功设置登录位置为当前位置！");
        player.sendMessage(ChatColor.GREEN + "世界: " + location.getWorld().getName());
        player.sendMessage(ChatColor.GREEN + "坐标: X=" + String.format("%.2f", location.getX()) + 
                           ", Y=" + String.format("%.2f", location.getY()) + 
                           ", Z=" + String.format("%.2f", location.getZ()));
        
        return true;
    }
} 