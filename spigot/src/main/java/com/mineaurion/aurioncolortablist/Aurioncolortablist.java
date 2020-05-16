package com.mineaurion.aurioncolortablist;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryOptions;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import javax.swing.text.html.Option;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class Aurioncolortablist extends JavaPlugin implements Listener {

    private LuckPerms api;
    private Scoreboard board;

    @Override
    public void onEnable() {
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();
        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        board = Bukkit.getScoreboardManager().getMainScoreboard();
        if(provider != null){
            api = provider.getProvider();
            getServer().getPluginManager().registerEvents(this, this);
        }
        else{
            Bukkit.getPluginManager().disablePlugin(this);
        }
        this.getCommand("aurioncolortablist").setExecutor(new ReloadCommand(this));
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        updateScoreBoard(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        leavingScoreBoard(event.getPlayer());
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
        leavingScoreBoard(event.getPlayer());
    }

    public void leavingScoreBoard(Player player) {
        Optional<String> metaColor = getMetaNameColor(player.getUniqueId());
        if(metaColor.isPresent()) {
            Team team = board.getTeam(metaColor.get());
            if(team != null){
                team.removePlayer(player);
                if(team.getSize() < 1) {
                    System.out.println("Unregister team " + team.getSize());
                    team.unregister();
                }
            }
        }
    }

    public void updateScoreBoard(Player player) {
        Optional<String> metaColor = getMetaNameColor(player.getUniqueId());
        Team team = null;
        if(metaColor.isPresent()) {
            if(board.getTeam(metaColor.get()) == null) {
                team = board.registerNewTeam(metaColor.get());
                team.setPrefix(ChatColor.translateAlternateColorCodes('&',"&" + metaColor.get()));
            } else {
                team = board.getTeam(metaColor.get());
            }
        } else {
            team = board.registerNewTeam("default");
        }
        System.out.println("Ajout Tablist");
        team.addPlayer(Bukkit.getOfflinePlayer(player.getUniqueId()));
        player.setScoreboard(board);
    }

    private Optional<CachedMetaData> getMetaData(UUID uuid){
        Optional<CachedMetaData> cachedMetaData = Optional.empty();
        if(this.api != null){
            User user = this.api.getUserManager().getUser(uuid);
            if(user != null){
                Optional<QueryOptions> context = api.getContextManager().getQueryOptions(user);
                if(context.isPresent()){
                    cachedMetaData = Optional.of(user.getCachedData().getMetaData(context.get()));
                }
            }
        }
        return cachedMetaData;
    }

    public Optional<String> getMetaNameColor(UUID uuid) {
        Optional<CachedMetaData> cachedMetaData = getMetaData(uuid);
        return Optional.ofNullable(cachedMetaData.get().getMetaValue(getConfig().getString("meta")));
    }
}
