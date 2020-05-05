package me.shedaniel.fiber2cloth.api;

import io.github.fablabsmc.fablabs.api.fiber.v1.FiberId;
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.derived.ConfigTypes;
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.ConfigAttribute;
import me.shedaniel.fiber2cloth.impl.ColorPickerFormat;
import me.shedaniel.fiber2cloth.impl.Fiber2ClothImpl;
import me.shedaniel.fiber2cloth.impl.GroupDisplay;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.MutableRegistry;
import net.minecraft.util.registry.Registry;

public final class ClothAttributes {

    /* aesthetics */

    public static final FiberId DEFAULT_BACKGROUND = id("default_background");
    public static final FiberId TRANSPARENT_BACKGROUND = id("transparent_background");
    public static final FiberId CATEGORY_BACKGROUND = id("category_background");

    /**
     * Creates an attribute describing the default background used by every category that
     * does not have a specific background set.
     *
     * <p>The returned attribute can only be applied to the root of a config tree
     *
     * @param backgroundLocation the location of the texture to use for the background
     * @see #TRANSPARENT_BACKGROUND
     */
    public static ConfigAttribute<String> defaultBackground(String backgroundLocation) {
        return ConfigAttribute.create(DEFAULT_BACKGROUND, Fiber2ClothImpl.IDENTIFIER_TYPE.getSerializedType(), backgroundLocation);
    }

    public static ConfigAttribute<String> defaultBackground(Identifier backgroundLocation) {
        return ConfigAttribute.create(DEFAULT_BACKGROUND, Fiber2ClothImpl.IDENTIFIER_TYPE, backgroundLocation);
    }

    /**
     * Creates an attribute indicating that in-game config screens should use transparent backgrounds.
     *
     * <p>The returned attribute can only be applied to the root of a config tree
     */
    public static ConfigAttribute<Boolean> transparentBackground() {
        return ConfigAttribute.create(TRANSPARENT_BACKGROUND, ConfigTypes.BOOLEAN, true);
    }

    public static ConfigAttribute<String> categoryBackground(String backgroundLocation) {
        return ConfigAttribute.create(CATEGORY_BACKGROUND, Fiber2ClothImpl.IDENTIFIER_TYPE.getSerializedType(), backgroundLocation);
    }

    public static ConfigAttribute<String> categoryBackground(Identifier backgroundLocation) {
        return ConfigAttribute.create(CATEGORY_BACKGROUND, Fiber2ClothImpl.IDENTIFIER_TYPE, backgroundLocation);
    }

    /* display options */

    public static final FiberId EXCLUDED = id("excluded");
    public static final FiberId GROUP_DISPLAY = id("group_display");
    public static final FiberId SUGGESTION_ENUM = id("suggestion");
    public static final FiberId SLIDER = id("slider");
    public static final FiberId COLOR_PICKER = id("color_picker");

    public static ConfigAttribute<Boolean> excluded() {
        return ConfigAttribute.create(EXCLUDED, ConfigTypes.BOOLEAN, true);
    }

    public static ConfigAttribute<String> collapsible(boolean startExpanded) {
        return ConfigAttribute.create(GROUP_DISPLAY, GroupDisplay.TYPE, startExpanded ? GroupDisplay.COLLAPSIBLE_EXPANDED : GroupDisplay.COLLAPSIBLE);
    }

    public static ConfigAttribute<String> transitive() {
        return ConfigAttribute.create(GROUP_DISPLAY, GroupDisplay.TYPE, GroupDisplay.TRANSITIVE);
    }

    public static ConfigAttribute<Boolean> suggestionEnum() {
        return ConfigAttribute.create(SUGGESTION_ENUM, ConfigTypes.BOOLEAN, true);
    }

    public static ConfigAttribute<Boolean> slider() {
        return ConfigAttribute.create(SLIDER, ConfigTypes.BOOLEAN, true);
    }

    public static ConfigAttribute<String> colorPicker(boolean alpha) {
        return ConfigAttribute.create(COLOR_PICKER, ColorPickerFormat.TYPE, alpha ? ColorPickerFormat.ARGB : ColorPickerFormat.RGB);
    }

    /* type properties */

    public static final FiberId REGISTRY_INPUT = id("registry_object");
    public static final FiberId REQUIRES_RESTART = id("requires_restart");

    public static ConfigAttribute<String> registryInput(MutableRegistry<?> registry) {
        Identifier registryId = Registry.REGISTRIES.getId(registry);
        if (registryId == null) throw new IllegalArgumentException("Unregistered registry " + registry);
        return registryInput(registryId);
    }

    public static ConfigAttribute<String> registryInput(Identifier registryId) {
        return ConfigAttribute.create(REGISTRY_INPUT, Fiber2ClothImpl.IDENTIFIER_TYPE, registryId);
    }

    public static ConfigAttribute<Boolean> requiresRestart() {
        return ConfigAttribute.create(REQUIRES_RESTART, ConfigTypes.BOOLEAN, true);
    }

    /* descriptions */

    public static final FiberId PREFIX_TEXT = id("prefix_text");
    public static final FiberId TOOLTIP = id("tooltip");

    public static ConfigAttribute<String> prefixText(String prefixKey) {
        return ConfigAttribute.create(PREFIX_TEXT, ConfigTypes.STRING, prefixKey);
    }

    /**
     * Creates a localizable tooltip attribute for a node.
     *
     * <p> The tooltip's translation key will be generated from the concatenation
     * of the node's own translation key and the string "@Tooltip"
     *
     * <p> If the localization file contains entries of the form {@code translationKey + "[i]"}, where {@code i}
     * is a number starting at 1, one tooltip line will be appended for every entry in the sequence.
     * Example:
     * <pre>{@code
     * "mymod.config.entry@Tooltip[1]": "First line",
     * "mymod.config.entry@Tooltip[2]": "Second line"
     * }</pre>
     * @see ClothSetting.Tooltip
     */
    public static ConfigAttribute<String> tooltip() {
        return tooltip(null);
    }

    /**
     * Creates a localizable tooltip attribute for a node.
     *
     * <p> If {@code tooltipKey} is empty or null, the translation key will be generated from the concatenation
     * of the node's own translation key and the string "@Tooltip"
     *
     * <p> If the localization file contains entries of the form {@code translationKey + "[i]"}, where {@code i}
     * is a number starting at 1, one tooltip line will be appended for every entry in the sequence.
     * Example:
     * <pre>{@code
     * "mymod.config.entry@Tooltip[1]": "First line",
     * "mymod.config.entry@Tooltip[2]": "Second line"
     * }</pre>
     * @see ClothSetting.Tooltip
     */
    public static ConfigAttribute<String> tooltip(String tooltipKey) {
        return ConfigAttribute.create(TOOLTIP, ConfigTypes.STRING, tooltipKey == null ? "" : tooltipKey);
    }


    private static FiberId id(String name) {
        return new FiberId("fiber2cloth", name);
    }

    private ClothAttributes() {
    }

}
