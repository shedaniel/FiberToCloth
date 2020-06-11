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

import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.SerializableType;
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.ConfigLeaf;
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.PropertyMirror;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@FunctionalInterface
public interface GuiEntryProvider<R, S, T extends SerializableType<S>> {
    /**
     * Converts a config property to a gui entry.
     *
     * @param leaf                   the property to convert
     * @param type                   the property's {@link ConfigLeaf#getType() serializable type}
     * @param mirror                 a mirror that can be used to access the property's value
     * @param defaultValue           the leaf's {@link ConfigLeaf#getDefaultValue() default value}
     * @param suggestedErrorSupplier an error supplier that can be used by the generated config entry to check the validity of input values
     * @return entries allowing users to edit the property, or {@code null} to delegate to a more general provider
     */
    /*@Nullable*/
    List<AbstractConfigListEntry<?>> apply(ConfigLeaf<S> leaf, T type, PropertyMirror<R> mirror, R defaultValue, Function<R, Optional<Text>> suggestedErrorSupplier);
}
