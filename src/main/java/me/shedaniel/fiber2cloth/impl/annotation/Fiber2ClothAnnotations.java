package me.shedaniel.fiber2cloth.impl.annotation;

import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.AnnotatedSettings;
import me.shedaniel.fiber2cloth.api.ClothAttributes;
import me.shedaniel.fiber2cloth.api.ClothSetting;
import net.minecraft.util.Identifier;

public final class Fiber2ClothAnnotations {
    public static void configure(AnnotatedSettings in) {
        in.registerGroupProcessor(ClothSetting.CategoryBackground.class,
                (annotation, field, pojo, builder) -> builder.withAttribute(ClothAttributes.categoryBackground(annotation.value())));
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
        in.registerSettingProcessor(ClothSetting.SuggestionEnumInput.class,
                (annotation, field, pojo, builder) -> builder.withAttribute(ClothAttributes.suggestionEnum()));
        in.registerSettingProcessor(ClothSetting.RegistryInput.class,
                (annotation, field, pojo, builder) -> builder.withAttribute(ClothAttributes.registryInput(new Identifier(annotation.value()))));
        in.registerSettingProcessor(ClothSetting.Slider.class,
                (annotation, field, pojo, builder) -> builder.withAttribute(ClothAttributes.slider()));
        in.registerSettingProcessor(ClothSetting.RequiresRestart.class,
                (annotation, field, pojo, builder) -> builder.withAttribute(ClothAttributes.requiresRestart()));
    }
}
