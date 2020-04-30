package me.shedaniel.fiber2cloth.api;

import me.zeroeightsix.fiber.api.FiberId;
import me.zeroeightsix.fiber.api.tree.ConfigAttribute;
import net.minecraft.util.Identifier;

import java.util.stream.IntStream;

public final class ClothAttributes {
    private ClothAttributes() {}

    public static final FiberId DEFAULT_BACKGROUND = id("background");
    public static final FiberId CATEGORY_BACKGROUND = id("category_background");
    public static final FiberId PREFIX_TEXT = id("prefix_text");
    public static final FiberId TOOLTIP = id("tooltip");
    public static final FiberId TRANSITIVE = id("transitive");

    public static ConfigAttribute<Identifier> defaultBackground(String backgroundLocation) {
        return ConfigAttribute.create(DEFAULT_BACKGROUND, Identifier.class, new Identifier(backgroundLocation));
    }

    public static ConfigAttribute<Identifier> defaultBackground(Identifier backgroundLocation) {
        return ConfigAttribute.create(DEFAULT_BACKGROUND, Identifier.class, backgroundLocation);
    }

    public static ConfigAttribute<Identifier> categoryBackground(Identifier backgroundLocation) {
        return ConfigAttribute.create(CATEGORY_BACKGROUND, Identifier.class, backgroundLocation);
    }

    public static ConfigAttribute<Identifier> categoryBackground(String backgroundLocation) {
        return ConfigAttribute.create(CATEGORY_BACKGROUND, Identifier.class, new Identifier(backgroundLocation));
    }

    public static ConfigAttribute<String> prefixText(String prefixKey) {
        return ConfigAttribute.create(PREFIX_TEXT, String.class, prefixKey);
    }

    public static ConfigAttribute<String[]> tooltip(String baseKey, int numberOfLines) {
        return tooltip(IntStream.range(1, numberOfLines + 1).mapToObj(i -> baseKey + i).toArray(String[]::new));
    }

    public static ConfigAttribute<String[]> tooltip(String... tooltipKeys) {
        return ConfigAttribute.create(TOOLTIP, String[].class, tooltipKeys);
    }

    public static ConfigAttribute<Boolean> transitive() {
        return ConfigAttribute.create(TRANSITIVE, Boolean.class, true);
    }

    private static FiberId id(String name) {
        return new FiberId("fiber2cloth", name);
    }
}
