package gg.sunken.sdk.stringtoitem.serializers;

import gg.sunken.sdk.stringtoitem.ItemSerializer;
import com.google.auto.service.AutoService;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Optional;

@AutoService(ItemSerializer.class)
public class MinecraftItemSerializer implements ItemSerializer {

    @Override
    public Optional<ItemStack> apply(ItemStack current, String token) {
        if (current == null) {
            // Try to parse base type
            try {
                Material material = Material.valueOf(token.toUpperCase());
                return Optional.of(new ItemStack(material));
            } catch (IllegalArgumentException ignored) {
            }
            return Optional.empty();
        }

        // Metadata
        ItemMeta meta = current.getItemMeta();

        if (token.startsWith("amount:")) {
            current.setAmount(Integer.parseInt(token.substring(7)));
            return Optional.of(current);
        }

        if (token.startsWith("enchant:")) {
            String[] parts = token.substring(8).split(":");
            if (parts.length == 3) {
                Enchantment enchant = Enchantment.getByKey(new NamespacedKey(parts[0], parts[1]));
                int level = Integer.parseInt(parts[2]);
                if (enchant != null) {
                    meta.addEnchant(enchant, level, true);
                    current.setItemMeta(meta);
                    return Optional.of(current);
                }
            }
        }

        if (token.startsWith("durability:")) {
            current.setDurability(Short.parseShort(token.substring(11)));
            return Optional.of(current);
        }

        if (token.startsWith("name:")) {
            String name = token.substring(5);
            if (name.startsWith("\"")) name = name.substring(1);
            if (name.endsWith("\"")) name = name.substring(0, name.length() - 1);
            meta.displayName(MiniMessage.miniMessage().deserialize(name));
            current.setItemMeta(meta);
            return Optional.of(current);
        }

        if (token.equalsIgnoreCase("unbreakable")) {
            meta.setUnbreakable(true);
            current.setItemMeta(meta);
            return Optional.of(current);
        }

        if (token.startsWith("custommodeldata:")) {
            meta.setCustomModelData(Integer.parseInt(token.substring(16)));
            current.setItemMeta(meta);
            return Optional.of(current);
        }

        return Optional.empty();
    }

    @Override
    public Optional<String> serialize(ItemStack item) {
        StringBuilder out = new StringBuilder();

        out.append(item.getType());

        if (item.getAmount() != 1) {
            out.append(" amount:").append(item.getAmount());
        }

        if (item.getDurability() != 0) {
            out.append(" durability:").append(item.getDurability());
        }

        if (!item.hasItemMeta()) return Optional.of(out.toString());

        ItemMeta meta = item.getItemMeta();

        if (meta.hasDisplayName()) {
            out.append(" name:\"").append(meta.getDisplayName()).append("\"");
        }

        if (meta.isUnbreakable()) {
            out.append(" unbreakable");
        }

        if (meta.hasCustomModelData()) {
            out.append(" custommodeldata:").append(meta.getCustomModelData());
        }

        meta.getEnchants().forEach((ench, lvl) ->
                out.append(" enchant:").append(ench.getKey().getNamespace())
                        .append(":").append(ench.getKey().getKey())
                        .append(":").append(lvl)
        );

        return Optional.of(out.toString());
    }
}
