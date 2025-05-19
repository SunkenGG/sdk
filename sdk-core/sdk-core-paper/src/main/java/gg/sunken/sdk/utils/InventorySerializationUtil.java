package gg.sunken.sdk.utils;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Base64;

public class InventorySerializationUtil {

    private InventorySerializationUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * A method to serialize an inventory to Base64 string.
     *
     * @param playerInventory to turn into a Base64 string.
     * @return A string array with 3 elements: inventory content, offHand & armor
     * @throws IllegalStateException
     */
    public static String[] playerInventoryToBase64(PlayerInventory playerInventory) throws IllegalStateException {
        //get the main content part, this doesn't return the armor
        String content = toBase64(playerInventory);
        String offHand = serializeItem(playerInventory.getItemInOffHand());
        String armor = itemStackArrayToBase64(playerInventory.getArmorContents());

        return new String[]{content, offHand, armor};
    }

    /**
     * Set the player inventory from a string array
     *
     * @param playerInventory The player inventory
     * @param data            The string array
     * @throws IOException If the data is invalid
     */
    public static void setPlayerInventoryFromBase64(PlayerInventory playerInventory, String[] data) throws IOException {
        playerInventory.setContents(fromBase64(data[0]).getContents());
        playerInventory.setItemInOffHand(deserializeItem(data[1]));
        playerInventory.setArmorContents(itemStackArrayFromBase64(data[2]));
    }

    /**
     * Get the player inventory from a string array
     *
     * @param items The string array
     * @return The itemstack array
     * @throws IllegalStateException
     */
    public static String itemStackArrayToBase64(ItemStack... items) throws IllegalStateException {
        if (items == null || items.length == 0) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        for (ItemStack item : items) {
            builder.append(serializeItem(item));
            builder.append(",");
        }

        return builder.substring(0, builder.length() - 1);
    }

    /**
     * Get the player inventory from a string array
     *
     * @param data The string array
     * @return The itemstack array
     * @throws IOException
     */
    public static ItemStack[] itemStackArrayFromBase64(String data) throws IOException {
        if (data.isEmpty()) {
            return new ItemStack[0];
        }

        String[] split = data.split(",");
        ItemStack[] items = new ItemStack[split.length];
        for (int i = 0; i < split.length; i++) {
            items[i] = deserializeItem(split[i]);
        }

        return items;
    }

    /**
     * A method to serialize an inventory to Base64 string.
     *
     * @param inventory
     * @return Base64 string of the provided inventory
     * @throws IllegalStateException
     */
    public static String toBase64(Inventory inventory) throws IllegalStateException {
        StringBuilder builder = new StringBuilder();
        for (ItemStack item : inventory.getContents()) {
            if (item == null || item.getType().isAir()) {
                builder.append("null,");
                continue;
            }
            builder.append(serializeItem(item));
            builder.append(",");
        }

        return builder.substring(0, builder.length() - 1);
    }

    /**
     * Gets an inventory from a Base64 string.
     *
     * @param data Base64 string of the inventory
     * @return Inventory created from the Base64 string
     */
    public static Inventory fromBase64(String data) {
        if (data.isEmpty()) {
            return null;
        }

        String[] split = data.split(",");
        Inventory inventory = Bukkit.createInventory(null, split.length);
        for (int i = 0; i < split.length; i++) {
            if (split[i].equals("null")) {
                continue;
            }
            inventory.setItem(i, deserializeItem(split[i]));
        }

        return inventory;
    }

    /**
     * A method to serialize an ItemStack to Base64 string.
     *
     * @param obj to serialize
     * @return Base64 string of the provided ItemStack
     * @throws IllegalStateException
     */
    public static String serializeItem(ItemStack obj) {
        @NotNull byte[] serializedAsBytes = obj.serializeAsBytes();
        return Base64.getEncoder().encodeToString(serializedAsBytes);
    }

    /**
     * A method to serialize an ItemStack to Base64 string.
     *
     * @param base64 to deserialize
     * @return ItemStack created from the Base64 string
     * @throws IllegalStateException
     */
    public static ItemStack deserializeItem(String base64) {
        @NotNull byte[] serializedAsBytes = Base64.getDecoder().decode(base64);
        return ItemStack.deserializeBytes(serializedAsBytes);
    }

}