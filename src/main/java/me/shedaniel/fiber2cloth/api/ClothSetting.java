package me.shedaniel.fiber2cloth.api;

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

    /**
     * Applies to objects.
     * Adds GUI entries for the field's inner fields at the same level as this field.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface TransitiveObject {
    }

    /**
     * Applies to objects.
     * Adds GUI entries for the field's inner fields in a collapsible section.
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
         * Translation keys making up the tooltip, one key per line.
         *
         * <p> If this value is left empty, the tooltip will be generated with {@link #count()} lines,
         * where the translation key of a line {@code n} is of the form {@code baseKey + "[" + n + "]"}.
         */
        String[] value() default {};

        /**
         * The base key used to generate translation keys if {@link #value()} is left empty.
         *
         * <p> If left empty, a base key will be generated from the concatenation of the node's name
         * and the string {@code "@Tooltip"}
         */
        String baseKey() default "";

        /**
         * The tooltip's line count, used if {@link #value()} is left empty.
         */
        int count() default 1;
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
         */
        String translationKey() default "";
    }

    /**
     * Defines how an enum is handled
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface EnumHandler {
        EnumDisplayOption display();

        enum EnumDisplayOption {
            DROPDOWN,
            BUTTON
        }
    }
}
