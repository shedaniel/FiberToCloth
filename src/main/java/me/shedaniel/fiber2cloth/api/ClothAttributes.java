package me.shedaniel.fiber2cloth.api;

import me.zeroeightsix.fiber.api.FiberId;
import me.zeroeightsix.fiber.api.tree.ConfigAttribute;
import net.minecraft.util.Identifier;

public final class ClothAttributes {
    private ClothAttributes() {}

    public static final FiberId BACKGROUND = id("background");
    public static final FiberId CATEGORY_BACKGROUND = id("category_background");

    public static ConfigAttribute<Identifier> background(String backgroundLocation) {
        return ConfigAttribute.create(BACKGROUND, Identifier.class, new Identifier(backgroundLocation));
    }

    public static ConfigAttribute<Identifier> background(Identifier backgroundLocation) {
        return ConfigAttribute.create(BACKGROUND, Identifier.class, backgroundLocation);
    }

    public static ConfigAttribute<Identifier> categoryBackground(Identifier backgroundLocation) {
        return ConfigAttribute.create(CATEGORY_BACKGROUND, Identifier.class, backgroundLocation);
    }

    public static ConfigAttribute<Identifier> categoryBackground(String backgroundLocation) {
        return ConfigAttribute.create(CATEGORY_BACKGROUND, Identifier.class, new Identifier(backgroundLocation));
    }

    private static FiberId id(String name) {
        return new FiberId("fiber2cloth", name);
    }
}
