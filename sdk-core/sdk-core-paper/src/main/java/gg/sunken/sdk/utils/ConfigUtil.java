package gg.sunken.sdk.utils;

import gg.sunken.sdk.lang.Message;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.apache.commons.lang3.NotImplementedException;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ConfigUtil {

    private final static MiniMessage miniMessage = MiniMessage.miniMessage();

    /**
     * Expected Section Format: <br>
     * <pre>
     * {@code
     * <item-section>:
     *  material: ENUM (MATERIAL)
     *  head-url: STRING (Mojang Profile Id)
     *  name: STRING (must be minimessage)
     *  legacy-name: STRING (must be legacy)
     *  lore: LIST (STRING) (must be minimessage)
     *  - STRING
     *  legacy-lore: LIST (STRING) (must be legacy)
     *  - STRING
     *  max-stack-size: INT (optional, default is vanilla)
     *  max-durability: INT (optional, default is vanilla)
     *  tooltip:
     *     style: NamespacedKey (optional, for custom tooltip style)
     *     hide: BOOLEAN (optional, if true will hide the tooltip)
     *  glider: BOOLEAN (optional, if true will allow the item to be used as a glider)
     *  item-flags:
     *  - ENUM (ItemFlag)
     *  enchantments:
     *  - STRING (namespace:enchant-name:level)
     *  attributes:
     *      <attribute>:
     *          namespacedKey: STRING
     *          amount: DOUBLE
     *          operation: ENUM (AttributeModifier.Operation)
     *          slot: ENUM (EquipmentSlotGroup)
     *          * (repeat for each attribute)
     *  unbreakable: BOOLEAN
     *  amount: INT
     *  color: INT
     *  custom-model-data: INT
     *  banner-color: ENUM (DyeColor)
     *  banner-patterns:
     *  - STRING (DyeColor:PatternType)
     *  equippable:
     *    slot: ENUM (EquipmentSlot) (optional)
     *    sound: KEY (NamespacedKey of Sound) (optional, for equippable items)
     *    model: KEY (NamespacedKey) (optional, for equippable items)
     *    camera-overlay: KEY (NamespacedKey) (optional, for equippable items)
     *    allowed-entities: LIST (STRING) (optional, for equippable items)
     *    dispensible: BOOLEAN (optional, for equippable items)
     *    swappable: BOOLEAN (optional, for equippable items)
     *    damage-on-hurt: BOOLEAN (optional, for equippable items)
     * }
     * </pre>
     *
     * @param section The section to get the item from
     * @return The itemstack from the section
     */
    public static ItemBuilder getItemBuilder(ConfigurationSection section) {
        if (!section.contains("material")) {
            throw new RuntimeException("Material is not defined in the config for " + section.getCurrentPath());
        }

        if (!StringUtils.validateEnum(section.getString("material").toUpperCase(Locale.ROOT), Material.class)) {
            throw new RuntimeException("Material is not valid in the config for " + section.getCurrentPath());
        }

        Material material = Material.valueOf(section.getString("material").toUpperCase(Locale.ROOT));
        ItemBuilder builder = new ItemBuilder(material);

        if (section.contains("head-url")) {
            builder.setHeadUrl(section.getString("head-url"));
        }

        if (section.contains("name")) {
            builder.setName(miniMessage.deserialize(section.getString("name")));
        } else if (section.contains("legacy-name")) {
            builder.setLegacyName(LegacyHexUtils.colorify(section.getString("legacy-name")));
        }

        if (section.contains("lore")) {
            List<Component> lore = new ArrayList<>();
            for (String line : section.getStringList("lore")) {
                lore.add(miniMessage.deserialize(line));
            }
            builder.setLore(lore);
        } else if (section.contains("legacy-lore")) {
            List<String> lore = new ArrayList<>();
            for (String line : section.getStringList("legacy-lore")) {
                lore.add(LegacyHexUtils.colorify(line));
            }
            builder.setLegacyLore(lore);
        }

        if (section.contains("item-flags")) {
            for (String flag : section.getStringList("item-flags")) {
                if (!StringUtils.validateEnum(flag.toUpperCase(Locale.ROOT), ItemFlag.class)) {
                    throw new RuntimeException("ItemFlag is not valid in the config for " + section.getCurrentPath());
                }
                builder.addItemFlag(ItemFlag.valueOf(flag.toUpperCase(Locale.ROOT)));
            }
        }

        if (section.contains("max-stack-size")) {
            int maxStackSize = section.getInt("max-stack-size");
            builder.setMaxStackSize(maxStackSize);
        }

        if (section.contains("max-durability")) {
            int maxDurability = section.getInt("max-durability");
            builder.setMaxDurability(maxDurability);
        }

        if (section.contains("tooltip")) {
            if (section.contains("tooltip.style")) {
                String styleString = section.getString("tooltip.style");
                NamespacedKey styleKey = NamespacedKey.fromString(styleString);
                if (styleKey == null) {
                    throw new RuntimeException("Tooltip style is not valid in the config for " + section.getCurrentPath());
                }
                builder.setToolTipStyle(styleKey);
            }

            if (section.contains("tooltip.hide")) {
                boolean hideTooltip = section.getBoolean("tooltip.hide");
                builder.setHideToolTip(hideTooltip);
            }
        }

        if (section.contains("glider")) {
            boolean isGlider = section.getBoolean("glider");
            builder.setGlider(isGlider);
        }

        if (section.contains("enchantments")) {
            for (String enchant : section.getStringList("enchantments")) {
                String[] split = enchant.split(":");
                if (split.length != 3) {
                    throw new RuntimeException("Enchantment is not valid in the config for " + section.getCurrentPath());
                }
                builder.addEnchantment(NamespacedKey.fromString(split[0] + ":" + split[1]), Integer.parseInt(split[2]));
            }
        }

        if (section.contains("attributes")) {
            for (String key : section.getConfigurationSection("attributes").getKeys(false)) {
                ConfigurationSection attributeSection = section.getConfigurationSection("attributes." + key);
                Attribute attribute = Registry.ATTRIBUTE.get(NamespacedKey.fromString(key));
                AttributeModifier modifier = new AttributeModifier(
                        NamespacedKey.fromString(attributeSection.getString("namespaceKey")),
                        attributeSection.getDouble("amount"),
                        AttributeModifier.Operation.valueOf(attributeSection.getString("operation")),
                        EquipmentSlotGroup.getByName(attributeSection.getString("slot"))
                );

                builder.addAttributeModifier(attribute, modifier);
            }
        }

        if (section.contains("unbreakable")) {
            builder.setUnbreakable(section.getBoolean("unbreakable"));
        }

        if (section.contains("amount")) {
            builder.setAmount(section.getInt("amount"));
        }

        if (section.contains("color")) {
            builder.setLeatherColor(Color.fromRGB(section.getInt("color")));
        }

        if (section.contains("custom-model-data")) {
            builder.setCustomModelData(section.getInt("custom-model-data"));
        }

        if (section.contains("banner-patterns")) {
            List<Pattern> patterns = new ArrayList<>();

            for (String pattern : section.getStringList("banner-patterns")) {
                String[] split = pattern.split(":");
                if (split.length != 2) {
                    throw new RuntimeException("Banner Pattern is not valid in the config for " + section.getCurrentPath());
                }
                if (!StringUtils.validateEnum(split[0].toUpperCase(Locale.ROOT), DyeColor.class)) {
                    throw new RuntimeException("DyeColor is not valid in the config for " + section.getCurrentPath());
                }
                PatternType patternType = Registry.BANNER_PATTERN.get(NamespacedKey.minecraft(split[1]));
                if (patternType == null) {
                    throw new RuntimeException("PatternType is not valid in the config for " + section.getCurrentPath());
                }

                patterns.add(new Pattern(DyeColor.valueOf(split[0].toUpperCase(Locale.ROOT)), patternType));
            }

            builder.setBannerPatterns(patterns);
        }

        if (section.contains("equippable")) {
            if (section.contains("equippable.slot")) {
                EquipmentSlot slot = EquipmentSlot.valueOf(section.getString("equippable.slot").toUpperCase(Locale.ROOT));
                builder.setEquipmentSlot(slot);
            }

            if (section.contains("equippable.sound")) {
                String soundString = section.getString("equippable.sound").toUpperCase(Locale.ROOT);
                Sound sound = Registry.SOUNDS.get(NamespacedKey.fromString(soundString));
                if (sound == null) {
                    throw new RuntimeException("Sound is not valid in the config for " + section.getCurrentPath());
                }

                builder.setEquipSound(sound);
            }

            if (section.contains("equippable.model")) {
                String modelString = section.getString("equippable.model");
                NamespacedKey modelKey = NamespacedKey.fromString(modelString);
                if (modelKey == null) {
                    throw new RuntimeException("Model is not valid in the config for " + section.getCurrentPath());
                }

                builder.setModel(modelKey);
            }

            if (section.contains("equippable.camera-overlay")) {
                String overlayString = section.getString("equippable.camera-overlay");
                NamespacedKey overlayKey = NamespacedKey.fromString(overlayString);
                if (overlayKey == null) {
                    throw new RuntimeException("Camera Overlay is not valid in the config for " + section.getCurrentPath());
                }

                builder.setCameraOverlay(overlayKey);
            }

            if (section.contains("equippable.allowed-entities")) {
                boolean isList = section.isList("equippable.allowed-entities");

                if (isList) {
                    List<String> allowedEntitiesList = section.getStringList("equippable.allowed-entities");
                    List<EntityType> allowedEntities = new ArrayList<>();

                    for (String entity : allowedEntitiesList) {
                        if (!StringUtils.validateEnum(entity.toUpperCase(Locale.ROOT), EntityType.class)) {
                            throw new RuntimeException("EntityType is not valid in the config for " + section.getCurrentPath());
                        }
                        EntityType entityType = EntityType.valueOf(entity.toUpperCase(Locale.ROOT));
                        allowedEntities.add(entityType);
                    }

                    builder.setAllowedEntities(allowedEntities);
                } else {
                    EntityType allowedEntity;
                    String allowedEntityString = section.getString("equippable.allowed-entity");
                    if (!StringUtils.validateEnum(allowedEntityString.toUpperCase(Locale.ROOT), EntityType.class)) {
                        throw new RuntimeException("EntityType is not valid in the config for " + section.getCurrentPath());
                    }
                    allowedEntity = EntityType.valueOf(allowedEntityString.toUpperCase(Locale.ROOT));
                    builder.setAllowedEntities(allowedEntity);
                }
            }

            if (section.contains("equippable.dispensible")) {
                boolean canDispense = section.getBoolean("equippable.dispensible");
                builder.setDispensable(canDispense);
            }

            if (section.contains("equippable.swappable")) {
                boolean canSwap = section.getBoolean("equippable.swappable");
                builder.setSwappable(canSwap);
            }

            if (section.contains("equippable.damage-on-hurt")) {
                boolean damageOnHurt = section.getBoolean("equippable.damage-on-hurt");
                builder.setDamageOnHurt(damageOnHurt);
            }
        }

        return builder;
    }


    /**
     * Expected input format:
     * <pre>
     * {@code
     * <message-id>:
     *   message: <message string or string list>
     *   geyser: <message string or string list for bedrock players only>
     *   actionbar: <actionbar string>
     *   geyser-actionbar: <actionbar string for bedrock players only>
     *   title:
     *     title: <title string> (optional, requires subtitle to be set if not set)
     *     subtitle: <subtitle string> (optional, requires title to be set if not set)
     *     fadeIn: <fade in time> (default: 10)
     *     stay: <stay time> (default: 70)
     *     fadeOut: <fade out time> (default: 20)
     *   geyser-title:
     *     title: <title string> (optional, requires subtitle to be set if not set)
     *     subtitle: <subtitle string> (optional, requires title to be set if not set)
     *     fadeIn: <fade in time> (default: 10)
     *     stay: <stay time> (default: 70)
     *     fadeOut: <fade out time> (default: 20)
     *   sounds:
     *    <whatever>:
     *      id: <sound string id> (default: minecraft:block.amethyst_block.hit)
     *      volume: <sound volume> (default: 1.0)
     *      pitch: <sound pitch> (default: 1.0)
     *      offset: <sound offset> (default: 0)
     *      tick-offset: <sound tick offset> (default: 0)
     * }
     * </pre>
     */
    //todo: commented to allow compiling
    public static Message getMessage(ConfigurationSection section) {
//        if (section == null) {
//            throw new IllegalArgumentException("ConfigurationSection cannot be null");
//        }
//
//        Message.MessageBuilder builder = Message.builder();
//        builder.key(section.getCurrentPath());
//
//        if (section.contains("message")) {
//            // Handle both String and List<String> for messages
//            if (section.isList("message")) {
//                List<String> messages = section.getStringList("message");
//                builder.message(messages);
//            } else {
//                String message = section.getString("message");
//                builder.message(List.of(message));
//            }
//        }
//
//        if (section.contains("geyser-message")) {
//            // Handle both String and List<String> for geyser messages
//            if (section.isList("geyser-message")) {
//                List<String> geyserMessages = section.getStringList("geyser-message");
//                builder.geyserMessage(geyserMessages);
//            } else {
//                String geyserMessage = section.getString("geyser-message");
//                builder.geyserMessage(List.of(geyserMessage));
//            }
//        }
//
//        if (section.contains("actionbar")) {
//            String actionbar = section.getString("actionbar");
//            builder.actionbar(actionbar);
//        }
//
//        if (section.contains("geyser-actionbar")) {
//            String geyserActionbar = section.getString("geyser-actionbar");
//            builder.actionbarGeyser(geyserActionbar);
//        }
//
//        if (section.contains("title")) {
//            ConfigurationSection titleSection = section.getConfigurationSection("title");
//            Title.TitleBuilder titleBuilder = Title.builder();
//
//            if (titleSection.contains("title")) {
//                String title = titleSection.getString("title");
//                titleBuilder.title(title);
//            }
//
//            if (titleSection.contains("subtitle")) {
//                String subtitle = titleSection.getString("subtitle");
//                titleBuilder.subtitle(subtitle);
//            }
//
//            if (titleSection.contains("fadeIn")) {
//                int fadeIn = titleSection.getInt("fadeIn", 10);
//                titleBuilder.fadeIn(fadeIn);
//            }
//
//            if (titleSection.contains("stay")) {
//                int stay = titleSection.getInt("stay", 70);
//                titleBuilder.stay(stay);
//            }
//
//            if (titleSection.contains("fadeOut")) {
//                int fadeOut = titleSection.getInt("fadeOut", 20);
//                titleBuilder.fadeOut(fadeOut);
//            }
//        }
//
//        if (section.contains("geyser-title")) {
//            ConfigurationSection geyserTitleSection = section.getConfigurationSection("geyser-title");
//            Title.TitleBuilder titleBuilder = Title.builder();
//
//            if (geyserTitleSection.contains("title")) {
//                String title = geyserTitleSection.getString("title", "");
//                titleBuilder.title(title);
//            }
//
//            if (geyserTitleSection.contains("subtitle")) {
//                String subtitle = geyserTitleSection.getString("subtitle", "");
//                titleBuilder.subtitle(subtitle);
//            }
//
//            if (geyserTitleSection.contains("fadeIn")) {
//                int fadeIn = geyserTitleSection.getInt("fadeIn", 10);
//                titleBuilder.fadeIn(fadeIn);
//            }
//
//            if (geyserTitleSection.contains("stay")) {
//                int stay = geyserTitleSection.getInt("stay", 70);
//                titleBuilder.stay(stay);
//            }
//
//            if (geyserTitleSection.contains("fadeOut")) {
//                int fadeOut = geyserTitleSection.getInt("fadeOut", 20);
//                titleBuilder.fadeOut(fadeOut);
//            }
//        }
//
//        if (section.contains("sounds")) {
//            ConfigurationSection soundsSection = section.getConfigurationSection("sounds");
//
//            for (String key : soundsSection.getKeys(false)) {
//                ConfigurationSection soundSection2 = soundsSection.getConfigurationSection(key);
//                if (soundSection2 == null) {
//                    continue; // Skip if the section is null
//                }
//
//                gg.sunken.sdk.lang.Sound.SoundBuilder soundBuilder = gg.sunken.sdk.lang.Sound.builder();
//
//                String soundId = soundSection2.getString("id", "minecraft:block.amethyst_block.hit");
//                soundBuilder.sound(soundId);
//
//                float volume = (float) soundSection2.getDouble("volume", 1.0);
//                soundBuilder.volume(volume);
//
//                float pitch = (float) soundSection2.getDouble("pitch", 1.0);
//                soundBuilder.pitch(pitch);
//
//                int offset = soundSection2.getInt("offset", 0);
//                soundBuilder.offset(offset);
//
//                int tickOffset = soundSection2.getInt("tick-offset", 0);
//                soundBuilder.tickOffset(tickOffset);
//            }
//        }
//
//        return builder.build();
        throw new NotImplementedException("Getting messages from config is not currently implemented");
    }
}
