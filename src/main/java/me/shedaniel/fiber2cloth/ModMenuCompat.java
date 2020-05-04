package me.shedaniel.fiber2cloth;

import blue.endless.jankson.Comment;
import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.AnnotatedSettings;
import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting;
import io.github.fablabsmc.fablabs.api.fiber.v1.exception.FiberException;
import io.github.fablabsmc.fablabs.api.fiber.v1.exception.RuntimeFiberException;
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.derived.ConfigTypes;
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.ConfigBranch;
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.ConfigTree;
import io.github.prospector.modmenu.api.ConfigScreenFactory;
import io.github.prospector.modmenu.api.ModMenuApi;
import me.shedaniel.fiber2cloth.api.ClothAttributes;
import me.shedaniel.fiber2cloth.api.ClothSetting;
import me.shedaniel.fiber2cloth.api.Fiber2Cloth;
import me.shedaniel.fiber2cloth.impl.Fiber2ClothImpl;
import net.minecraft.block.Blocks;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.Arrays;
import java.util.Collections;

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
                .applyFromPojo(new Pojo(), Fiber2Cloth.configure(
                        AnnotatedSettings.create()
                        .registerTypeMapping(Pojo.SecondCategory.Choice.class, ConfigTypes.makeEnum(Pojo.SecondCategory.Choice.class))
                        .registerTypeMapping(Identifier.class, Fiber2ClothImpl.IDENTIFIER_TYPE)
                ))
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
            public Identifier[] ids = new Identifier[] {
                    new Identifier("minecraft:diamond"),
                    new Identifier("fabric:bike_shed")
            };

            @Comment("Your favourite block in the game")
            @ClothSetting.RegistryInput("block")
            public Identifier favouriteBlock = Registry.BLOCK.getId(Blocks.COARSE_DIRT);
        }

        private static class SecondCategory {
            @ClothSetting.Slider
            @Setting.Constrain.Range(min = 0.1, max = 1, step = 0.1)
            public float percentage = 0.5f;

            @ClothSetting.SuggestionEnumInput
            public Choice yes = Choice.NO;

            enum Choice {
                YES, NO, IDK
            }
        }
    }
}
