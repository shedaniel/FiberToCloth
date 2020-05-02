package me.shedaniel.fiber2cloth.impl;

import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.derived.ConfigTypes;
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.derived.EnumConfigType;

public enum GroupDisplay {
    DEFAULT, TRANSITIVE, COLLAPSIBLE, COLLAPSIBLE_EXPANDED;

    public static final EnumConfigType<GroupDisplay> TYPE = ConfigTypes.makeEnum(GroupDisplay.class);

    public boolean isTransitive() {
        return this == TRANSITIVE;
    }

    /**
     * @return {@code true} if a group with this display option can be represented as a top-level category
     */
    public boolean isCategoryCandidate() {
        return this == DEFAULT;
    }
}
