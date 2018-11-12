package com.mineaurion.aurionscolortablist;

import com.google.inject.Inject;
import me.lucko.luckperms.LuckPerms;
import me.lucko.luckperms.api.Contexts;
import me.lucko.luckperms.api.LuckPermsApi;
import me.lucko.luckperms.api.User;
import me.lucko.luckperms.api.caching.MetaData;
import me.lucko.luckperms.api.caching.UserData;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.tab.TabList;
import org.spongepowered.api.entity.living.player.tab.TabListEntry;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppedServerEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import javax.swing.text.html.Option;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Plugin(
        id = "aurionscolortablist",
        name = "Aurionscolortablist",
        description = "Color Tablist based on luckperms",
        url = "https://mineaurion.com",
        authors = {
                "Yann151924"
        },
        dependencies = {
            @Dependency(id= "luckperms")
        }
)
public class Aurionscolortablist {

    @Inject
    private Logger logger;

    @Inject
    Game game;

    @Inject @DefaultConfig(sharedRoot = true)
    Path path;
    @Inject @DefaultConfig(sharedRoot = true)
    ConfigurationLoader<CommentedConfigurationNode> loader;

    Config config;


    public Optional<LuckPermsApi> luckPermsApi;

    @Listener
    public void Init(GamePreInitializationEvent event) throws IOException, ObjectMappingException {
        luckPermsApi = Sponge.getPluginManager().getPlugin("luckperms").isPresent() ? LuckPerms.getApiSafe() : Optional.empty();
        if(!Files.exists(path)){
            game.getAssetManager().getAsset(this, "config.conf").get().copyToFile(path);
        }
        config = loader.load().getValue(Config.type);
        if(luckPermsApi.isPresent()){
            Task.builder()
                    .name("update-color-name")
                    .interval(1, TimeUnit.SECONDS)
                    .execute(() -> {
                        for (Player player : Sponge.getServer().getOnlinePlayers()) {
                            updateName(player);
                        }
                    }).submit(this);
        }
        else{
            disablePlugin();
        }
    }

    @Listener
    public void onServerReload(GameReloadEvent event) throws IOException, ObjectMappingException{
        config = loader.load().getValue(Config.type);
    }

    @Listener
    public void Stop(GameStoppedServerEvent event){
        disablePlugin();
    }

    private void updateName(Player player){
        TabList tabList = player.getTabList();
        Optional<TabListEntry> tabListEntry = tabList.getEntry(player.getUniqueId());
        StringBuilder newName = new StringBuilder();
        if(tabListEntry.isPresent()){
            TabListEntry entry = tabListEntry.get();
            if(config.prefix){
                newName.append(getPrefixLuckPerms(player).orElse(""));
            }
            newName.append("&")
                    .append(getMetanamecolor(player).orElse(""))
                    .append(player.getName());

            entry.setDisplayName(TextSerializers.FORMATTING_CODE.deserialize(newName.toString()));
        }
    }

    private Optional<String> getMetanamecolor(Player player){
        Optional<String> namecolor = Optional.empty();
        if(luckPermsApi.isPresent()){
            LuckPermsApi api = luckPermsApi.get();
            Optional<User> user = api.getUserSafe(player.getUniqueId());
            if(user.isPresent()){
                Contexts contexts = api.getContextsForPlayer(player);
                UserData userData = user.get().getCachedData();
                MetaData metaData = userData.getMetaData(contexts);

                Map<String ,String> metas = metaData.getMeta();
                namecolor = Optional.of(metas.get(config.meta));
            }
        }
        return namecolor;
    }

    private Optional<String> getPrefixLuckPerms(Player player){
        Optional<String> prefix = Optional.empty();
        if(luckPermsApi.isPresent()){
            LuckPermsApi api = luckPermsApi.get();
            Optional<User> user = api.getUserSafe(player.getUniqueId());
            if(user.isPresent()){
                Contexts contexts = api.getContextsForPlayer(player);
                UserData userData = user.get().getCachedData();
                MetaData metaData = userData.getMetaData(contexts);
                prefix = Optional.of(metaData.getPrefix());
            }
        }
        return prefix;
    }

    private void disablePlugin(){
        Sponge.getEventManager().unregisterListeners(this);
        Sponge.getCommandManager().getOwnedBy(this).forEach(Sponge.getCommandManager()::removeMapping);
        Sponge.getScheduler().getScheduledTasks(this).forEach(Task::cancel);
    }
}
