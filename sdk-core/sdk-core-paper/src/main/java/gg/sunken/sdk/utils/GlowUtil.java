package gg.sunken.sdk.utils;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.*;

public class GlowUtil {

    private static final Map<Integer, ChatColor> glowingEntities = new HashMap<>();

    public static void glow(Entity entity, ChatColor color) {
        glowingEntities.put(entity.getEntityId(), color);
    }

    public static void glow(int entityId, ChatColor color) {
        glowingEntities.put(entityId, color);
        for (Player receiver : Bukkit.getOnlinePlayers()) {
            sendGlowMetadata(entityId, true, receiver);
            sendTeamPacket(entityId, color, WrapperPlayServerTeams.TeamMode.CREATE, receiver);
        }
    }

    public static void unglow(Entity entity) {
        unglow(entity.getEntityId());
    }

    public static void unglow(int entityId) {
        glowingEntities.remove(entityId);
        for (Player receiver : Bukkit.getOnlinePlayers()) {
            sendGlowMetadata(entityId, false, receiver);
            sendTeamPacket(entityId, null, WrapperPlayServerTeams.TeamMode.REMOVE, receiver);
        }
    }

    public static void resendAllGlows(Player viewer) {
        for (Map.Entry<Integer, ChatColor> entry : glowingEntities.entrySet()) {
            int entityId = entry.getKey();
            ChatColor color = entry.getValue();
            sendGlowMetadata(entityId, true, viewer);
            sendTeamPacket(entityId, color, WrapperPlayServerTeams.TeamMode.CREATE, viewer);
        }
    }

    private static Entity getEntityById(int id) {
        for (World world : Bukkit.getWorlds()) {
            for (Entity e : world.getEntities()) {
                if (e.getEntityId() == id) return e;
            }
        }
        return null;
    }

    private static String getEntityName(Entity entity) {
        if (entity instanceof Player) return entity.getName();
        return entity.getUniqueId().toString(); // For mobs, use UUID to avoid name issues
    }

    private static void sendGlowMetadata(int entityId, boolean glowing, Player receiver) {
        List<EntityData> data = new ArrayList<>();
        data.add(new EntityData(0, EntityDataTypes.BYTE, (byte) (glowing ? 0x40 : 0)));
        WrapperPlayServerEntityMetadata packet = new WrapperPlayServerEntityMetadata(entityId,data);
        PacketEvents.getAPI().getPlayerManager().sendPacket(receiver, packet);
    }

    private static void sendTeamPacket(int entityId, ChatColor color, WrapperPlayServerTeams.TeamMode mode, Player receiver) {
        Entity entity = getEntityById(entityId);
        String teamName = getTeamName(entityId);
        WrapperPlayServerTeams.ScoreBoardTeamInfo info = new WrapperPlayServerTeams.ScoreBoardTeamInfo(Component.text(""),
                Component.text(""),
                Component.text(""),
                WrapperPlayServerTeams.NameTagVisibility.ALWAYS,
                WrapperPlayServerTeams.CollisionRule.NEVER,
                get(color),
                WrapperPlayServerTeams.OptionData.NONE);
        WrapperPlayServerTeams packet = new WrapperPlayServerTeams(teamName,mode,info,Collections.singletonList(getEntityName(entity)));
        PacketEvents.getAPI().getPlayerManager().sendPacket(receiver, packet);
    }

    private static String getTeamName(int entityId) {
        return "glow_" + entityId;
    }

    private static NamedTextColor get(ChatColor color) {
        if (color == null) return NamedTextColor.WHITE;
        return switch (color) {
            case BLACK -> NamedTextColor.BLACK;
            case DARK_BLUE -> NamedTextColor.DARK_BLUE;
            case DARK_GREEN -> NamedTextColor.DARK_GREEN;
            case DARK_AQUA -> NamedTextColor.DARK_AQUA;
            case DARK_RED -> NamedTextColor.DARK_RED;
            case DARK_PURPLE -> NamedTextColor.DARK_PURPLE;
            case GOLD -> NamedTextColor.GOLD;
            case GRAY -> NamedTextColor.GRAY;
            case DARK_GRAY -> NamedTextColor.DARK_GRAY;
            case BLUE -> NamedTextColor.BLUE;
            case GREEN -> NamedTextColor.GREEN;
            case AQUA -> NamedTextColor.AQUA;
            case RED -> NamedTextColor.RED;
            case LIGHT_PURPLE -> NamedTextColor.LIGHT_PURPLE;
            case YELLOW -> NamedTextColor.YELLOW;
            case WHITE -> NamedTextColor.WHITE;
            default -> NamedTextColor.WHITE; // Fallback
        };
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        resendAllGlows(event.getPlayer());
    }
}
