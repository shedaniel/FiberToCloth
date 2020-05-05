package me.shedaniel.fiber2cloth.impl;

import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.derived.ConfigTypes;
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.derived.EnumConfigType;

public enum ColorPickerFormat {
    RGB, ARGB;

    public static final EnumConfigType<ColorPickerFormat> TYPE = ConfigTypes.makeEnum(ColorPickerFormat.class);
}
