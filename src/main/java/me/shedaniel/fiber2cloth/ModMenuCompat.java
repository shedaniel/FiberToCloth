package me.shedaniel.fiber2cloth;

import io.github.prospector.modmenu.api.ConfigScreenFactory;
import io.github.prospector.modmenu.api.ModMenuApi;
import me.shedaniel.fiber2cloth.api.ClothAttributes;
import me.shedaniel.fiber2cloth.api.Fiber2Cloth;
import me.zeroeightsix.fiber.api.tree.ConfigBranch;
import me.zeroeightsix.fiber.api.tree.ConfigTree;
import net.minecraft.util.Identifier;

import java.util.concurrent.atomic.AtomicReference;

public class ModMenuCompat implements ModMenuApi {
    @Override
    public String getModId() {
        return "fiber2cloth";
    }
    
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return screen -> {
            AtomicReference<ConfigBranch> secondCategory = new AtomicReference<>();
            ConfigBranch cfg = ConfigTree.builder()
                    .withAttribute(ClothAttributes.background("minecraft:textures/block/oak_planks.png"))
                    .withAttribute(ClothAttributes.categoryBackground("minecraft:textures/block/stone.png"))
                    .beginValue("basicIntField", Integer.class, 100)
                    .withComment("This field will accept 0 - 100.")
                    .beginConstraints()
                    .atLeast(0).atMost(100)
                    .finishConstraints()
                    .finishValue()
                    .fork("second.category")
                    .withValue("nestedExample", String.class, "Hi")
                    .fork("i.am.inside")
                    .beginValue("nestedNestedExample", Boolean.class, false)
                    .withComment("I am inside lol wot u doing")
                    .finishValue()
                    .beginListValue("nestedNestedList", String.class, "hi", "no").finishValue()
                    .fork("lol")
                    .withValue("exampleBool", Boolean.class, false)
                    .finishBranch()
                    .finishBranch()
                    .finishBranch(secondCategory::set)
                    .build();
            return Fiber2Cloth.create(screen, getModId(), cfg, "Fiber2Cloth Example Config").setDefaultCategoryNode(secondCategory.get()).setSaveRunnable(() -> {
                // Here you should serialise the node into the config file.
            }).build().getScreen();
        };
    }
}
