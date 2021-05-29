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

package me.shedaniel.fiber2cloth;

import blue.endless.jankson.Comment;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.AnnotatedSettings;
import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting;
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.derived.ConfigTypes;
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.ConfigBranch;
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.ConfigTree;
import me.shedaniel.fiber2cloth.api.ClothAttributes;
import me.shedaniel.fiber2cloth.api.ClothSetting;
import me.shedaniel.fiber2cloth.api.DefaultTypes;
import me.shedaniel.fiber2cloth.api.Fiber2Cloth;
import net.minecraft.block.Blocks;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.Difficulty;

import java.util.Arrays;
import java.util.Collections;

public class ModMenuCompat implements ModMenuApi {
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return screen -> {
            ConfigBranch cfg = ConfigTree.builder()
                    .withAttribute(ClothAttributes.transparentBackground())
                    .withAttribute(ClothAttributes.defaultBackground(new Identifier("minecraft:textures/block/oak_planks.png")))
                    .applyFromPojo(new Pojo(),
                            AnnotatedSettings.builder()
                                    .registerTypeMapping(Identifier.class, DefaultTypes.IDENTIFIER_TYPE)
                                    .apply(Fiber2Cloth::configure)
                                    .build())
                    .fork("second.category")
                    .withAttribute(ClothAttributes.categoryBackground(new Identifier("minecraft:textures/block/stone.png")))
                    .withValue("nestedExample", ConfigTypes.STRING, "Hi")
                    .fork("i.am.inside")
                    .fork("i.am.inside.but.hidden")
                    .withAttribute(ClothAttributes.transitive())
                    .withAttribute(ClothAttributes.tooltip())
                    .withValue("transitiveExample", ConfigTypes.makeList(ConfigTypes.DOUBLE), Collections.emptyList())
                    .finishBranch()
                    .beginValue("nestedExample", ConfigTypes.BOOLEAN, false)
                    .withComment("This comment is overridden by the tooltip")
                    .withAttribute(ClothAttributes.tooltip("config.fiber2cloth.nestedExample.tooltip"))
                    .finishValue()
                    .beginValue("nestedList", ConfigTypes.makeList(ConfigTypes.STRING), Arrays.asList("hi", "no"))
                    .withAttribute(ClothAttributes.prefixText("config.fiber2cloth.nestedList.description"))
                    .finishValue()
                    .fork("lol")
                    .withValue("exampleBool", ConfigTypes.BOOLEAN, false)
                    .finishBranch()
                    .finishBranch()
                    .finishBranch()
                    .build();
            ConfigBranch secondCategory = (ConfigBranch) cfg.lookup("second.category");
            return Fiber2Cloth.create(screen, "fiber2cloth", cfg, "Fiber2Cloth Example Config").setDefaultCategoryBranch(secondCategory).setSaveRunnable(() -> {
                // Here you should serialise the node into the config file.
            }).build().getScreen();
        };
    }
    
    @SuppressWarnings("unused")
    private static class Pojo {
        
        // adding a field directly to the root will cause it to be added to a "default" category
        @Setting.Constrain.Range(min = 0, max = 100)
        @Setting(comment = "This field will accept 0 - 100.")
        public int basicIntField = 100;
        
        @ClothSetting.EnumHandler(ClothSetting.EnumHandler.EnumDisplayOption.DROPDOWN)
        public SecondCategory.Choice doYouLikeShulkers = SecondCategory.Choice.IDK;
        
        @Setting.Group
        @ClothSetting.PrefixText
        @ClothSetting.CollapsibleObject
        public FirstCategory collapsibleCategory = new FirstCategory();
        
        @Setting.Group
        @ClothSetting.PrefixText
        @ClothSetting.TransitiveObject
        public SecondCategory inlineCategory = new SecondCategory();
        
        @Setting.Group
        @ClothSetting.CategoryBackground("minecraft:textures/block/bricks.png")
        public FirstCategory firstPojoCategory = new FirstCategory();
        
        @Setting.Group
        public SecondCategory secondPojoCategory = new SecondCategory();
        
        private static class FirstCategory {
            public Identifier[] ids = new Identifier[]{
                    new Identifier("minecraft:diamond"),
                    new Identifier("fabric:bike_shed")
            };
            
            @Comment("Your favourite block in the game")
            @ClothSetting.RegistryInput("block")
            public Identifier favouriteBlock = Registry.BLOCK.getId(Blocks.COARSE_DIRT);
            
            @ClothSetting.ColorPicker(alpha = false)
            public int favouriteColor = 0xFF0000;
        }
        
        private static class SecondCategory {
            @ClothSetting.Slider
            @Setting.Constrain.Range(min = 0.1, max = 1, step = 0.1)
            public float percentage = 0.5f;
            
            @ClothSetting.RequiresRestart
            @ClothSetting.EnumHandler(ClothSetting.EnumHandler.EnumDisplayOption.SUGGESTION_INPUT)
            public Choice yes = Choice.NO;
            
            @ClothSetting.EnumHandler(ClothSetting.EnumHandler.EnumDisplayOption.BUTTON)
            public Difficulty difficulty = Difficulty.HARD;
            
            enum Choice {
                YES,
                NO,
                IDK
            }
        }
    }
}
