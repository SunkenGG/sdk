package gg.sunken.platform.playground;

import gg.sunken.sdk.config.ConfigValue;
import gg.sunken.sdk.utils.ItemBuilder;
import com.google.auto.service.AutoService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

@AutoService(Listener.class)
public class JoinListener implements Listener {

    @ConfigValue("database")
    private DatabaseConfig database;

    @ConfigValue(value = "reward-item", file = "rewards.yml")
    private ItemBuilder rewardItem;

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.getPlayer().sendMessage("Database host: " + database.getHost());
        event.getPlayer().sendMessage("Database port: " + database.getPort());
        event.getPlayer().sendMessage("Database creds: " + database.getCredentials().toString());
    }
}
