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

package me.shedaniel.fiber2cloth.impl;

import com.google.common.collect.Maps;
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.SerializableType;
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.derived.ConfigType;
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.ConfigBranch;
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.ConfigLeaf;
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.ConfigNode;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.fiber2cloth.api.Fiber2Cloth;
import me.shedaniel.fiber2cloth.api.GuiEntryProvider;
import net.minecraft.client.gui.screen.Screen;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class FakeFiber2Cloth implements Fiber2Cloth {
    
    private final Screen parentScreen;
    private final String title;
    private final ConfigBranch node;
    
    @Deprecated
    public FakeFiber2Cloth(Screen parentScreen, ConfigBranch node, String title) {
        this.parentScreen = parentScreen;
        this.node = node;
        this.title = title;
    }
    
    @Override
    public Fiber2Cloth setAfterInitConsumer(Consumer<Screen> afterInitConsumer) {
        return this;
    }
    
    @Override
    public Consumer<Screen> getAfterInitConsumer() {
        return screen -> {};
    }
    
    @Override
    public Fiber2Cloth setDefaultCategoryBranch(ConfigBranch defaultCategoryNode) {
        return this;
    }
    
    @Override
    public ConfigBranch getDefaultCategoryBranch() {
        return node;
    }
    
    @Override
    public Fiber2Cloth setSaveRunnable(Runnable saveRunnable) {
        return this;
    }
    
    @Override
    public Fiber2Cloth registerNodeEntryFunction(ConfigNode item, Function<ConfigNode, List<AbstractConfigListEntry<?>>> function) {
        return this;
    }
    
    @Override
    public <R, S, T extends SerializableType<S>> Fiber2Cloth registerLeafEntryFunction(ConfigType<R, S, T> type, GuiEntryProvider<R, S, T> function) {
        return this;
    }
    
    @Override
    public Map<Class<? extends SerializableType<?>>, Function<ConfigLeaf<?>, List<AbstractConfigListEntry<?>>>> getFunctionMap() {
        return Maps.newHashMap();
    }
    
    @Override
    public ConfigBranch getConfigRoot() {
        return node;
    }
    
    @Override
    public Screen getParentScreen() {
        return parentScreen;
    }
    
    @Override
    public String getDefaultCategoryKey() {
        return "config.fiber2cloth.default.category";
    }
    
    @Override
    public Fiber2Cloth setDefaultCategoryKey(String key) {
        return this;
    }
    
    @Override
    public String getTitle() {
        return title;
    }
    
    @Override
    public Fiber2Cloth setTitle(String title) {
        return this;
    }
    
    @Override
    public Result build() {
        return new Result() {
            @Override
            public boolean isSuccessful() {
                return false;
            }
            
            @Override
            public Screen getScreen() {
                return null;
            }
        };
    }
}
