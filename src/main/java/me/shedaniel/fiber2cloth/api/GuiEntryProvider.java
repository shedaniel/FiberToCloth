package me.shedaniel.fiber2cloth.api;

import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.SerializableType;
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.ConfigLeaf;
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.PropertyMirror;

import java.util.Optional;
import java.util.function.Function;

@FunctionalInterface
public interface GuiEntryProvider<R, S, T extends SerializableType<S>> {
    AbstractConfigListEntry<?> apply(T type, ConfigLeaf<S> leaf, PropertyMirror<R> mirror, R defaultValue, Function<R, Optional<String>> suggestedErrorSupplier);
}
