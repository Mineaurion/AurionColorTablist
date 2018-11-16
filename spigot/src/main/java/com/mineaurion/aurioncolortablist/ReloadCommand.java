package com.mineaurion.aurioncolortablist;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ReloadCommand implements CommandExecutor {

    private Aurioncolortablist plugin;

    public ReloadCommand(Aurioncolortablist plugin){
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        if(cmd.getName().equalsIgnoreCase("aurioncolortablist")){
            if(args.length >= 1 && args[0].equalsIgnoreCase("reload")){
                if(sender.hasPermission("aurioncolortablist.admin")){
                    plugin.reloadConfig();
                    sender.sendMessage("Aurioncolortablist is reloaded");
                    return true;
                }
                else{
                    sender.sendMessage(ChatColor.RED + "You don't have permission");
                    return false;
                }
            }
        }
        return false;
    }
}
