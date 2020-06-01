/*
 * This is free and unencumbered software released into the public domain.
 *
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 *
 * In jurisdictions that recognize copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 * For more information, please refer to <http://unlicense.org>
 */

package me.shedaniel.fiber2cloth.api;

import io.github.fablabsmc.fablabs.api.fiber.v1.FiberId;
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.derived.ConfigTypes;
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.ConfigAttribute;
import me.shedaniel.fiber2cloth.impl.ColorPickerFormat;
import me.shedaniel.fiber2cloth.impl.Fiber2ClothImpl;
import me.shedaniel.fiber2cloth.impl.GroupDisplayOption;
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
    public static ConfigAttribute<String> defaultBackground(Identifier backgroundLocation) {
        return ConfigAttribute.create(DEFAULT_BACKGROUND, DefaultTypes.IDENTIFIER_TYPE, backgroundLocation);
    }
    
    /**
     * Creates an attribute indicating that in-game config screens should use transparent backgrounds.
     *
     * <p>The returned attribute can only be applied to the root of a config tree
     */
    public static ConfigAttribute<Boolean> transparentBackground() {
        return ConfigAttribute.create(TRANSPARENT_BACKGROUND, ConfigTypes.BOOLEAN, true);
    }
    
    public static ConfigAttribute<String> categoryBackground(Identifier backgroundLocation) {
        return ConfigAttribute.create(CATEGORY_BACKGROUND, DefaultTypes.IDENTIFIER_TYPE, backgroundLocation);
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
        return ConfigAttribute.create(GROUP_DISPLAY, GroupDisplayOption.TYPE, startExpanded ? GroupDisplayOption.COLLAPSIBLE_EXPANDED : GroupDisplayOption.COLLAPSIBLE);
    }
    
    public static ConfigAttribute<String> transitive() {
        return ConfigAttribute.create(GROUP_DISPLAY, GroupDisplayOption.TYPE, GroupDisplayOption.TRANSITIVE);
    }
    
    public static ConfigAttribute<String> enumDisplay(ClothSetting.EnumHandler.EnumDisplayOption value) {
        return ConfigAttribute.create(SUGGESTION_ENUM, Fiber2ClothImpl.ENUM_DISPLAY_TYPE, value);
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
        return ConfigAttribute.create(REGISTRY_INPUT, DefaultTypes.IDENTIFIER_TYPE, registryId);
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
     *
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
     *
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
