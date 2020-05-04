package me.shedaniel.fiber2cloth.api;

import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.SerializableType;
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.ConfigLeaf;
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.PropertyMirror;

import java.util.Optional;
import java.util.function.Function;

@FunctionalInterface
public interface GuiEntryProvider<R, S, T extends SerializableType<S>> {
    /**
     * Converts a config property to a gui entry.
     *
     * @param leaf the property to convert
     * @param type the property's {@link ConfigLeaf#getType() serializable type}
     * @param mirror a mirror that can be used to access the property's value
     * @param defaultValue the leaf's {@link ConfigLeaf#getDefaultValue() default value}
     * @param suggestedErrorSupplier an error supplier that can be used by the generated config entry to check the validity of input values
     * @return an entry allowing users to edit the property, or {@code null} to delegate to a more general provider
     */
    /*@Nullable*/
    AbstractConfigListEntry<?> apply(ConfigLeaf<S> leaf, T type, PropertyMirror<R> mirror, R defaultValue, Function<R, Optional<String>> suggestedErrorSupplier);
}
