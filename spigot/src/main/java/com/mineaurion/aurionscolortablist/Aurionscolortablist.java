package com.mineaurion.aurionscolortablist;

import me.lucko.luckperms.api.Contexts;
import me.lucko.luckperms.api.LuckPermsApi;
import me.lucko.luckperms.api.User;
import me.lucko.luckperms.api.caching.MetaData;
import me.lucko.luckperms.api.caching.UserData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.Optional;

public final class Aurionscolortablist extends JavaPlugin implements Listener {

    private LuckPermsApi api;

    @Override
    public void onEnable() {
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();
        RegisteredServiceProvider<LuckPermsApi> provider = Bukkit.getServicesManager().getRegistration(LuckPermsApi.class);
        if(provider != null){
            api = provider.getProvider();
            getServer().getPluginManager().registerEvents(this, this);
            getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
                public void run() {
                    for (Player player : Aurionscolortablist.this.getServer().getOnlinePlayers()) {
                        Aurionscolortablist.this.updateName(player);
                    }
                }
            }, 0L, 200L);
        }
        else{
            Bukkit.getPluginManager().disablePlugin(this);
        }
        this.getCommand("aurionscolortablist").setExecutor(new ReloadCommand(this));
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        updateName(event.getPlayer());
    }

    private void updateName(Player player){
        StringBuilder newName = new StringBuilder();
        if(getConfig().getBoolean("prefix")){
            newName.append(getPrefixLuckPerms(player).orElse(""));
        }
        newName.append("&")
                .append(getMetanamecolor(player).orElse(""))
                .append(player.getDisplayName());

        player.setPlayerListName(ChatColor.translateAlternateColorCodes('&',newName.toString()));
    }

    private Optional<String> getMetanamecolor(Player player){
        Optional<String> namecolor = Optional.empty();
        Optional<User> user = api.getUserSafe(player.getUniqueId());
        if(user.isPresent()){
            Contexts contexts = api.getContextsForPlayer(player);
            UserData userData = user.get().getCachedData();
            MetaData metaData = userData.getMetaData(contexts);
            Map<String, String> metas = metaData.getMeta();
            namecolor = Optional.of(metas.get(getConfig().getString("meta")));
        }
        return namecolor;
    }

    private Optional<String> getPrefixLuckPerms(Player player){
        Optional<String> prefix = Optional.empty();
        Optional<User> user = api.getUserSafe(player.getUniqueId());
        if(user.isPresent()){
            Contexts contexts = api.getContextsForPlayer(player);
            UserData userData = user.get().getCachedData();
            MetaData metaData = userData.getMetaData(contexts);
            prefix = Optional.of(metaData.getPrefix());
        }
        return prefix;
    }
}
