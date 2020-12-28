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

package me.shedaniel.fiber2cloth.impl;

import com.google.common.collect.Maps;
import io.github.fablabsmc.fablabs.api.fiber.v1.exception.FiberConversionException;
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.EnumSerializableType;
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.SerializableType;
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.derived.ConfigType;
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.derived.ConfigTypes;
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.derived.EnumConfigType;
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.*;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.DropdownBoxEntry;
import me.shedaniel.clothconfig2.gui.entries.TooltipListEntry;
import me.shedaniel.clothconfig2.impl.builders.DropdownMenuBuilder;
import me.shedaniel.fiber2cloth.api.*;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Fiber2ClothImpl implements Fiber2Cloth {
    
    public static final EnumConfigType<ClothSetting.EnumHandler.EnumDisplayOption> ENUM_DISPLAY_TYPE = ConfigTypes.makeEnum(ClothSetting.EnumHandler.EnumDisplayOption.class);
    
    private final String modId;
    private final Screen parentScreen;
    private Text defaultCategory = new TranslatableText("config.fiber2cloth.default.category");
    private Text title;
    private final ConfigBranch root;
    private ConfigBranch defaultCategoryBranch;
    private final Map<Class<? extends SerializableType<?>>, Function<ConfigLeaf<?>, List<AbstractConfigListEntry<?>>>> functionMap = Maps.newHashMap();
    private final Map<ConfigNode, Function<ConfigNode, List<AbstractConfigListEntry<?>>>> nodeEntryMap = Maps.newHashMap();
    private final ConfigEntryBuilder configEntryBuilder = ConfigEntryBuilder.create();
    private Runnable saveRunnable;
    private Consumer<Screen> afterInitConsumer = screen -> {};
    
    @Deprecated
    public Fiber2ClothImpl(Screen parentScreen, String modId, ConfigBranch root, Text title) {
        this.parentScreen = parentScreen;
        this.root = root;
        this.defaultCategoryBranch = root;
        this.modId = modId;
        this.title = title;
        initDefaultFunctionMap();
    }
    
    private static <S, T> Optional<Text> error(ConfigType<T, S, ?> type, SerializableType<S> constraints, T value) {
        try {
            S v = type.toSerializedType(value);
            return error(constraints, v);
        } catch (FiberConversionException e) {
            return Optional.of(new TranslatableText("error.fiber2cloth.when.casting", e.getMessage()));
        }
    }
    
    private static <S> Optional<Text> error(SerializableType<S> constraints, S v) {
        if (!constraints.accepts(v)) {
            return Optional.of(new TranslatableText("error.fiber2cloth.invalid.value", v, constraints));
        }
        return Optional.empty();
    }
    
    private static List<String> gatherLocalizedLines(String tt) {
        List<String> lines = new ArrayList<>();
        if (I18n.hasTranslation(tt)) {
            lines.add(I18n.translate(tt));
        }
        int i = 1;
        while (I18n.hasTranslation(tt + "[" + i + "]")) {
            lines.add(I18n.translate(tt + "[" + i + "]"));
            i++;
        }
        return lines;
    }
    
    @Override
    public Fiber2Cloth setDefaultCategoryBranch(ConfigBranch defaultCategoryNode) {
        if (!root.getItems().contains(defaultCategoryNode))
            throw new IllegalArgumentException("The default category node must be on the first level!");
        this.defaultCategoryBranch = Objects.requireNonNull(defaultCategoryNode);
        return this;
    }
    
    @Override
    public ConfigBranch getDefaultCategoryBranch() {
        return defaultCategoryBranch;
    }
    
    @Override
    public Fiber2Cloth setAfterInitConsumer(Consumer<Screen> afterInitConsumer) {
        this.afterInitConsumer = Objects.requireNonNull(afterInitConsumer);
        return this;
    }
    
    @Override
    public Consumer<Screen> getAfterInitConsumer() {
        return afterInitConsumer;
    }
    
    @Override
    public Fiber2Cloth setSaveRunnable(Runnable saveRunnable) {
        this.saveRunnable = saveRunnable;
        return this;
    }
    
    @Override
    public Fiber2Cloth registerNodeEntryFunction(ConfigNode node, Function<ConfigNode, List<AbstractConfigListEntry<?>>> function) {
        nodeEntryMap.put(node, function);
        return this;
    }
    
    @Override
    public Map<Class<? extends SerializableType<?>>, Function<ConfigLeaf<?>, List<AbstractConfigListEntry<?>>>> getFunctionMap() {
        return functionMap;
    }
    
    @Override
    public <R, S, T extends SerializableType<S>> Fiber2Cloth registerLeafEntryFunction(ConfigType<R, S, T> type, GuiEntryProvider<R, S, T> function) {
        @SuppressWarnings("unchecked") Class<T> cls = (Class<T>) type.getSerializedType().getClass();
        Function<ConfigLeaf<?>, List<AbstractConfigListEntry<?>>> f = leaf -> {
            if (type.getSerializedType().isAssignableFrom(leaf.getConfigType())) {
                PropertyMirror<R> mirror = PropertyMirror.create(type);
                mirror.mirror(leaf);
                T actualType = cls.cast(leaf.getConfigType());
                @SuppressWarnings("unchecked") ConfigLeaf<S> l = (ConfigLeaf<S>) leaf;
                List<AbstractConfigListEntry<?>> entries = function.apply(l, actualType, mirror, type.toRuntimeType(l.getDefaultValue()), v -> error(type, actualType, v));
                if (entries != null) {
                    return entries;
                }
            }
            return Collections.emptyList();
        };
        functionMap.merge(cls, f, (f1, f2) -> v -> {
            List<AbstractConfigListEntry<?>> res = f2.apply(v);
            return res == null || res.isEmpty() ? f1.apply(v) : res;
        });
        return this;
    }
    
    private void initDefaultFunctionMap() {
        registerLeafEntryFunction(ConfigTypes.DOUBLE, (leaf, type, mirror, defaultValue, errorSupplier) -> {
            if (leaf.getAttributeValue(ClothAttributes.SLIDER, ConfigTypes.BOOLEAN).orElse(false)) {
                if (type.getMinimum() == null || type.getMaximum() == null || type.getIncrement() == null) {
                    throw new IllegalStateException("Cannot build a slider without a minimum, a maximum, and a step (" + leaf + ")");
                }
                BigDecimal step = type.getIncrement();
                BigDecimal min = type.getMinimum();
                BigDecimal max = type.getMaximum();
                long scaledCurrent = leaf.getValue().subtract(type.getMinimum()).divide(type.getIncrement(), RoundingMode.FLOOR).longValue();
                long scaledDefault = leaf.getDefaultValue().subtract(type.getMinimum()).divide(type.getIncrement(), RoundingMode.FLOOR).longValue();
                long scaledMax = max.subtract(type.getMinimum()).divide(type.getIncrement(), RoundingMode.FLOOR).longValue();
                return Collections.singletonList(configEntryBuilder
                        .startLongSlider(getFieldNameKey(leaf.getName()), scaledCurrent, 0, scaledMax)
                        .setDefaultValue(scaledDefault)
                        .setSaveConsumer(v -> leaf.setValue(BigDecimal.valueOf(v).multiply(step).add(min)))
                        .setErrorSupplier(v -> error(type, BigDecimal.valueOf(v).multiply(step).add(min)))
                        .setTextGetter(v -> {
                            BigDecimal val = BigDecimal.valueOf(v);
                            return new TranslatableText("gui.fiber2cloth.slider.value", val.multiply(step).add(min).setScale(step.scale(), RoundingMode.FLOOR));
                        })
                        .build()
                );
            } else {
                return Collections.singletonList(configEntryBuilder
                        .startDoubleField(getFieldNameKey(leaf.getName()), mirror.getValue())
                        .setDefaultValue(defaultValue)
                        .setSaveConsumer(mirror::setValue)
                        .setErrorSupplier(errorSupplier)
                        .build()
                );
            }
        });
        registerLeafEntryFunction(ConfigTypes.LONG, (leaf, type, mirror, defaultValue, errorSupplier) -> {
            assert type.getMinimum() != null && type.getMaximum() != null && type.getIncrement() != null;
            long step;
            long min;
            long max;
            try {
                step = type.getIncrement().longValueExact();
                min = type.getMinimum().longValueExact();
                max = type.getMaximum().longValueExact();
            } catch (ArithmeticException e) {
                // FIXME remove this kludge when Fiber has fixed their decimal constraint checker
                return null;
            }
            if (leaf.getAttributeValue(ClothAttributes.SLIDER, ConfigTypes.BOOLEAN).orElse(false)) {
                long scaledCurrent = leaf.getValue().subtract(type.getMinimum()).divide(type.getIncrement(), RoundingMode.FLOOR).longValue();
                long scaledDefault = leaf.getDefaultValue().subtract(type.getMinimum()).divide(type.getIncrement(), RoundingMode.FLOOR).longValue();
                long scaledMax = type.getMaximum().subtract(type.getMinimum()).divide(type.getIncrement(), RoundingMode.FLOOR).longValue();
                return Collections.singletonList(configEntryBuilder
                        .startLongSlider(getFieldNameKey(leaf.getName()), scaledCurrent, 0, scaledMax)
                        .setDefaultValue(scaledDefault)
                        .setSaveConsumer(v -> mirror.setValue(v * step + min))
                        .setErrorSupplier(v -> error(ConfigTypes.LONG, type, v * step + min))
                        .setTextGetter(v -> new TranslatableText("gui.fiber2cloth.slider.value", v * step + min))
                        .build()
                );
            } else {
                return Collections.singletonList(configEntryBuilder
                        .startLongField(getFieldNameKey(leaf.getName()), mirror.getValue())
                        .setDefaultValue(defaultValue)
                        .setSaveConsumer(mirror::setValue)
                        .setErrorSupplier(errorSupplier)
                        .setMin(min)
                        .setMax(max)
                        .build()
                );
            }
        });
        registerLeafEntryFunction(ConfigTypes.INTEGER, (leaf, type, mirror, defaultValue, errorSupplier) ->
                leaf.getAttributeValue(ClothAttributes.COLOR_PICKER, ColorPickerFormat.TYPE).map(t -> configEntryBuilder
                        .startColorField(getFieldNameKey(leaf.getName()), mirror.getValue())
                        .setDefaultValue(defaultValue)
                        .setSaveConsumer(mirror::setValue)
                        .setErrorSupplier(errorSupplier)
                        .setAlphaMode(t == ColorPickerFormat.ARGB)
                        .build()
                ).map(Collections::<AbstractConfigListEntry<?>>singletonList).orElse(null)
        );
        registerLeafEntryFunction(ConfigTypes.BOOLEAN, (leaf, type, mirror, defaultValue, errorSupplier) -> {
            String s = "config." + modId + "." + leaf.getName();
            return Collections.singletonList(configEntryBuilder
                    .startBooleanToggle(getFieldNameKey(leaf.getName()), mirror.getValue())
                    .setDefaultValue(defaultValue)
                    .setSaveConsumer(mirror::setValue)
                    .setErrorSupplier(errorSupplier)
                    .setYesNoTextSupplier(bool -> {
                        if (I18n.hasTranslation(s + ".boolean." + bool))
                            return new TranslatableText(s + ".boolean." + bool);
                        return new LiteralText(bool ? "§aYes" : "§cNo");
                    }).build()
            );
        });
        this.functionMap.put(EnumSerializableType.class, node -> {
            assert node.getConfigType().getErasedPlatformType() == String.class;
            @SuppressWarnings("unchecked") ConfigLeaf<String> leaf = ((ConfigLeaf<String>) node);
            EnumSerializableType type = (EnumSerializableType) leaf.getConfigType();
            ClothSetting.EnumHandler.EnumDisplayOption displayOption = leaf.getAttributeValue(ClothAttributes.SUGGESTION_ENUM, ENUM_DISPLAY_TYPE).orElse(ClothSetting.EnumHandler.EnumDisplayOption.BUTTON);
            String key = "config." + modId + "." + leaf.getName();
            if (displayOption == ClothSetting.EnumHandler.EnumDisplayOption.BUTTON) {
                return Collections.singletonList(configEntryBuilder
                        .startSelector(getFieldNameKey(leaf.getName()), type.getValidValues().toArray(new String[0]), leaf.getValue())
                        .setDefaultValue(leaf.getDefaultValue())
                        .setSaveConsumer(leaf::setValue)
                        .setErrorSupplier(v -> error(type, v))
                        .setNameProvider((name) -> {
                            if (I18n.hasTranslation(key + ".enum." + name.toLowerCase(Locale.ROOT)))
                                return new TranslatableText(key + ".enum." + name.toLowerCase(Locale.ROOT));
                            return new LiteralText(name);
                        })
                        .build()
                );
            } else {
                return Collections.singletonList(configEntryBuilder
                        .startDropdownMenu(getFieldNameKey(leaf.getName()), leaf.getValue(), s -> type.accepts(s) ? s : null)
                        .setDefaultValue(leaf.getDefaultValue())
                        .setSaveConsumer(leaf::setValue)
                        .setSelections(type.getValidValues())
                        .setSuggestionMode(displayOption == ClothSetting.EnumHandler.EnumDisplayOption.SUGGESTION_INPUT)
                        .build()
                );
            }
        });
        registerLeafEntryFunction(ConfigTypes.STRING, (leaf, type, mirror, defaultValue, errorSupplier) ->
                Collections.singletonList(configEntryBuilder
                        .startStrField(getFieldNameKey(leaf.getName()), mirror.getValue())
                        .setDefaultValue(defaultValue)
                        .setSaveConsumer(mirror::setValue)
                        .setErrorSupplier(errorSupplier).build()
                ));
        registerLeafEntryFunction(DefaultTypes.IDENTIFIER_TYPE, (leaf, type, mirror, defaultValue, suggestedErrorSupplier) ->
                leaf.getAttributeValue(ClothAttributes.REGISTRY_INPUT, DefaultTypes.IDENTIFIER_TYPE).map(Registry.REGISTRIES::get).map(registry -> {
                    DropdownBoxEntry.SelectionTopCellElement<Identifier> topCellElement;
                    if (registry == Registry.BLOCK) {
                        topCellElement = DropdownMenuBuilder.TopCellElementBuilder.ofBlockIdentifier(Registry.BLOCK.get(mirror.getValue()));
                    } else if (registry == Registry.ITEM) {
                        topCellElement = DropdownMenuBuilder.TopCellElementBuilder.ofItemIdentifier(Registry.ITEM.get(mirror.getValue()));
                    } else {
                        //noinspection Convert2MethodRef
                        topCellElement = DropdownMenuBuilder.TopCellElementBuilder.of(mirror.getValue(), s -> Optional.ofNullable(Identifier.tryParse(s)).filter(identifier -> registry.containsId(identifier)).orElse(null));
                    }
                    return configEntryBuilder
                            .startDropdownMenu(getFieldNameKey(leaf.getName()), topCellElement)
                            .setSelections(registry.getIds())
                            .setDefaultValue(defaultValue)
                            .setSaveConsumer(mirror::setValue)
                            .build();
                }).map(Collections::<AbstractConfigListEntry<?>>singletonList).orElse(null)
        );
        registerLeafEntryFunction(ConfigTypes.makeList(ConfigTypes.DOUBLE), (leaf, type, mirror, defaultValue, errorSupplier) ->
                Collections.singletonList(configEntryBuilder.startDoubleList(getFieldNameKey(leaf.getName()), mirror.getValue())
                        .setDefaultValue(defaultValue)
                        .setExpanded(true)
                        .setSaveConsumer(mirror::setValue)
                        .setErrorSupplier(errorSupplier)
                        .build()
                ));
        registerLeafEntryFunction(ConfigTypes.makeList(ConfigTypes.LONG), (leaf, type, mirror, defaultValue, errorSupplier) ->
                Collections.singletonList(configEntryBuilder
                        .startLongList(getFieldNameKey(leaf.getName()), mirror.getValue())
                        .setDefaultValue(defaultValue)
                        .setExpanded(true)
                        .setSaveConsumer(mirror::setValue)
                        .setErrorSupplier(errorSupplier)
                        .build()
                ));
        registerLeafEntryFunction(ConfigTypes.makeList(ConfigTypes.INTEGER), (leaf, type, mirror, defaultValue, errorSupplier) ->
                Collections.singletonList(configEntryBuilder
                        .startIntList(getFieldNameKey(leaf.getName()), mirror.getValue())
                        .setDefaultValue(defaultValue)
                        .setExpanded(true)
                        .setSaveConsumer(mirror::setValue)
                        .setErrorSupplier(errorSupplier)
                        .build()
                ));
        registerLeafEntryFunction(ConfigTypes.makeList(ConfigTypes.STRING), (leaf, type, mirror, defaultValue, errorSupplier) ->
                Collections.singletonList(configEntryBuilder
                        .startStrList(getFieldNameKey(leaf.getName()), mirror.getValue())
                        .setDefaultValue(defaultValue)
                        .setExpanded(true)
                        .setSaveConsumer(mirror::setValue)
                        .setErrorSupplier(errorSupplier)
                        .build()
                ));
    }
    
    private Text getFieldNameKey(String name) {
        return new TranslatableText("config." + modId + "." + name);
    }
    
    @Override
    public ConfigBranch getConfigRoot() {
        return root;
    }
    
    @Override
    public Screen getParentScreen() {
        return parentScreen;
    }
    
    @Override
    public Text getDefaultCategory() {
        return defaultCategory;
    }
    
    @Override
    public Text getTitleText() {
        return title;
    }
    
    @Override
    public Fiber2Cloth setTitleText(Text title) {
        this.title = Objects.requireNonNull(title);
        return this;
    }
    
    @Override
    public Fiber2Cloth setDefaultCategory(Text key) {
        this.defaultCategory = key;
        return this;
    }
    
    @Override
    public Result build() {
        try {
            ConfigBuilder builder = ConfigBuilder.create().setTitle(getTitleText()).setParentScreen(getParentScreen());
            transformNode(builder, getConfigRoot());
            getConfigRoot().getAttributeValue(ClothAttributes.DEFAULT_BACKGROUND, DefaultTypes.IDENTIFIER_TYPE).ifPresent(builder::setDefaultBackgroundTexture);
            getConfigRoot().getAttributeValue(ClothAttributes.TRANSPARENT_BACKGROUND, ConfigTypes.BOOLEAN).ifPresent(builder::setTransparentBackground);
            Text defaultS = defaultCategoryBranch == root ? getDefaultCategory() : getFieldNameKey(defaultCategoryBranch.getName());
            if (builder.hasCategory(defaultS)) {
                builder.setFallbackCategory(builder.getOrCreateCategory(defaultS));
            } else {
                if (defaultCategoryBranch != root) {
                    new IllegalStateException("Illegal default config category!").printStackTrace();
                    return new Result() {
                        @Override
                        public boolean isSuccessful() {
                            return false;
                        }
                        
                        @Override
                        public Screen getScreen() {
                            return null;
                        }
                    };
                }
            }
            if (saveRunnable != null)
                builder.setSavingRunnable(saveRunnable);
            Screen screen = builder.setAfterInitConsumer(afterInitConsumer).build();
            return new Result() {
                @Override
                public boolean isSuccessful() {
                    return true;
                }
                
                @Override
                public Screen getScreen() {
                    return screen;
                }
            };
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return new Result() {
                @Override
                public boolean isSuccessful() {
                    return false;
                }
                
                @Override
                public Screen getScreen() {
                    return null;
                }
            };
        }
    }
    
    private void transformNode(ConfigBuilder builder, ConfigBranch configNode) {
        for (ConfigNode item : configNode.getItems()) {
            ConfigCategory category;
            List<AbstractConfigListEntry<?>> entries;
            if (nodeEntryMap.containsKey(item)) {
                category = getOrCreateCategory(builder, getDefaultCategory(), this.root);
                entries = appendEntries(new ArrayList<>(), item, nodeEntryMap.get(item));
            } else if (item instanceof ConfigLeaf<?>) {
                ConfigLeaf<?> value = (ConfigLeaf<?>) item;
                category = getOrCreateCategory(builder, getDefaultCategory(), this.root);
                entries = appendEntries(new ArrayList<>(), value, functionMap.get(value.getConfigType().getClass()));
            } else if (item instanceof ConfigBranch) {
                ConfigBranch branch = (ConfigBranch) item;
                if (branch.getAttributeValue(ClothAttributes.GROUP_DISPLAY, GroupDisplayOption.TYPE).orElse(GroupDisplayOption.DEFAULT).isCategoryCandidate()) {
                    Text categoryKey = getFieldNameKey(branch.getName());
                    if (builder.hasCategory(categoryKey)) {
                        throw new IllegalStateException("Duplicate category " + categoryKey);
                    }
                    category = getOrCreateCategory(builder, categoryKey, branch);
                    entries = transformNodeFirstLayer(branch.getName(), branch);
                } else {
                    category = getOrCreateCategory(builder, getDefaultCategory(), this.root);
                    entries = appendSubCategory(null, new ArrayList<>(), branch);
                }
            } else {
                continue;
            }
            entries.forEach(category::addEntry);
        }
    }
    
    private ConfigCategory getOrCreateCategory(ConfigBuilder builder, Text key, ConfigNode node) {
        ConfigCategory defaultCategory = builder.getOrCreateCategory(key);
        node.getAttributeValue(ClothAttributes.CATEGORY_BACKGROUND, DefaultTypes.IDENTIFIER_TYPE).ifPresent(defaultCategory::setCategoryBackground);
        return defaultCategory;
    }
    
    private List<AbstractConfigListEntry<?>> transformNodeFirstLayer(String categoryName, ConfigBranch configNode) {
        List<AbstractConfigListEntry<?>> category = new ArrayList<>(configNode.getItems().size());
        for (ConfigNode item : configNode.getItems()) {
            if (nodeEntryMap.containsKey(item)) {
                appendEntries(category, configNode, nodeEntryMap.get(item));
            } else if (item instanceof ConfigLeaf<?>) {
                ConfigLeaf<?> value = (ConfigLeaf<?>) item;
                appendEntries(category, value, functionMap.get(value.getConfigType().getClass()));
            } else if (item instanceof ConfigBranch) {
                ConfigBranch branch = (ConfigBranch) item;
                appendSubCategory(categoryName, category, branch);
            }
        }
        return category;
    }
    
    @SuppressWarnings("rawtypes")
    private List<AbstractConfigListEntry> transformNodeSecondLayer(String categoryName, ConfigBranch nestedNode) {
        List<AbstractConfigListEntry<?>> entries = new ArrayList<>(nestedNode.getItems().size());
        for (ConfigNode item : nestedNode.getItems()) {
            if (nodeEntryMap.containsKey(item)) {
                appendEntries(entries, nestedNode, nodeEntryMap.get(item));
            } else if (item instanceof ConfigLeaf<?>) {
                ConfigLeaf<?> value = (ConfigLeaf<?>) item;
                appendEntries(entries, value, functionMap.get(value.getConfigType().getClass()));
            } else if (item instanceof ConfigBranch) {
                ConfigBranch branch = (ConfigBranch) item;
                appendSubCategory(categoryName, entries, branch);
            }
        }
        @SuppressWarnings("unchecked") List<AbstractConfigListEntry> ret = (List<AbstractConfigListEntry>) (List<?>) entries;
        return ret;
    }
    
    private List<AbstractConfigListEntry<?>> appendSubCategory(String categoryName, List<AbstractConfigListEntry<?>> entries, ConfigBranch nestedNode) {
        String subCategoryName = (categoryName == null ? "" : (categoryName + ".")) + nestedNode.getName();
        GroupDisplayOption groupDisplayOption = nestedNode.getAttributeValue(ClothAttributes.GROUP_DISPLAY, GroupDisplayOption.TYPE).orElse(GroupDisplayOption.DEFAULT);
        if (groupDisplayOption.isTransitive()) {
            this.addPrefixText(entries, nestedNode, getFieldNameKey(subCategoryName));
            // no addAll because raw types
            transformNodeSecondLayer(subCategoryName, nestedNode).forEach(entries::add);
        } else {
            appendEntries(entries, nestedNode, n -> Collections.singletonList(configEntryBuilder.startSubCategory(
                    getFieldNameKey(subCategoryName),
                    transformNodeSecondLayer(subCategoryName, n)
            ).setExpanded(groupDisplayOption == GroupDisplayOption.COLLAPSIBLE_EXPANDED).build()));
        }
        return entries;
    }
    
    private <T extends ConfigNode> List<AbstractConfigListEntry<?>> appendEntries(List<AbstractConfigListEntry<?>> category, T value, Function<T, List<AbstractConfigListEntry<?>>> factory) {
        if (factory != null && !value.getAttributeValue(ClothAttributes.EXCLUDED, ConfigTypes.BOOLEAN).orElse(false)) {
            List<AbstractConfigListEntry<?>> entries = factory.apply(value);
            for (AbstractConfigListEntry<?> entry : entries) {
                if (entry instanceof TooltipListEntry<?>) {
                    Optional<String> rawTooltip = value.getAttributeValue(ClothAttributes.TOOLTIP, ConfigTypes.STRING);
                    Optional<Text[]> tooltip;
                    if (rawTooltip.isPresent()) {
                        tooltip = rawTooltip
                                .map(key -> key.isEmpty() ? entry.getFieldName().getString() + "@Tooltip" : key)
                                .map(Fiber2ClothImpl::gatherLocalizedLines)
                                .map(strings -> strings.stream().map(LiteralText::new).collect(Collectors.toList()))
                                .map(l -> l.toArray(new Text[0]));
                    } else {
                        String comment = value instanceof Commentable ? ((Commentable) value).getComment() : null;
                        tooltip = Optional.ofNullable(comment).map(s -> s.split("\n")).map(Arrays::asList)
                                .map(strings -> strings.stream().map(LiteralText::new).collect(Collectors.toList()))
                                .map(l -> l.toArray(new Text[0]));
                    }
                    ((TooltipListEntry<?>) entry).setTooltipSupplier(() -> tooltip);
                }
                this.addPrefixText(category, value, entry.getFieldName());
                value.getAttributeValue(ClothAttributes.REQUIRES_RESTART, ConfigTypes.BOOLEAN)
                        .ifPresent(entry::setRequiresRestart);
                category.add(entry);
            }
        }
        return category;
    }
    
    private void addPrefixText(List<AbstractConfigListEntry<?>> entries, ConfigNode value, Text baseTranslationKey) {
        value.getAttributeValue(ClothAttributes.PREFIX_TEXT, ConfigTypes.STRING)
                .map(key -> key.isEmpty() ? (baseTranslationKey instanceof TranslatableText ? ((TranslatableText) baseTranslationKey).getKey() : baseTranslationKey.getString()) + "@PrefixText" : key)
                .map(Fiber2ClothImpl::gatherLocalizedLines)
                .map(l -> String.join("\n", l))
                .map(LiteralText::new)
                .map(txt -> configEntryBuilder.startTextDescription(txt).build())
                .ifPresent(entries::add);
    }
}
