package me.shedaniel.fiber2cloth.impl;

import com.google.common.collect.Maps;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.fiber2cloth.api.Fiber2Cloth;
import me.shedaniel.fiber2cloth.api.GuiEntryProvider;
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.SerializableType;
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.derived.ConfigType;
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.ConfigBranch;
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.ConfigLeaf;
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.ConfigNode;
import net.minecraft.client.gui.screen.Screen;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class FakeFiber2Cloth implements Fiber2Cloth {
    
    private Screen parentScreen;
    private String title;
    private ConfigBranch node;
    
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
    public Fiber2Cloth setDefaultCategoryNode(ConfigBranch defaultCategoryNode) {
        return this;
    }
    
    @Override
    public ConfigBranch getDefaultCategoryNode() {
        return node;
    }
    
    @Override
    public Fiber2Cloth setSaveRunnable(Runnable saveRunnable) {
        return this;
    }
    
    @Override
    public Fiber2Cloth registerTreeEntryFunction(ConfigNode item, Function function) {
        return this;
    }

    @Override
    public <R, S, T extends SerializableType<S>> Fiber2Cloth registerLeafEntryFunction(ConfigType<R, S, T> type, GuiEntryProvider<R, S, T> function) {
        return this;
    }

    @Override
    public Map<Class<? extends SerializableType<?>>, Function<ConfigLeaf<?>, AbstractConfigListEntry<?>>> getFunctionMap() {
        return Maps.newHashMap();
    }

    @Override
    public ConfigBranch getNode() {
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
