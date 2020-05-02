package me.shedaniel.fiber2cloth;

import blue.endless.jankson.Comment;
import io.github.prospector.modmenu.api.ConfigScreenFactory;
import io.github.prospector.modmenu.api.ModMenuApi;
import me.shedaniel.fiber2cloth.api.ClothAttributes;
import me.shedaniel.fiber2cloth.api.ClothSetting;
import me.shedaniel.fiber2cloth.api.Fiber2Cloth;
import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.AnnotatedSettings;
import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting;
import io.github.fablabsmc.fablabs.api.fiber.v1.exception.FiberException;
import io.github.fablabsmc.fablabs.api.fiber.v1.exception.RuntimeFiberException;
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.derived.ConfigType;
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.derived.ConfigTypes;
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.ConfigBranch;
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.ConfigTree;
import me.shedaniel.fiber2cloth.impl.annotation.Fiber2ClothAnnotations;
import net.minecraft.util.Identifier;

import java.util.Arrays;
import java.util.Collections;
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
                .applyFromPojo(new Pojo(), Fiber2Cloth.configure(AnnotatedSettings.create().registerTypeMapping(Pojo.SecondCategory.Choice.class, ConfigTypes.makeEnum(Pojo.SecondCategory.Choice.class))))
                .fork("second.category")
                    .withAttribute(ClothAttributes.categoryBackground("minecraft:textures/block/stone.png"))
                    .withValue("nestedExample", ConfigTypes.STRING, "Hi")
                    .fork("i.am.inside")
                        .fork("i.am.inside.but.hidden")
                            .withAttribute(ClothAttributes.transitive())
                            .withValue("transitiveExample", ConfigTypes.makeList(ConfigTypes.DOUBLE), Collections.emptyList())
                        .finishBranch()
                        .beginValue("nestedNestedExample", ConfigTypes.BOOLEAN, false)
                            .withComment("This comment is overridden by the tooltip")
                            .withAttribute(ClothAttributes.tooltip("config.fiber2cloth.nestedNestedExample.tooltip.", 2))
                        .finishValue()
                        .beginValue("nestedNestedList", ConfigTypes.makeList(ConfigTypes.STRING), Arrays.asList("hi", "no"))
                            .withAttribute(ClothAttributes.prefixText("config.fiber2cloth.nestedNestedList.description"))
                        .finishValue()
                        .fork("lol")
                            .withValue("exampleBool", ConfigTypes.BOOLEAN, false)
                        .finishBranch()
                    .finishBranch()
                .finishBranch()
                .build();
        return cfg;
    }

    private static class Pojo {

        // adding a field directly to the root will cause it to be added to a "default" category
        @Setting.Constrain.Range(min = 0, max = 100)
        @Comment("This field will accept 0 - 100.")
        public int basicIntField = 100;

        @Setting.Group
        @ClothSetting.CollapsibleObject
        public FirstCategory collapsibleCategory = new FirstCategory();

        @Setting.Group
        @ClothSetting.TransitiveObject
        public SecondCategory inlineCategory = new SecondCategory();

        @Setting.Group
        @ClothSetting.CategoryBackground("minecraft:textures/block/bricks.png")
        public FirstCategory firstPojoCategory = new FirstCategory();

        @Setting.Group
        public SecondCategory secondPojoCategory = new SecondCategory();

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
