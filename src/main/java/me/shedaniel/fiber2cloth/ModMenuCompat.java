package me.shedaniel.fiber2cloth;

import io.github.prospector.modmenu.api.ConfigScreenFactory;
import io.github.prospector.modmenu.api.ModMenuApi;
import me.shedaniel.fiber2cloth.api.ClothAttributes;
import me.shedaniel.fiber2cloth.api.ClothSetting;
import me.shedaniel.fiber2cloth.api.Fiber2Cloth;
import me.zeroeightsix.fiber.api.annotation.Setting;
import me.zeroeightsix.fiber.api.exception.FiberException;
import me.zeroeightsix.fiber.api.exception.RuntimeFiberException;
import me.zeroeightsix.fiber.api.tree.ConfigBranch;
import me.zeroeightsix.fiber.api.tree.ConfigTree;
import net.minecraft.util.Identifier;

import java.util.List;

public class ModMenuCompat implements ModMenuApi {
    @Override
    public String getModId() {
        return "fiber2cloth";
    }
    
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return screen -> {
            ConfigBranch cfg;
            try {
                cfg = buildConfigTree();
            } catch (FiberException e) {
                throw new RuntimeFiberException("Failed to load config", e);
            }
            ConfigBranch secondCategory = (ConfigBranch) cfg.lookup("second.category");
            return Fiber2Cloth.create(screen, getModId(), cfg, "Fiber2Cloth Example Config").setDefaultCategoryNode(secondCategory).setSaveRunnable(() -> {
                // Here you should serialise the node into the config file.
            }).build().getScreen();
        };
    }

    private ConfigBranch buildConfigTree() throws FiberException {
        ConfigBranch cfg;
        cfg = ConfigTree.builder()
                .withAttribute(ClothAttributes.defaultBackground("minecraft:textures/block/oak_planks.png"))
                .applyFromPojo(new Pojo())
                // adding a field directly to the root will cause it to be added to a "default" category
                .beginValue("basicIntField", Integer.class, 100)
                    .withComment("This field will accept 0 - 100.")
                    .beginConstraints()
                    .atLeast(0).atMost(100)
                    .finishConstraints()
                .finishValue()
                .fork("second.category")
                    .withAttribute(ClothAttributes.categoryBackground("minecraft:textures/block/stone.png"))
                    .withValue("nestedExample", String.class, "Hi")
                    .fork("i.am.inside")
                        .fork("i.am.inside.but.hidden")
                            .withAttribute(ClothAttributes.transitive())
                            .withValue("transitiveExample", Double[].class, new Double[0])
                        .finishBranch()
                        .beginValue("nestedNestedExample", Boolean.class, false)
                            .withComment("This comment is overridden by the tooltip")
                            .withAttribute(ClothAttributes.tooltip("config.fiber2cloth.nestedNestedExample.tooltip.", 2))
                        .finishValue()
                        .beginAggregateValue("nestedNestedList", new String[] {"hi", "no"})
                            .withAttribute(ClothAttributes.prefixText("config.fiber2cloth.nestedNestedList.description"))
                        .finishValue()
                        .fork("lol")
                            .withValue("exampleBool", Boolean.class, false)
                        .finishBranch()
                    .finishBranch()
                .finishBranch()
                .build();
        return cfg;
    }

    private static class Pojo {
        @Setting.Group
        @ClothSetting.CategoryBackground("minecraft:textures/block/bricks.png")
        public FirstCategory firstCategory = new FirstCategory();

        @Setting.Group
        public SecondCategory secondCategory = new SecondCategory();

        private static class FirstCategory {
            public String[] ids = new String[] {"minecraft:diamond", "fabric:bike_shed"};
        }

        private static class SecondCategory {
            @ClothSetting.EnumHandler(display = ClothSetting.EnumHandler.EnumDisplayOption.BUTTON)
            public Choice yes = Choice.NO;

            enum Choice {
                YES, NO, IDK
            }
        }
    }
}
