package com.mineaurion.aurioncolortablist;

import com.google.inject.Inject;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryOptions;
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
import org.spongepowered.api.event.game.state.GameStoppedServerEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.service.ProviderRegistration;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Plugin(
        id = "aurioncolortablist",
        name = "Aurioncolortablist",
        description = "Color Tablist based on luckperms",
        url = "https://mineaurion.com",
        authors = {
                "Yann151924"
        },
        dependencies = {
            @Dependency(id= "luckperms")
        }
)
public class Aurioncolortablist {

    @Inject
    private Logger logger;

    @Inject
    Game game;

    @Inject @DefaultConfig(sharedRoot = true)
    Path path;
    @Inject @DefaultConfig(sharedRoot = true)
    ConfigurationLoader<CommentedConfigurationNode> loader;

    Config config;


    public Optional<LuckPerms> api;

    @Listener
    public void Init(GamePreInitializationEvent event) throws IOException, ObjectMappingException {
        Optional<ProviderRegistration<LuckPerms>> provider = Sponge.getServiceManager().getRegistration(LuckPerms.class);
        api = provider.map(ProviderRegistration::getProvider);
        if(!Files.exists(path)){
            game.getAssetManager().getAsset(this, "config.conf").get().copyToFile(path);
        }
        config = loader.load().getValue(Config.type);
        if(api.isPresent()){
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
        for (TabListEntry entry: tabList.getEntries()) {
            StringBuilder newName = new StringBuilder();
            Optional<Player> playerEntry = Sponge.getServer().getPlayer(entry.getProfile().getUniqueId());
            playerEntry.ifPresent( p -> {
                if(config.prefix){
                    newName.append(getPlayerPrefix(p.getUniqueId()).orElse(""));
                }
                newName.append("&")
                        .append(getMetaNameColor(p.getUniqueId()).orElse(""))
                        .append(p.getName());
            });
            entry.setDisplayName(TextSerializers.FORMATTING_CODE.deserialize(newName.toString()));

        }
    }

    private Optional<CachedMetaData> getMetaData(UUID uuid){
        Optional<CachedMetaData> cachedMetaData = Optional.empty();
        if(this.api.isPresent()){
            User user = this.api.get().getUserManager().getUser(uuid);
            if(user != null){
                Optional<QueryOptions> context = api.get().getContextManager().getQueryOptions(user);
                if(context.isPresent()){
                    cachedMetaData = Optional.of(user.getCachedData().getMetaData(context.get()));
                }
            }
        }
        return cachedMetaData;
    }

    public Optional<String> getPlayerPrefix(UUID uuid){
        Optional<CachedMetaData> cachedMetaData = getMetaData(uuid);
        return Optional.ofNullable(cachedMetaData.get().getPrefix());
    }

    public Optional<String> getMetaNameColor(UUID uuid) {
        Optional<CachedMetaData> cachedMetaData = getMetaData(uuid);
        return Optional.ofNullable(cachedMetaData.get().getMetaValue(config.meta));
    }


    private void disablePlugin(){
        Sponge.getEventManager().unregisterListeners(this);
        Sponge.getCommandManager().getOwnedBy(this).forEach(Sponge.getCommandManager()::removeMapping);
        Sponge.getScheduler().getScheduledTasks(this).forEach(Task::cancel);
    }
}
