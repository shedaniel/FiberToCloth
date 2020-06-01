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

package me.shedaniel.fiber2cloth.impl.annotation;

import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.AnnotatedSettings;
import me.shedaniel.fiber2cloth.api.ClothAttributes;
import me.shedaniel.fiber2cloth.api.ClothSetting;
import net.minecraft.util.Identifier;

public final class Fiber2ClothAnnotations {
    public static void configure(AnnotatedSettings.Builder in) {
        in.registerGroupProcessor(ClothSetting.CategoryBackground.class,
                (annotation, field, pojo, builder) -> builder.withAttribute(ClothAttributes.categoryBackground(new Identifier(annotation.value()))));
        in.registerGroupProcessor(ClothSetting.Excluded.class,
                (annotation, field, pojo, builder) -> builder.withAttribute(ClothAttributes.excluded()));
        in.registerSettingProcessor(ClothSetting.Excluded.class,
                (annotation, field, pojo, builder) -> builder.withAttribute(ClothAttributes.excluded()));
        in.registerGroupProcessor(ClothSetting.PrefixText.class,
                (annotation, field, pojo, builder) -> builder.withAttribute(ClothAttributes.prefixText(annotation.value())));
        in.registerSettingProcessor(ClothSetting.PrefixText.class,
                (annotation, field, pojo, builder) -> builder.withAttribute(ClothAttributes.prefixText(annotation.value())));
        in.registerGroupProcessor(ClothSetting.Tooltip.class,
                (annotation, field, pojo, builder) -> builder.withAttribute(ClothAttributes.tooltip(annotation.value())));
        in.registerSettingProcessor(ClothSetting.Tooltip.class,
                (annotation, field, pojo, builder) -> builder.withAttribute(ClothAttributes.tooltip(annotation.value())));
        in.registerGroupProcessor(ClothSetting.TransitiveObject.class,
                (annotation, field, pojo, builder) -> builder.withAttribute(ClothAttributes.transitive()));
        in.registerGroupProcessor(ClothSetting.CollapsibleObject.class,
                (annotation, field, pojo, builder) -> builder.withAttribute(ClothAttributes.collapsible(annotation.startExpanded())));
        in.registerSettingProcessor(ClothSetting.EnumHandler.class,
                (annotation, field, pojo, builder) -> builder.withAttribute(ClothAttributes.enumDisplay(annotation.value())));
        in.registerSettingProcessor(ClothSetting.RegistryInput.class,
                (annotation, field, pojo, builder) -> builder.withAttribute(ClothAttributes.registryInput(new Identifier(annotation.value()))));
        in.registerSettingProcessor(ClothSetting.Slider.class,
                (annotation, field, pojo, builder) -> builder.withAttribute(ClothAttributes.slider()));
        in.registerSettingProcessor(ClothSetting.RequiresRestart.class,
                (annotation, field, pojo, builder) -> builder.withAttribute(ClothAttributes.requiresRestart()));
        in.registerSettingProcessor(ClothSetting.ColorPicker.class,
                (annotation, field, pojo, builder) -> builder.withAttribute(ClothAttributes.colorPicker(annotation.alpha())));
    }
}
