package gg.sunken.sdk.utils;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class ServerUtils {

    @Getter
    private static final String serverJar = Bukkit.getVersionMessage();

    private ServerUtils() {
        throw new UnsupportedOperationException("Cannot instantiate utility class.");
    }

    /**
     * Get the plugin that called this method. This is caller sensitive & cannot be cached.
     *
     * @return The plugin that called this method.
     */
    public static JavaPlugin getCallingPlugin() {
        Exception ex = new Exception();
        try {
            Class<?> clazz = Class.forName(ex.getStackTrace()[2].getClassName());
            return JavaPlugin.getProvidingPlugin(clazz);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Read the contents of a log file.
     *
     * @param log The log file to read.
     * @return The contents of the log file, or null if an error occurred.
     */
    @Nullable
    public static String readLog(File log) {
        byte[] bytes;
        try {
            bytes = Files.readAllBytes(log.toPath());
        } catch (IOException e) {
            return null;
        }

        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static void damagePlayer(Player p, double damage, EntityDamageEvent.DamageCause cause) {
        double points = p.getAttribute(Attribute.ARMOR).getValue();
        double toughness = p.getAttribute(Attribute.ARMOR_TOUGHNESS).getValue();
        PotionEffect effect = p.getPotionEffect(PotionEffectType.RESISTANCE);
        int resistance = effect == null ? 0 : effect.getAmplifier();
        int epf = getEPF(p.getInventory(), cause);
        
        p.damage(calculateDamageApplied(damage, points, toughness, resistance, epf));
    }

    /**
     * https://www.spigotmc.org/threads/tutorial-calculating-damage-taken-by-a-player-manually.424680/
     * @param damage The damage to apply
     * @param points The player's armor points
     * @param toughness The player's armor toughness
     * @param resistance The player's resistance level
     * @param epf The player's enchantment protection factor
     * @return
     */
    public static double calculateDamageApplied(double damage, double points, double toughness, int resistance, int epf) {
        double withArmorAndToughness = damage * (1 - Math.min(20, Math.max(points / 5, points - damage / (2 + toughness / 4))) / 25);
        double withResistance = withArmorAndToughness * (1 - (resistance * 0.2));
        return withResistance * (1 - (Math.min(20.0, epf) / 25));
    }

    private static int getEPF(PlayerInventory inv, EntityDamageEvent.DamageCause cause) {
        ItemStack helm = inv.getHelmet();
        ItemStack chest = inv.getChestplate();
        ItemStack legs = inv.getLeggings();
        ItemStack boot = inv.getBoots();

        return (helm != null ? helm.getEnchantmentLevel(Enchantment.PROTECTION) : 0) +
                (chest != null ? chest.getEnchantmentLevel(Enchantment.PROTECTION) : 0) +
                (legs != null ? legs.getEnchantmentLevel(Enchantment.PROTECTION) : 0) +
                (boot != null ? boot.getEnchantmentLevel(Enchantment.PROTECTION) : 0);

        //TODO: Implement this
//        switch (cause) {
//            case FALL -> {
//                return (helm != null ? helm.getEnchantmentLevel(Enchantment.FEATHER_FALLING) : 0) +
//                        (chest != null ? chest.getEnchantmentLevel(Enchantment.FEATHER_FALLING) : 0) +
//                        (legs != null ? legs.getEnchantmentLevel(Enchantment.FEATHER_FALLING) : 0) +
//                        (boot != null ? boot.getEnchantmentLevel(Enchantment.FEATHER_FALLING) : 0);
//            }
//            case LAVA, FIRE, FIRE_TICK -> {
//                return (helm != null ? helm.getEnchantmentLevel(Enchantment.FIRE_PROTECTION) : 0) +
//                        (chest != null ? chest.getEnchantmentLevel(Enchantment.FIRE_PROTECTION) : 0) +
//                        (legs != null ? legs.getEnchantmentLevel(Enchantment.FIRE_PROTECTION) : 0) +
//                        (boot != null ? boot.getEnchantmentLevel(Enchantment.FIRE_PROTECTION) : 0);
//            }
//            case BLOCK_EXPLOSION, ENTITY_EXPLOSION -> {
//                return (helm != null ? helm.getEnchantmentLevel(Enchantment.BLAST_PROTECTION) : 0) +
//                        (chest != null ? chest.getEnchantmentLevel(Enchantment.BLAST_PROTECTION) : 0) +
//                        (legs != null ? legs.getEnchantmentLevel(Enchantment.BLAST_PROTECTION) : 0) +
//                        (boot != null ? boot.getEnchantmentLevel(Enchantment.BLAST_PROTECTION) : 0);
//            }
//            default -> {
//            }
//        }
    }
}
