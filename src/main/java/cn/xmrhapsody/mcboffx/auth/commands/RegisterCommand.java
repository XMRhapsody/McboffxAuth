package cn.xmrhapsody.mcboffx.auth.commands;

import cn.xmrhapsody.mcboffx.auth.McboffxAuth;
import cn.xmrhapsody.mcboffx.auth.utils.AuthManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RegisterCommand implements CommandExecutor {

    private final McboffxAuth plugin;

    public RegisterCommand(McboffxAuth plugin) {
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

        if (plugin.getStorageProvider().isRegistered(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "[Mcboffx] 你已经注册过了！请使用 /login <密码> 或 /l <密码> 进行登录。");
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "[Mcboffx] 用法: /register <密码> <确认密码> 或 /reg <密码> <确认密码>");
            return true;
        }

        String password = args[0];
        String confirmPassword = args[1];

        // 检查密码长度
        if (password.length() < 6) {
            player.sendMessage(ChatColor.RED + "[Mcboffx] 密码长度必须至少为6个字符！");
            return true;
        }

        // 检查两次密码是否一致
        if (!password.equals(confirmPassword)) {
            player.sendMessage(ChatColor.RED + "[Mcboffx] 两次输入的密码不一致！");
            return true;
        }

        // 注册玩家
        plugin.getStorageProvider().registerPlayer(player, password);
        AuthManager.authenticate(player.getUniqueId());
        player.sendMessage(ChatColor.GREEN + "[Mcboffx] 注册成功！你已自动登录，现在可以自由移动了。");

        return true;
    }
} 