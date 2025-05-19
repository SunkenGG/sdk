package gg.sunken.sdk.utils;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class BossBar {
    private final UUID id;
    private Component title;
    private float progress;
    private net.kyori.adventure.bossbar.BossBar.Color color;
    private net.kyori.adventure.bossbar.BossBar.Overlay style;
    private final Set<UUID> viewers;
    public BossBar(Component title, float progress, net.kyori.adventure.bossbar.BossBar.Color color, net.kyori.adventure.bossbar.BossBar.Overlay style) {
        this.id = UUID.randomUUID();
        this.title = title;
        this.progress = progress;
        this.color = color;
        this.style = style;
        this.viewers = new HashSet<>();
    }

    public void show(Player player) {
        viewers.add(player.getUniqueId());
        WrapperPlayServerBossBar packet = new WrapperPlayServerBossBar(
                id,
                WrapperPlayServerBossBar.Action.ADD);
        packet.setColor(color);
        packet.setOverlay(style);
        packet.setHealth(progress/100);
        packet.setTitle(title);
        packet.setFlags(EnumSet.noneOf(net.kyori.adventure.bossbar.BossBar.Flag.class));
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
    }

    public void hide(Player player) {
        if (!viewers.contains(player.getUniqueId())) return;

        WrapperPlayServerBossBar removeBar = new WrapperPlayServerBossBar(
                id,
                WrapperPlayServerBossBar.Action.REMOVE
        );

        PacketEvents.getAPI().getPlayerManager().sendPacket(player, removeBar);
        viewers.remove(player.getUniqueId());
    }

    public void update() {
        for (UUID uuid : viewers) {
            Player viewer = Bukkit.getPlayer(uuid);
            if (viewer != null && viewer.isOnline()) {
                WrapperPlayServerBossBar updateTitle = new WrapperPlayServerBossBar(id, WrapperPlayServerBossBar.Action.UPDATE_TITLE);
                updateTitle.setTitle(title);

                WrapperPlayServerBossBar updateHealth = new WrapperPlayServerBossBar(id, WrapperPlayServerBossBar.Action.UPDATE_HEALTH);
                updateHealth.setHealth(progress / 100);

                WrapperPlayServerBossBar updateStyle = new WrapperPlayServerBossBar(id, WrapperPlayServerBossBar.Action.UPDATE_STYLE);
                updateStyle.setColor(color);
                updateStyle.setOverlay(style);

                PacketEvents.getAPI().getPlayerManager().sendPacket(viewer, updateTitle);
                PacketEvents.getAPI().getPlayerManager().sendPacket(viewer, updateHealth);
                PacketEvents.getAPI().getPlayerManager().sendPacket(viewer, updateStyle);
            }
        }
    }

    public void setTitle(Component title) {
        this.title = title;
    }

    public void setPercentage(float percentage) {
        this.progress = percentage;
    }

    public void setColor(net.kyori.adventure.bossbar.BossBar.Color color) {
        this.color = color;
    }

    public void setStyle(net.kyori.adventure.bossbar.BossBar.Overlay style) {
        this.style = style;
    }

    public UUID getId() {
        return id;
    }

    public Set<UUID> getViewers() {
        return viewers;
    }
}
