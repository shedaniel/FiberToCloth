package me.shedaniel.fiber2cloth.api;

import me.shedaniel.fiber2cloth.impl.FakeFiber2Cloth;
import me.zeroeightsix.fiber.tree.ConfigNode;
import me.zeroeightsix.fiber.tree.ConfigValue;
import me.zeroeightsix.fiber.tree.Node;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resource.language.I18n;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public interface Fiber2Cloth {
    
    static Fiber2Cloth create(Screen parentScreen, String modId, ConfigNode node, String title) {
        try {
            return (Fiber2Cloth) Class.forName("me.shedaniel.fiber2cloth.impl.Fiber2ClothImpl").getConstructor(Screen.class, String.class, ConfigNode.class, String.class).newInstance(parentScreen, modId, node, I18n.translate(Objects.requireNonNull(title)));
        } catch (InstantiationException | ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return new FakeFiber2Cloth(parentScreen, node, I18n.translate(Objects.requireNonNull(title)));
        }
    }
    
    Fiber2Cloth setAfterInitConsumer(Consumer<Screen> afterInitConsumer);
    
    Consumer<Screen> getAfterInitConsumer();
    
    Fiber2Cloth setSaveRunnable(Runnable saveRunnable);
    
    Fiber2Cloth registerNodeEntryFunction(Node node, Function function);
    
    default Fiber2Cloth hideNode(Node node) {
        registerNodeEntryFunction(node, null);
        return this;
    }
    
    Fiber2Cloth registerNodeEntryFunction(Class clazz, Function<ConfigValue, Object> function);
    
    Map<Class, Function> getFunctionMap();
    
    ConfigNode getNode();
    
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
