package cn.xmrhapsody.mcboffx.auth.commands;

import cn.xmrhapsody.mcboffx.auth.McboffxAuth;
import cn.xmrhapsody.mcboffx.auth.utils.AuthManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LoginCommand implements CommandExecutor {

    private final McboffxAuth plugin;

    public LoginCommand(McboffxAuth plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "[Mcboffx] 只有玩家可以使用此命令！");
            return true;
        }

        Player player = (Player) sender;

        if (AuthManager.isAuthenticated(player.getUniqueId())) {
            player.sendMessage(ChatColor.GREEN + "[Mcboffx] 你已经登录了！");
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "[Mcboffx] 用法: /login <密码> 或 /l <密码>");
            return true;
        }

        String password = args[0];

        // 检查玩家是否已注册
        if (!plugin.getStorageProvider().isRegistered(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "[Mcboffx] 你还没有注册！请使用 /register <密码> <确认密码> 或 /reg <密码> <确认密码> 进行注册。");
            return true;
        }

        // 验证密码
        if (plugin.getStorageProvider().checkPassword(player.getUniqueId(), password)) {
            AuthManager.authenticate(player.getUniqueId());
            plugin.getStorageProvider().updateLastLogin(player);
            player.sendMessage(ChatColor.GREEN + "[Mcboffx] 登录成功！现在你可以自由移动了。");
        } else {
            player.sendMessage(ChatColor.RED + "[Mcboffx] 密码错误，请重试！");
        }

        return true;
    }
} 