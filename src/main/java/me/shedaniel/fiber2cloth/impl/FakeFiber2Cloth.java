package me.shedaniel.fiber2cloth.impl;

import com.google.common.collect.Maps;
import me.shedaniel.fiber2cloth.api.Fiber2Cloth;
import me.zeroeightsix.fiber.tree.ConfigNode;
import net.minecraft.client.gui.screen.Screen;

import java.util.Map;
import java.util.function.Function;

public class FakeFiber2Cloth implements Fiber2Cloth {
    
    private Screen parentScreen;
    private String title;
    private ConfigNode node;
    
    @Deprecated
    public FakeFiber2Cloth(Screen parentScreen, ConfigNode node, String title) {
        this.parentScreen = parentScreen;
        this.node = node;
        this.title = title;
    }
    
    @Override
    public Fiber2Cloth setSaveRunnable(Runnable saveRunnable) {
        return this;
    }
    
    @Override
    public Fiber2Cloth registerConfigEntryFunction(Class clazz, Function function) {
        return this;
    }
    
    @Override
    public Map<Class, Function> getFunctionMap() {
        return Maps.newHashMap();
    }
    
    @Override
    public ConfigNode getNode() {
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
