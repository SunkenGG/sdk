package gg.sunken.sdk.stringtoitem;

import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public interface ItemSerializer {
    /**
     * Applies a token to the given item. If the item is null, it should try to create the base item.
     * Otherwise, it should try to apply metadata.
     *
     * @param current the current ItemStack or null if base not yet created
     * @param token   the token to process
     * @return the modified or created ItemStack, or empty if this serializer can't handle the token
     */
    Optional<ItemStack> apply(ItemStack current, String token);

    /**
     * Try to serialize an ItemStack into a string format handled by this serializer.
     * Return only the fragment you are responsible for.
     */
    Optional<String> serialize(ItemStack item);
}
