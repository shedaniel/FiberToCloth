package me.shedaniel.fiber2cloth.impl.annotation;

import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.AnnotatedSettings;
import me.shedaniel.fiber2cloth.api.ClothAttributes;
import me.shedaniel.fiber2cloth.api.ClothSetting;

public final class Fiber2ClothAnnotations {
    public static void configure(AnnotatedSettings in) {
        in.registerGroupProcessor(ClothSetting.CategoryBackground.class,
                (annotation, field, pojo, builder) -> builder.withAttribute(ClothAttributes.categoryBackground(annotation.value())));
        in.registerGroupProcessor(ClothSetting.Excluded.class,
                (annotation, field, pojo, builder) -> builder.withAttribute(ClothAttributes.excluded()));
        in.registerSettingProcessor(ClothSetting.Excluded.class,
                (annotation, field, pojo, builder) -> builder.withAttribute(ClothAttributes.excluded()));
        in.registerGroupProcessor(ClothSetting.TransitiveObject.class,
                (annotation, field, pojo, builder) -> builder.withAttribute(ClothAttributes.transitive()));
        in.registerGroupProcessor(ClothSetting.CollapsibleObject.class,
                (annotation, field, pojo, builder) -> builder.withAttribute(ClothAttributes.collapsible(annotation.startExpanded())));
    }
}
