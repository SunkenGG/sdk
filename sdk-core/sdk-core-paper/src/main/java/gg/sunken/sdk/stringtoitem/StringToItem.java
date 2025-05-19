package gg.sunken.sdk.stringtoitem;

import org.bukkit.inventory.ItemStack;

import java.util.*;

public class StringToItem {
    private static final List<ItemSerializer> serializers = new ArrayList<>();

    public static void register(ItemSerializer serializer) {
        if (!serializers.contains(serializer)) {
            serializers.add(serializer);
        }
    }

    public static String serialize(ItemStack itemStack) {
        StringBuilder builder = new StringBuilder();

        for (ItemSerializer serializer : serializers) {
            serializer.serialize(itemStack).ifPresent(part -> builder.append(part).append(" "));
        }

        return builder.toString().trim();
    }

    public static ItemStack deserialize(String itemString) {
        List<String> tokens = ArgumentTokenizer.tokenize(itemString.trim());

        ItemStack result = null;
        for (String token : tokens) {
            boolean handled = false;

            for (ItemSerializer serializer : serializers) {
                Optional<ItemStack> out = serializer.apply(result, token);
                if (out.isPresent()) {
                    result = out.get();
                    handled = true;
                    break;
                }
            }

            if (!handled) {
                throw new IllegalArgumentException("Unrecognized token: " + token);
            }
        }

        if (result == null) {
            throw new IllegalStateException("No valid base item type found.");
        }

        return result;
    }
}
