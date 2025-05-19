package gg.sunken.sdk.utils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;
import java.util.UUID;

public class ItemStackUtils {
    private static final UUID RANDOM_UUID = UUID.fromString("92864445-51c5-4c3b-9039-517c9927d1b4"); // We reuse the same "random" UUID all the time

    /**
     * Checks if the inventory is full
     *
     * @param inventory The inventory to check
     * @return If the inventory is full
     */
    public static boolean isFull(Inventory inventory) {
        return inventory.firstEmpty() == -1;
    }

    /**
     * Checks if the inventory is full after adding the item
     *
     * @param inventory The inventory to check
     * @param toAdd     The item to add
     * @return If the inventory is full after adding the item
     */
    public static boolean isFull(Inventory inventory, ItemStack toAdd) {
        if (!isFull(inventory)) {
            return false;
        }

        int leftToAdd = toAdd.getAmount();
        for (ItemStack itemStack : inventory) {
            if (!itemStack.isSimilar(toAdd)) {
                continue;
            }
            if (leftToAdd - (itemStack.getMaxStackSize() - itemStack.getAmount()) <= 0) {
                return false;
            }
            leftToAdd -= itemStack.getMaxStackSize() - itemStack.getAmount();
        }
        return true;
    }

    /**
     * Returns the amount of free slots in the inventory
     *
     * @param inventory
     * @return the amount of free slots in the inventory
     */
    public static int freeSlots(Inventory inventory) {
        int freeSlots = 0;
        for (ItemStack itemStack : inventory) {
            if (itemStack == null) {
                freeSlots++;
            }
        }
        return freeSlots;
    }

    /**
     * Returns the amount of free slots in the inventory after adding the item
     *
     * @param inventory The inventory to check
     * @param toAdd     The item to add
     * @return the amount of free slots in the inventory after adding the item
     */
    public static int freeSlots(Inventory inventory, ItemStack toAdd) {
        int freeSlots = 0;
        for (ItemStack itemStack : inventory) {
            if (itemStack == null) {
                freeSlots += toAdd.getMaxStackSize();
            } else if (itemStack.equals(toAdd)) {
                freeSlots += itemStack.getMaxStackSize() - itemStack.getAmount();
            }
        }
        return freeSlots;
    }

    /**
     * Returns the amount of free slots in the inventory after adding the item
     *
     * @param url The mojang skin URL of the head
     * @return The head ItemStack
     */
    public static ItemStack getHeadUrl(String url) {
        PlayerProfile profile = getProfile(url);
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setOwnerProfile(profile); // Set the owning player of the head to the player profile
        head.setItemMeta(meta);
        return head;
    }

    /**
     * Applies the head to the ItemStack
     *
     * @param stack The ItemStack to apply the head to
     * @param url   The mojang skin URL of the head
     * @return The ItemStack with the head applied
     */
    public static ItemStack applyHeadUrl(ItemStack stack, String url) {
        PlayerProfile profile = getProfile(url);
        SkullMeta meta = (SkullMeta) stack.getItemMeta();
        meta.setOwnerProfile(profile); // Set the owning player of the head to the player profile
        stack.setItemMeta(meta);
        return stack;
    }

    /**
     * Applies the head to the SkullMeta
     *
     * @param meta The SkullMeta to apply the head to
     * @param url  The mojang skin URL of the head
     * @return The SkullMeta with the head applied
     */
    public static SkullMeta applyHeadUrl(SkullMeta meta, String url) {
        PlayerProfile profile = getProfile(url);
        meta.setOwnerProfile(profile); // Set the owning player of the head to the player profile
        return meta;
    }

    /**
     * Returns the head ItemStack from a base64 string
     *
     * @param base64 The base64 string of the mojang skin url
     * @return The head ItemStack
     * @throws MalformedURLException
     */
    public static ItemStack getHeadBase64(String base64) throws MalformedURLException {
        return getHeadUrl(getUrlFromBase64(base64).toString());
    }

    /**
     * Applies the head to the ItemStack from a base64 string
     *
     * @param stack  The ItemStack to apply the head to
     * @param base64 The base64 string of the mojang skin url
     * @return The ItemStack with the head applied
     * @throws MalformedURLException
     */
    public static ItemStack applyHeadBase64(ItemStack stack, String base64) throws MalformedURLException {
        return applyHeadUrl(stack, getUrlFromBase64(base64).toString());
    }

    /**
     * Applies the head to the SkullMeta from a base64 string
     *
     * @param meta   The SkullMeta to apply the head to
     * @param base64 The base64 string of the mojang skin url
     * @return The SkullMeta with the head applied
     * @throws MalformedURLException
     */
    public static SkullMeta applyHeadBase64(SkullMeta meta, String base64) throws MalformedURLException {
        return applyHeadUrl(meta, getUrlFromBase64(base64).toString());
    }

    /**
     * Returns the URL from a base64 string
     *
     * @param base64 The base64 string of the mojang skin url
     * @return The URL
     * @throws MalformedURLException
     */
    public static URL getUrlFromBase64(String base64) throws MalformedURLException {
        String decoded = new String(Base64.getDecoder().decode(base64));
        // We simply remove the "beginning" and "ending" part of the JSON, so we're left with only the URL. You could use a proper
        // JSON parser for this, but that's not worth it. The String will always start exactly with this stuff anyway
        return new URL(decoded.substring("{\"textures\":{\"SKIN\":{\"url\":\"".length(), decoded.length() - "\"}}}".length()));
    }

    private static PlayerProfile getProfile(String url) {
        PlayerProfile profile = Bukkit.createPlayerProfile(RANDOM_UUID); // Get a new player profile
        PlayerTextures textures = profile.getTextures();
        URL urlObject;
        try {
            urlObject = new URL(url); // The URL to the skin, for example: https://textures.minecraft.net/texture/18813764b2abc94ec3c3bc67b9147c21be850cdf996679703157f4555997ea63a
        } catch (MalformedURLException exception) {
            throw new RuntimeException("Invalid URL", exception);
        }
        textures.setSkin(urlObject); // Set the skin of the player profile to the URL
        profile.setTextures(textures); // Set the textures back to the profile
        return profile;
    }

}
