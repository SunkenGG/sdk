package gg.sunken.sdk.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.banner.Pattern;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.inventory.meta.components.EquippableComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ItemBuilder {

    private final static LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.builder().hexColors().useUnusualXRepeatedCharacterHexFormat().character('&').build();
    private final List<Consumer<ItemMeta>> queuedChanges = new ArrayList<>();
    private ItemStack stack;

    public ItemBuilder(ItemStack stack) {
        this.stack = stack;
    }

    public ItemBuilder(Material material) {
        stack = new ItemStack(material);
    }

    public ItemBuilder(Material material, int amount) {
        stack = new ItemStack(material, amount);
    }

    public static ItemBuilder of(ItemStack stack) {
        return new ItemBuilder(stack);
    }

    public static ItemBuilder of(Material material) {
        return new ItemBuilder(material);
    }

    public static ItemBuilder of(Material material, int amount) {
        return new ItemBuilder(material, amount);
    }

    public static ItemBuilder of(ItemBuilder builder) {
        return new ItemBuilder(builder.stack);
    }

    public ItemBuilder setLegacyName(String name) {
        queuedChanges.add(meta -> meta.displayName(SERIALIZER.deserialize(name)));
        return this;
    }

    public ItemBuilder setName(Component name) {
        queuedChanges.add(meta -> meta.displayName(name));
        return this;
    }

    public ItemBuilder setLegacyLore(List<String> lore) {
        queuedChanges.add(meta -> {
            List<Component> components = new ArrayList<>();
            lore.forEach(line -> components.add(SERIALIZER.deserialize(line)));
            meta.lore(components);
        });
        return this;
    }

    public ItemBuilder setLore(List<Component> lore) {
        queuedChanges.add(meta -> meta.lore(lore));
        return this;
    }

    public ItemBuilder addLegacyLore(String line) {
        queuedChanges.add(meta -> {
            List<Component> components = new ArrayList<>(meta.lore());
            components.add(SERIALIZER.deserialize(line));
            meta.lore(components);
        });
        return this;
    }

    public ItemBuilder addLore(Component line) {
        queuedChanges.add(meta -> {
            List<Component> components = new ArrayList<>(meta.lore());
            components.add(line);
            meta.lore(components);
        });
        return this;
    }

    public ItemBuilder setModel(NamespacedKey key) {
        queuedChanges.add(meta -> {
            EquippableComponent equippable = meta.getEquippable();
            equippable.setModel(key);
        });
        return this;
    }

    public ItemBuilder setEquipmentSlot(EquipmentSlot slot) {
        queuedChanges.add(meta -> {
            EquippableComponent equippable = meta.getEquippable();
            equippable.setSlot(slot);
        });
        return this;
    }

    public ItemBuilder setEquipSound(Sound sound) {
        queuedChanges.add(meta -> {
            EquippableComponent equippable = meta.getEquippable();
            equippable.setEquipSound(sound);
        });
        return this;
    }

    public ItemBuilder setCameraOverlay(NamespacedKey key) {
        queuedChanges.add(meta -> {
            EquippableComponent equippable = meta.getEquippable();
            equippable.setCameraOverlay(key);
        });
        return this;
    }

    public ItemBuilder setAllowedEntities(EntityType type) {
        queuedChanges.add(meta -> {
            EquippableComponent equippable = meta.getEquippable();
            equippable.setAllowedEntities(type);
        });
        return this;
    }

    public ItemBuilder setAllowedEntities(List<EntityType> types) {
        queuedChanges.add(meta -> {
            EquippableComponent equippable = meta.getEquippable();
            equippable.setAllowedEntities(types);
        });
        return this;
    }

    public ItemBuilder setDispensable(boolean dispensable) {
        queuedChanges.add(meta -> {
            EquippableComponent equippable = meta.getEquippable();
            equippable.setDispensable(dispensable);
        });
        return this;
    }

    public ItemBuilder setSwappable(boolean swappable) {
        queuedChanges.add(meta -> {
            EquippableComponent equippable = meta.getEquippable();
            equippable.setSwappable(swappable);
        });
        return this;
    }

    public ItemBuilder setDamageOnHurt(boolean damageOnHurt) {
        queuedChanges.add(meta -> {
            EquippableComponent equippable = meta.getEquippable();
            equippable.setDamageOnHurt(damageOnHurt);
        });
        return this;
    }

    public ItemBuilder setMaxDurability(int maxDurability) {
        queuedChanges.add(meta -> {
            if (meta instanceof Damageable damageable) {
                damageable.setMaxDamage(maxDurability);
            }
        });
        return this;
    }

    public ItemBuilder setMaxStackSize(int maxStackSize) {
        queuedChanges.add(meta -> {
            if (stack.getType() != Material.AIR) {
                meta.setMaxStackSize(maxStackSize);
            }
        });
        return this;
    }

    public ItemBuilder setToolTipStyle(NamespacedKey key) {
        queuedChanges.add(meta -> {
            meta.setTooltipStyle(key);
        });
        return this;
    }

    public ItemBuilder setHideToolTip(boolean hideToolTip) {
        queuedChanges.add(meta -> {
            meta.setHideTooltip(hideToolTip);
        });
        return this;
    }

    public ItemBuilder setGlider(boolean glider) {
        queuedChanges.add(meta -> {
            meta.setGlider(glider);
        });
        return this;
    }

    public ItemBuilder setHeadUrl(String string) {
        queuedChanges.add(meta -> {
            if (meta instanceof SkullMeta) {
                ItemStackUtils.applyHeadUrl((SkullMeta) meta, string);
            }
        });
        return this;
    }

    public ItemBuilder setSkullOwner(String owner) {
        queuedChanges.add(meta -> {
            if (meta instanceof SkullMeta) {
                ((SkullMeta) meta).setOwningPlayer(Bukkit.getOfflinePlayer(owner));
            }
        });
        return this;
    }

    public ItemBuilder setLeatherColor(int red, int green, int blue) {
        queuedChanges.add(meta -> {
            if (meta instanceof LeatherArmorMeta) {
                ((LeatherArmorMeta) meta).setColor(Color.fromRGB(red, green, blue));
            }
        });
        return this;
    }

    public ItemBuilder setLeatherColor(Color color) {
        queuedChanges.add(meta -> {
            if (meta instanceof LeatherArmorMeta) {
                ((LeatherArmorMeta) meta).setColor(color);
            }
        });
        return this;
    }

    public ItemBuilder setBannerPattern(Pattern pattern) {
        queuedChanges.add(meta -> {
            if (meta instanceof BannerMeta) {
                ((BannerMeta) meta).addPattern(pattern);
            }
        });
        return this;
    }

    public ItemBuilder setBannerPatterns(List<Pattern> patterns) {
        queuedChanges.add(meta -> {
            if (meta instanceof BannerMeta) {
                ((BannerMeta) meta).setPatterns(patterns);
            }
        });
        return this;
    }

    public ItemBuilder setAmount(int amount) {
        queuedChanges.add(meta -> stack.setAmount(amount));
        return this;
    }

    public ItemBuilder setDurability(short durability) {
        queuedChanges.add(meta -> stack.setDurability(durability));
        return this;
    }

    public ItemBuilder setUnbreakable(boolean unbreakable) {
        queuedChanges.add(meta -> meta.setUnbreakable(unbreakable));
        return this;
    }

    public ItemBuilder setGlowing(boolean glowing) {
        queuedChanges.add(meta -> {
            meta.setEnchantmentGlintOverride(glowing);
        });
        return this;
    }

    public ItemBuilder addItemFlag(ItemFlag flag) {
        queuedChanges.add(meta -> meta.addItemFlags(flag));
        return this;
    }

    public ItemBuilder removeItemFlag(ItemFlag flag) {
        queuedChanges.add(meta -> meta.removeItemFlags(flag));
        return this;
    }

    public ItemBuilder addEnchantment(NamespacedKey key, int level) {
        queuedChanges.add(meta -> meta.addEnchant(Enchantment.getByKey(key), level, true));
        return this;
    }

    public ItemBuilder addEnchantment(Enchantment enchantment, int level) {
        queuedChanges.add(meta -> meta.addEnchant(enchantment, level, true));
        return this;
    }

    public ItemBuilder removeEnchantment(NamespacedKey key) {
        queuedChanges.add(meta -> meta.removeEnchant(Enchantment.getByKey(key)));
        return this;
    }

    public ItemBuilder removeEnchantment(Enchantment enchantment) {
        queuedChanges.add(meta -> meta.removeEnchant(enchantment));
        return this;
    }

    public ItemBuilder setCustomModelData(int data) {
        queuedChanges.add(meta -> meta.setCustomModelData(data));
        return this;
    }

    public ItemBuilder addAttributeModifier(Attribute attribute, AttributeModifier modifier) {
        queuedChanges.add(meta -> meta.addAttributeModifier(attribute, modifier));
        return this;
    }

    public ItemBuilder removeAttributeModifier(Attribute attribute) {
        queuedChanges.add(meta -> meta.removeAttributeModifier(attribute));
        return this;
    }

    public ItemBuilder clearEnchantments() {
        queuedChanges.add(meta -> meta.getEnchants().keySet().forEach(meta::removeEnchant));
        return this;
    }

    public ItemBuilder clearFlags() {
        queuedChanges.add(meta -> meta.getItemFlags().forEach(meta::removeItemFlags));
        return this;
    }

    public ItemBuilder clearLore() {
        queuedChanges.add(meta -> meta.lore(new ArrayList<>()));
        return this;
    }

    public ItemBuilder clearName() {
        queuedChanges.add(meta -> meta.displayName(null));
        return this;
    }

    public ItemBuilder clearCustomModelData() {
        queuedChanges.add(meta -> meta.setCustomModelData(null));
        return this;
    }

    public ItemBuilder clearAttributes() {
        queuedChanges.add(meta -> meta.getAttributeModifiers().keySet().forEach(meta::removeAttributeModifier));
        return this;
    }

    public ItemStack build() {
        ItemStack clone = stack.clone();
        if (queuedChanges.isEmpty()) {
            return clone;
        }

        ItemMeta meta = clone.getItemMeta();
        queuedChanges.forEach(change -> change.accept(meta));
        clone.setItemMeta(meta);
        stack = clone;
        queuedChanges.clear();

        return clone;
    }
}