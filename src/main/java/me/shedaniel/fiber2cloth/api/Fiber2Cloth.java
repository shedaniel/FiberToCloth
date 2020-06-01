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

import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.AnnotatedSettings;
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.SerializableType;
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.derived.ConfigType;
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.ConfigBranch;
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.ConfigLeaf;
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.ConfigNode;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.fiber2cloth.impl.FakeFiber2Cloth;
import me.shedaniel.fiber2cloth.impl.annotation.Fiber2ClothAnnotations;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resource.language.I18n;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public interface Fiber2Cloth {
    
    /**
     * Configures an {@link AnnotatedSettings} object to recognize Fiber2Cloth annotations.
     *
     * @see AnnotatedSettings.Builder#apply(Consumer)
     */
    static void configure(AnnotatedSettings.Builder settings) {
        Fiber2ClothAnnotations.configure(settings);
    }
    
    static Fiber2Cloth create(Screen parentScreen, String modId, ConfigBranch node, String title) {
        try {
            return (Fiber2Cloth) Class.forName("me.shedaniel.fiber2cloth.impl.Fiber2ClothImpl").getConstructor(Screen.class, String.class, ConfigBranch.class, String.class).newInstance(parentScreen, modId, node, I18n.translate(Objects.requireNonNull(title)));
        } catch (InstantiationException | ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return new FakeFiber2Cloth(parentScreen, node, I18n.translate(Objects.requireNonNull(title)));
        }
    }
    
    Consumer<Screen> getAfterInitConsumer();
    
    Fiber2Cloth setAfterInitConsumer(Consumer<Screen> afterInitConsumer);
    
    ConfigBranch getDefaultCategoryBranch();
    
    Fiber2Cloth setDefaultCategoryBranch(ConfigBranch defaultCategoryNode);
    
    Fiber2Cloth setSaveRunnable(Runnable saveRunnable);
    
    default Fiber2Cloth hideBranch(ConfigBranch node) {
        hideNode(node);
        return this;
    }
    
    default Fiber2Cloth hideNode(ConfigNode item) {
        registerNodeEntryFunction(item, null);
        return this;
    }
    
    Fiber2Cloth registerNodeEntryFunction(ConfigNode item, Function<ConfigNode, List<AbstractConfigListEntry<?>>> function);
    
    <R, S, T extends SerializableType<S>> Fiber2Cloth registerLeafEntryFunction(ConfigType<R, S, T> superType, GuiEntryProvider<R, S, T> function);
    
    Map<Class<? extends SerializableType<?>>, Function<ConfigLeaf<?>, List<AbstractConfigListEntry<?>>>> getFunctionMap();
    
    
    ConfigBranch getConfigRoot();
    
    Screen getParentScreen();
    
    String getDefaultCategoryKey();
    
    Fiber2Cloth setDefaultCategoryKey(String key);
    
    String getTitle();
    
    Fiber2Cloth setTitle(String title);
    
    Result build();
    
    interface Result {
        boolean isSuccessful();
        
        Screen getScreen();
    }
    
}
