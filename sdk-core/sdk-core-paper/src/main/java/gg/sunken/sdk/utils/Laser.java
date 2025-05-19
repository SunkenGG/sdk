package gg.sunken.sdk.utils;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;


public class Laser{
    private final UUID uniqueId;
    private Location from;
    private Location to;
    private LaserType type;
    private final Set<UUID> viewers;
    private final int crystalId;
    private final int[] guardianIds;
    public Laser(Location from, Location to, LaserType type) {
        this.uniqueId = UUID.randomUUID();
        this.from = from;
        this.to = to;
        this.type = type;
        this.viewers = new HashSet<>();
        this.crystalId = (int) (Math.random() * Integer.MAX_VALUE);
        this.guardianIds = new int[]{(int) (Math.random() * Integer.MAX_VALUE),(int) (Math.random() * Integer.MAX_VALUE)};
    }

    public void show(Player viewer){
        viewers.add(viewer.getUniqueId());
        if(type == LaserType.CRYSTAL){
            spawnCrystalLaser(viewer);
        }else if(type == LaserType.GUARDIAN){
            spawnGuardianLaser(viewer);
        }
    }

    public void hide(Player viewer){
        if(!viewers.contains(viewer.getUniqueId())) return;
        WrapperPlayServerDestroyEntities packet;
        if(type == LaserType.CRYSTAL){
            packet = new WrapperPlayServerDestroyEntities(crystalId);
        }else{
            packet = new WrapperPlayServerDestroyEntities(guardianIds[0],guardianIds[1]);
        }
        PacketEvents.getAPI().getPlayerManager().sendPacket(viewer, packet);
        viewers.remove(viewer.getUniqueId());
    }

    public void update(){
        for(UUID uuid : viewers){
            Player viewer = Bukkit.getPlayer(uuid);
            if(viewer != null && viewer.isOnline()){
                hide(viewer);
                show(viewer);
            }
        }
    }

    private void spawnCrystalLaser(Player viewer){
        int crystalId = this.crystalId;
        com.github.retrooper.packetevents.protocol.world.Location fromLocation = new com.github.retrooper.packetevents.protocol.world.Location(from.getX(), from.getY(), from.getZ(), from.getYaw(), from.getPitch());
        WrapperPlayServerSpawnEntity fakeCrystal = new WrapperPlayServerSpawnEntity(
                crystalId,
                UUID.randomUUID(),
                EntityTypes.END_CRYSTAL,
                fromLocation,
                0,
                0,
                new Vector3d(0,0,0)
        );

        List<EntityData> crystalData = new ArrayList<>();
        Optional<Vector3i> optional = Optional.of(new Vector3i(to.getBlockX(), to.getBlockY(), to.getBlockZ()));
        crystalData.add(new EntityData(8, EntityDataTypes.OPTIONAL_BLOCK_POSITION, optional));
        crystalData.add(new EntityData(9, EntityDataTypes.BOOLEAN, false));
        crystalData.add(new EntityData(0, EntityDataTypes.BYTE, (byte) 0x20));
        WrapperPlayServerEntityMetadata metadataCrystal = new WrapperPlayServerEntityMetadata(crystalId,crystalData);

        PacketEvents.getAPI().getPlayerManager().sendPacket(viewer, fakeCrystal);
        PacketEvents.getAPI().getPlayerManager().sendPacket(viewer, metadataCrystal);
    }

    private void spawnGuardianLaser(Player viewer){
        int guardianId = guardianIds[0];
        int targetId = guardianIds[1];
        com.github.retrooper.packetevents.protocol.world.Location fromLocation = new com.github.retrooper.packetevents.protocol.world.Location(from.getX(), from.getY(), from.getZ(), from.getYaw(), from.getPitch());
        com.github.retrooper.packetevents.protocol.world.Location toLocation = new com.github.retrooper.packetevents.protocol.world.Location(to.getX(), to.getY(), to.getZ(), to.getYaw(), to.getPitch());
        WrapperPlayServerSpawnEntity fakeGuardian = new WrapperPlayServerSpawnEntity(
                guardianId,
                UUID.randomUUID(),
                EntityTypes.GUARDIAN,
                fromLocation,
                0,
                0,
                new Vector3d(0,0,0)
        );

        WrapperPlayServerSpawnEntity fakeArmorStand = new WrapperPlayServerSpawnEntity(
                targetId,
                UUID.randomUUID(),
                EntityTypes.ARMOR_STAND,
                toLocation,
                0,
                0,
                new Vector3d(0,0,0)
        );

        List<EntityData> targetData = new ArrayList<>();
        targetData.add(new EntityData(0, EntityDataTypes.BYTE, (byte) 0x20));
        targetData.add(new EntityData(5, EntityDataTypes.BOOLEAN, true));
        targetData.add(new EntityData(15, EntityDataTypes.BYTE, (byte)0x01));
        targetData.add(new EntityData(15, EntityDataTypes.BYTE, (byte)0x10));
        WrapperPlayServerEntityMetadata metadataArmorStand = new WrapperPlayServerEntityMetadata(targetId,targetData);

        List<EntityData> guardianData = new ArrayList<>();
        guardianData.add(new EntityData(0, EntityDataTypes.BYTE, (byte) 0x20));
        guardianData.add(new EntityData(16, EntityDataTypes.BOOLEAN, true));
        guardianData.add(new EntityData(17, EntityDataTypes.INT, targetId));
        WrapperPlayServerEntityMetadata metadataGuardian = new WrapperPlayServerEntityMetadata(guardianId,guardianData);

        PacketEvents.getAPI().getPlayerManager().sendPacket(viewer, fakeGuardian);
        PacketEvents.getAPI().getPlayerManager().sendPacket(viewer, fakeArmorStand);
        PacketEvents.getAPI().getPlayerManager().sendPacket(viewer, metadataArmorStand);
        PacketEvents.getAPI().getPlayerManager().sendPacket(viewer, metadataGuardian);

    }

    public Location getFrom() {
        return from;
    }

    public void setFrom(Location from) {
        this.from = from;
    }

    public Location getTo() {
        return to;
    }

    public void setTo(Location to) {
        this.to = to;
    }

    public LaserType getType() {
        return type;
    }

    public void setType(LaserType type) {
        this.type = type;
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    public Set<UUID> getViewers() {
        return viewers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Laser laser = (Laser) o;
        return uniqueId.equals(laser.uniqueId);
    }

    @Override
    public int hashCode() {
        return uniqueId.hashCode();
    }

    public enum LaserType{
        CRYSTAL,
        GUARDIAN
    }
}
