package me.shedaniel.fiber2cloth.api;

import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.EnumSerializableType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public @interface ClothSetting {

    /**
     * Sets the background of a specific category in the config GUI
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface CategoryBackground {
        String value();
    }

    /**
     * Removes the field from the config GUI.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface Excluded {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface RequiresRestart {}

    /**
     * Applies to {@linkplain io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting.Group setting groups}.
     * Adds GUI entries for the field's inner fields at the same level as this field.
     *
     * <p> When this annotation is applied to a direct child of the root (thus normally displayed as its own separate category),
     * the group's content will appear in the default category.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface TransitiveObject {
    }

    /**
     * Applies to {@linkplain io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting.Group setting groups}.
     * Adds GUI entries for the field's inner fields in a collapsible section.
     *
     * <p> When this annotation is applied to a direct child of the root (thus normally displayed as its own separate category),
     * the collapsible will appear in the default category. Otherwise, this annotation is only useful to specify
     * the collapsible's parameters.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface CollapsibleObject {
        boolean startExpanded() default false;
    }

    /**
     * Applies a tooltip to list entries that support it, defined in your lang file.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface Tooltip {
        /**
         * Sets the translation key for the content of the tooltip.
         *
         * <p> If left empty, the translation key will be generated from the concatenation
         * of this object's translation key and the string "@Tooltip"
         *
         * <p> If the localization file contains entries of the form {@code translationKey + "[i]"}, where {@code i}
         * is a number starting at 1, one tooltip line will be appended for every entry in the sequence.
         * Example:
         * <pre>{@code
         * "mymod.config.entry@Tooltip[1]": "First line",
         * "mymod.config.entry@Tooltip[2]": "Second line"
         * }</pre>
         * @see ClothAttributes#tooltip(String)
         */
        String value() default "";
    }

    /**
     * Applies a section of text right before this entry, defined in your lang file.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface PrefixText {
        /**
         * Sets the translation key for the content of the prefix text.
         *
         * <p> If left empty, the translation key will be generated from the concatenation
         * of this object's translation key and the string "@PrefixText"
         *
         * <p> If the localization file contains entries of the form {@code translationKey + "[i]"}, where {@code i}
         * is a number starting at 1, one tooltip line will be appended for every entry in the sequence.
         * Example:
         * <pre>{@code
         * "mymod.config.entry@PrefixText[1]": "First line",
         * "mymod.config.entry@PrefixText[2]": "Second line"
         * }</pre>
         * @see ClothAttributes#prefixText(String)
         */
        String value() default "";
    }

    /**
     * If applied to an enum field (or any field that is converted to an
     * {@link EnumSerializableType}, defines how the enum is displayed.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface EnumHandler {
        EnumDisplayOption value();

        enum EnumDisplayOption {
            /**
             * Displays the setting as a cycling button. This is the default option.
             */
            BUTTON,
            /**
             * Displays the setting as a dropdown menu, showing every possibility on click.
             */
            DROPDOWN,
            /**
             * Displays the setting as a text field with suggestions.
             *
             * <p>This option can be useful for settings with a large amount of possible values.
             */
            SUGGESTION_INPUT
        }
    }

    /**
     * Applies to numerical settings (types converted to a {@link io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.DecimalSerializableType}).
     * Replaces the default number input with a slider that uses the type's minimum, maximum, and step
     * @see io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting.Constrain.Range
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface Slider {}

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface RegistryInput {
        /**
         * If set to a valid {@link Registry} identifier, the generated text input will use the values
         * in that registry for suggestions.
         *
         * <p>The returned string should represent a valid {@link Identifier} that exists as
         * a key in {@link Registry#REGISTRIES}.
         *
         * @return a string denoting a valid {@link Identifier} for a {@link Registry}
         */
        String value();
    }

    /**
     * Applies to a setting of type {@link io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.derived.ConfigTypes#INTEGER}.
     * Replaces the default number input with a color picker.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface ColorPicker {
        boolean alpha();
    }
}
