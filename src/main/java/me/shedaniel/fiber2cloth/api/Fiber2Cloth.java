package me.shedaniel.fiber2cloth.api;

import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.AnnotatedSettings;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.fiber2cloth.impl.FakeFiber2Cloth;
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.SerializableType;
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.derived.ConfigType;
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.ConfigBranch;
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.ConfigLeaf;
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.ConfigNode;
import me.shedaniel.fiber2cloth.impl.annotation.Fiber2ClothAnnotations;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resource.language.I18n;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public interface Fiber2Cloth {

    /**
     * Configures an {@link AnnotatedSettings} object to recognize Fiber2Cloth annotations.
     */
    static AnnotatedSettings configure(AnnotatedSettings settings) {
        Fiber2ClothAnnotations.configure(settings);
        return settings;
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
    
    ConfigBranch getDefaultCategoryNode();
    
    Fiber2Cloth setDefaultCategoryNode(ConfigBranch defaultCategoryNode);
    
    Fiber2Cloth setSaveRunnable(Runnable saveRunnable);

    default Fiber2Cloth hideNode(ConfigBranch node) {
        hideTreeEntry(node);
        return this;
    }
    
    default Fiber2Cloth hideTreeEntry(ConfigNode item) {
        registerTreeEntryFunction(item, null);
        return this;
    }
    
    Fiber2Cloth registerTreeEntryFunction(ConfigNode item, Function<ConfigNode, AbstractConfigListEntry<?>> function);

    <R, S, T extends SerializableType<S>> Fiber2Cloth registerLeafEntryFunction(ConfigType<R, S, T> superType, GuiEntryProvider<R, S, T> function);

    Map<Class<? extends SerializableType<?>>, Function<ConfigLeaf<?>, AbstractConfigListEntry<?>>> getFunctionMap();


    ConfigBranch getNode();
    
    Screen getParentScreen();
    
    String getDefaultCategoryKey();
    
    Fiber2Cloth setDefaultCategoryKey(String key);
    
    String getTitle();
    
    Fiber2Cloth setTitle(String title);
    
    Result build();
    
    public interface Result {
        boolean isSuccessful();
        
        Screen getScreen();
    }
    
}
