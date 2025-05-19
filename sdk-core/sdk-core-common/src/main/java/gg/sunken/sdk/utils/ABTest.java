package gg.sunken.sdk.utils;

import gg.sunken.sdk.collections.RandomCollection;

public class ABTest {

    private final RandomCollection<String> variants;
    private final String seed;

    public ABTest(RandomCollection<String> variants, String seed) {
        this.variants = variants;
        this.seed = seed;
    }

    public String decideVariant(Object userId) {
        if (variants == null || variants.getMap().isEmpty()) {
            throw new IllegalArgumentException("Variants list cannot be empty.");
        }

        double hashValue = hashedPosition(userId, seed);
        if (variants.getMap().size() == 1) {
            return variants.getMap().entrySet().iterator().next().getValue();
        }

        Double key = variants.getMap().ceilingKey(hashValue);
        if (key != null) {
            return variants.getMap().get(key);
        } else {
            return variants.getMap().lastEntry().getValue();
        }
    }

    private double hashedPosition(Object id, String seed) {
        int hash = id.hashCode() ^ seed.hashCode();
        hash = hash & 0x7FFFFFFF;
        return hash / (double) Integer.MAX_VALUE;
    }
}
