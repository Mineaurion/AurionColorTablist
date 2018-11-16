package com.mineaurion.aurionscolortablist;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ReloadCommand implements CommandExecutor {

    private Aurionscolortablist plugin;

    public ReloadCommand(Aurionscolortablist plugin){
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        if(cmd.getName().equalsIgnoreCase("aurionscolortablist")){
            if(args.length >= 1 && args[0].equalsIgnoreCase("reload")){
                if(sender.hasPermission("aurionscolortablist.admin")){
                    plugin.reloadConfig();
                    sender.sendMessage("Aurionscolortablist is reloaded");
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
