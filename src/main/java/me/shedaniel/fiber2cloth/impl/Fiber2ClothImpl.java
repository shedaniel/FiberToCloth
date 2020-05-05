package me.shedaniel.fiber2cloth.impl;

import com.google.common.collect.Maps;
import io.github.fablabsmc.fablabs.api.fiber.v1.exception.FiberConversionException;
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.EnumSerializableType;
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.SerializableType;
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.derived.ConfigType;
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.derived.ConfigTypes;
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.derived.StringConfigType;
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.*;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.DropdownBoxEntry;
import me.shedaniel.clothconfig2.gui.entries.TooltipListEntry;
import me.shedaniel.clothconfig2.impl.builders.DropdownMenuBuilder;
import me.shedaniel.fiber2cloth.api.ClothAttributes;
import me.shedaniel.fiber2cloth.api.Fiber2Cloth;
import me.shedaniel.fiber2cloth.api.GuiEntryProvider;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class Fiber2ClothImpl implements Fiber2Cloth {

    public static final StringConfigType<Identifier> IDENTIFIER_TYPE = ConfigTypes.STRING
            .withPattern("(?>[a-z0-9_.-]+:)?[a-z0-9/._-]+")
            .derive(Identifier.class, Identifier::new, Identifier::toString);

    private final String modId;
    private final Screen parentScreen;
    private String defaultCategory = "config.fiber2cloth.default.category";
    private String title;
    private final ConfigBranch node;
    private ConfigBranch defaultCategoryNode;
    private final Map<Class<? extends SerializableType<?>>, Function<ConfigLeaf<?>, AbstractConfigListEntry<?>>> functionMap = Maps.newHashMap();
    private final Map<ConfigNode, Function<ConfigNode, AbstractConfigListEntry<?>>> treeEntryMap = Maps.newHashMap();
    private final ConfigEntryBuilder configEntryBuilder = ConfigEntryBuilder.create();
    private Runnable saveRunnable;
    private Consumer<Screen> afterInitConsumer = screen -> {};
    
    @Deprecated
    public Fiber2ClothImpl(Screen parentScreen, String modId, ConfigBranch node, String title) {
        this.parentScreen = parentScreen;
        this.node = node;
        this.defaultCategoryNode = node;
        this.modId = modId;
        this.title = title;
        initDefaultFunctionMap();
    }

    private static <S, T> Optional<String> error(ConfigType<T, S, ?> type, SerializableType<S> constraints, T value) {
        try {
            S v = type.toSerializedType(value);
            return error(constraints, v);
        } catch (FiberConversionException e) {
            return Optional.of(I18n.translate("error.fiber2cloth.when.casting", e.getMessage()));
        }
    }

    private static <S> Optional<String> error(SerializableType<S> constraints, S v) {
        if (!constraints.accepts(v)) {
            return Optional.of(I18n.translate("error.fiber2cloth.invalid.value", v, constraints));
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
    public Fiber2Cloth setDefaultCategoryNode(ConfigBranch defaultCategoryNode) {
        if (!node.getItems().contains(defaultCategoryNode))
            throw new IllegalArgumentException("The default category node must be on the first level!");
        this.defaultCategoryNode = Objects.requireNonNull(defaultCategoryNode);
        return this;
    }
    
    @Override
    public ConfigBranch getDefaultCategoryNode() {
        return defaultCategoryNode;
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
    public Fiber2Cloth registerTreeEntryFunction(ConfigNode node, Function<ConfigNode, AbstractConfigListEntry<?>> function) {
        treeEntryMap.put(node, function);
        return this;
    }

    @Override
    public Map<Class<? extends SerializableType<?>>, Function<ConfigLeaf<?>, AbstractConfigListEntry<?>>> getFunctionMap() {
        return functionMap;
    }

    @Override
    public <R, S, T extends SerializableType<S>> Fiber2Cloth registerLeafEntryFunction(ConfigType<R, S, T> type, GuiEntryProvider<R, S, T> function) {
        @SuppressWarnings("unchecked") Class<T> cls = (Class<T>) type.getSerializedType().getClass();
        Function<ConfigLeaf<?>, AbstractConfigListEntry<?>> f = leaf -> {
            if (type.getSerializedType().isAssignableFrom(leaf.getConfigType())) {
                PropertyMirror<R> mirror = PropertyMirror.create(type);
                mirror.mirror(leaf);
                T actualType = cls.cast(leaf.getConfigType());
                @SuppressWarnings("unchecked") ConfigLeaf<S> l = (ConfigLeaf<S>) leaf;
                return function.apply(l, actualType, mirror, type.toRuntimeType(l.getDefaultValue()), v -> error(type, actualType, v));
            }
            return null;
        };
        functionMap.merge(cls, f, (f1, f2) -> v -> {
            AbstractConfigListEntry<?> res = f2.apply(v);
            return res == null ? f1.apply(v) : res;
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
                long scaledCurrent = leaf.getValue().subtract(type.getMinimum()).divide(type.getIncrement(), BigDecimal.ROUND_DOWN).longValue();
                long scaledDefault = leaf.getDefaultValue().subtract(type.getMinimum()).divide(type.getIncrement(), BigDecimal.ROUND_DOWN).longValue();
                long scaledMax = max.subtract(type.getMinimum()).divide(type.getIncrement(), BigDecimal.ROUND_DOWN).longValue();
                return configEntryBuilder.startLongSlider(getFieldNameKey(leaf.getName()), scaledCurrent, 0, scaledMax)
                        .setDefaultValue(scaledDefault)
                        .setSaveConsumer(v -> leaf.setValue(BigDecimal.valueOf(v).multiply(step).add(min)))
                        .setErrorSupplier(v -> error(type, BigDecimal.valueOf(v).multiply(step).add(min)))
                        .setTextGetter(v -> {
                            BigDecimal val = BigDecimal.valueOf(v);
                            return I18n.translate("gui.fiber2cloth.slider.value", val.multiply(step).add(min).setScale(step.scale(), BigDecimal.ROUND_DOWN));
                        })
                        .build();
            } else {
                return configEntryBuilder
                        .startDoubleField(getFieldNameKey(leaf.getName()), mirror.getValue())
                        .setDefaultValue(defaultValue)
                        .setSaveConsumer(mirror::setValue)
                        .setErrorSupplier(errorSupplier)
                        .build();
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
                long scaledCurrent = leaf.getValue().subtract(type.getMinimum()).divide(type.getIncrement(), BigDecimal.ROUND_DOWN).longValue();
                long scaledDefault = leaf.getDefaultValue().subtract(type.getMinimum()).divide(type.getIncrement(), BigDecimal.ROUND_DOWN).longValue();
                long scaledMax = type.getMaximum().subtract(type.getMinimum()).divide(type.getIncrement(), BigDecimal.ROUND_DOWN).longValue();
                return configEntryBuilder.startLongSlider(getFieldNameKey(leaf.getName()), scaledCurrent, 0, scaledMax)
                        .setDefaultValue(scaledDefault)
                        .setSaveConsumer(v -> mirror.setValue(v * step + min))
                        .setErrorSupplier(v -> error(ConfigTypes.LONG, type, v * step + min))
                        .setTextGetter(v -> I18n.translate("gui.fiber2cloth.slider.value", v * step + min))
                        .build();
            } else {
                return configEntryBuilder.startLongField(getFieldNameKey(leaf.getName()), mirror.getValue())
                        .setDefaultValue(defaultValue)
                        .setSaveConsumer(mirror::setValue)
                        .setErrorSupplier(errorSupplier)
                        .setMin(min)
                        .setMax(max)
                        .build();
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
                ).orElse(null));
        registerLeafEntryFunction(ConfigTypes.BOOLEAN, (leaf, type, mirror, defaultValue, errorSupplier) -> {
            String s = getFieldNameKey(leaf.getName());
            return configEntryBuilder.startBooleanToggle(s, mirror.getValue())
                    .setDefaultValue(defaultValue)
                    .setSaveConsumer(mirror::setValue)
                    .setErrorSupplier(errorSupplier)
                    .setYesNoTextSupplier(bool -> {
                        if (I18n.hasTranslation(s + ".boolean." + bool))
                            return I18n.translate(s + ".boolean." + bool);
                        return bool ? "§aYes" : "§cNo";
                    }).build();
        });
        this.functionMap.put(EnumSerializableType.class, node -> {
            assert node.getConfigType().getPlatformType() == String.class;
            @SuppressWarnings("unchecked") ConfigLeaf<String> leaf = ((ConfigLeaf<String>) node);
            EnumSerializableType type = (EnumSerializableType) leaf.getConfigType();
            if (leaf.getAttributeValue(ClothAttributes.SUGGESTION_ENUM, ConfigTypes.BOOLEAN).orElse(false)) {
                return configEntryBuilder.startDropdownMenu(getFieldNameKey(leaf.getName()), leaf.getValue(), s -> type.accepts(s) ? s : null)
                        .setDefaultValue(leaf.getDefaultValue())
                        .setSaveConsumer(leaf::setValue)
                        .setSelections(type.getValidValues())
                        .build();
            } else {
                return configEntryBuilder.startSelector(getFieldNameKey(leaf.getName()), type.getValidValues().toArray(new String[0]), leaf.getValue())
                        .setDefaultValue(leaf.getDefaultValue())
                        .setSaveConsumer(leaf::setValue)
                        .setErrorSupplier(v -> error(type, v))
                        .build();
            }
        });
        registerLeafEntryFunction(ConfigTypes.STRING, (leaf, type, mirror, defaultValue, errorSupplier) -> configEntryBuilder
                .startStrField(getFieldNameKey(leaf.getName()), mirror.getValue())
                .setDefaultValue(defaultValue)
                .setSaveConsumer(mirror::setValue)
                .setErrorSupplier(errorSupplier).build());
        registerLeafEntryFunction(IDENTIFIER_TYPE, (leaf, type, mirror, defaultValue, suggestedErrorSupplier) ->
                leaf.getAttributeValue(ClothAttributes.REGISTRY_INPUT, IDENTIFIER_TYPE).map(Registry.REGISTRIES::get).map(registry -> {
                    DropdownBoxEntry.SelectionTopCellElement<Identifier> topCellElement;
                    if (registry == Registry.BLOCK) {
                        topCellElement = DropdownMenuBuilder.TopCellElementBuilder.ofBlockIdentifier(Registry.BLOCK.get(mirror.getValue()));
                    } else if (registry == Registry.ITEM) {
                        topCellElement = DropdownMenuBuilder.TopCellElementBuilder.ofItemIdentifier(Registry.ITEM.get(mirror.getValue()));
                    } else {
                        topCellElement = DropdownMenuBuilder.TopCellElementBuilder.of(mirror.getValue(), s -> Optional.ofNullable(Identifier.tryParse(s)).filter(registry::containsId).orElse(null));
                    }
                    return configEntryBuilder.startDropdownMenu(getFieldNameKey(leaf.getName()), topCellElement)
                            .setSelections(registry.getIds())
                            .setDefaultValue(defaultValue)
                            .setSaveConsumer(mirror::setValue)
                            .build();
                }).orElse(null));
        registerLeafEntryFunction(ConfigTypes.makeList(ConfigTypes.DOUBLE), (leaf, type, mirror, defaultValue, errorSupplier) -> configEntryBuilder
                .startDoubleList(getFieldNameKey(leaf.getName()), mirror.getValue())
                .setDefaultValue(defaultValue)
                .setExpanded(true)
                .setSaveConsumer(mirror::setValue)
                .setErrorSupplier(errorSupplier)
                .build());
        registerLeafEntryFunction(ConfigTypes.makeList(ConfigTypes.LONG), (leaf, type, mirror, defaultValue, errorSupplier) -> configEntryBuilder
                .startLongList(getFieldNameKey(leaf.getName()), mirror.getValue())
                .setDefaultValue(defaultValue)
                .setExpanded(true)
                .setSaveConsumer(mirror::setValue)
                .setErrorSupplier(errorSupplier)
                .build());
        registerLeafEntryFunction(ConfigTypes.makeList(ConfigTypes.INTEGER), (leaf, type, mirror, defaultValue, errorSupplier) -> configEntryBuilder
                .startIntList(getFieldNameKey(leaf.getName()), mirror.getValue())
                .setDefaultValue(defaultValue)
                .setExpanded(true)
                .setSaveConsumer(mirror::setValue)
                .setErrorSupplier(errorSupplier)
                .build());
        registerLeafEntryFunction(ConfigTypes.makeList(ConfigTypes.STRING), (leaf, type, mirror, defaultValue, errorSupplier) -> configEntryBuilder
                .startStrList(getFieldNameKey(leaf.getName()), mirror.getValue())
                .setDefaultValue(defaultValue)
                .setExpanded(true)
                .setSaveConsumer(mirror::setValue)
                .setErrorSupplier(errorSupplier)
                .build());
    }

    private String getFieldNameKey(String name) {
        return "config." + modId + "." + name;
    }

    @Override
    public ConfigBranch getNode() {
        return node;
    }
    
    @Override
    public Screen getParentScreen() {
        return parentScreen;
    }
    
    @Override
    public String getDefaultCategoryKey() {
        return defaultCategory;
    }
    
    @Override
    public String getTitle() {
        return title;
    }
    
    @Override
    public Fiber2Cloth setTitle(String title) {
        this.title = I18n.translate(Objects.requireNonNull(title));
        return null;
    }
    
    @Override
    public Fiber2Cloth setDefaultCategoryKey(String key) {
        this.defaultCategory = key;
        return this;
    }
    
    @Override
    public Result build() {
        try {
            ConfigBuilder builder = ConfigBuilder.create().setTitle(getTitle()).setParentScreen(getParentScreen());
            transformNode(builder, getNode());
            getNode().getAttributeValue(ClothAttributes.DEFAULT_BACKGROUND, IDENTIFIER_TYPE).ifPresent(builder::setDefaultBackgroundTexture);
            getNode().getAttributeValue(ClothAttributes.TRANSPARENT_BACKGROUND, ConfigTypes.BOOLEAN).ifPresent(builder::setTransparentBackground);
            String defaultS = defaultCategoryNode == node ? getDefaultCategoryKey() : getFieldNameKey(defaultCategoryNode.getName());
            if (builder.hasCategory(defaultS)) {
                builder.setFallbackCategory(builder.getOrCreateCategory(defaultS));
            } else {
                if (defaultCategoryNode != node) {
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
            if (treeEntryMap.containsKey(item)) {
                category = getOrCreateCategory(builder, getDefaultCategoryKey(), this.node);
                entries = appendEntries(new ArrayList<>(), item, treeEntryMap.get(item));
            } else if (item instanceof ConfigLeaf<?>) {
                ConfigLeaf<?> value = (ConfigLeaf<?>) item;
                category = getOrCreateCategory(builder, getDefaultCategoryKey(), this.node);
                entries = appendEntries(new ArrayList<>(), value, functionMap.get(value.getConfigType().getClass()));
            } else if (item instanceof ConfigBranch) {
                ConfigBranch branch = (ConfigBranch) item;
                if (branch.getAttributeValue(ClothAttributes.GROUP_DISPLAY, GroupDisplay.TYPE).orElse(GroupDisplay.DEFAULT).isCategoryCandidate()) {
                    String categoryKey = getFieldNameKey(branch.getName());
                    if (builder.hasCategory(categoryKey)) {
                        throw new IllegalStateException("Duplicate category "+ categoryKey);
                    }
                    category = getOrCreateCategory(builder, categoryKey, branch);
                    entries = transformNodeFirstLayer(branch.getName(), branch);
                } else {
                    category = getOrCreateCategory(builder, getDefaultCategoryKey(), this.node);
                    entries = appendSubCategory(null, new ArrayList<>(), branch);
                }
            } else {
                continue;
            }
            entries.forEach(category::addEntry);
        }
    }

    private ConfigCategory getOrCreateCategory(ConfigBuilder builder, String key, ConfigNode node) {
        ConfigCategory defaultCategory = builder.getOrCreateCategory(key);
        node.getAttributeValue(ClothAttributes.CATEGORY_BACKGROUND, IDENTIFIER_TYPE).ifPresent(defaultCategory::setCategoryBackground);
        return defaultCategory;
    }

    private List<AbstractConfigListEntry<?>> transformNodeFirstLayer(String categoryName, ConfigBranch configNode) {
        List<AbstractConfigListEntry<?>> category = new ArrayList<>(configNode.getItems().size());
        for (ConfigNode item : configNode.getItems()) {
            if (treeEntryMap.containsKey(item)) {
                appendEntries(category, configNode, treeEntryMap.get(item));
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
        for(ConfigNode item : nestedNode.getItems()) {
            if (treeEntryMap.containsKey(item)) {
                appendEntries(entries, nestedNode, treeEntryMap.get(item));
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
        GroupDisplay groupDisplay = nestedNode.getAttributeValue(ClothAttributes.GROUP_DISPLAY, GroupDisplay.TYPE).orElse(GroupDisplay.DEFAULT);
        if (groupDisplay.isTransitive()) {
            this.addPrefixText(entries, nestedNode, getFieldNameKey(subCategoryName));
            // no addAll because raw types
            transformNodeSecondLayer(subCategoryName, nestedNode).forEach(entries::add);
        } else {
            appendEntries(entries, nestedNode, n -> configEntryBuilder.startSubCategory(
                    getFieldNameKey(subCategoryName),
                    transformNodeSecondLayer(subCategoryName, n)
            ).setExpanded(groupDisplay == GroupDisplay.COLLAPSIBLE_EXPANDED).build());
        }
        return entries;
    }

    private <T extends ConfigNode> List<AbstractConfigListEntry<?>> appendEntries(List<AbstractConfigListEntry<?>> category, T value, Function<T, AbstractConfigListEntry<?>> factory) {
        if (factory != null && !value.getAttributeValue(ClothAttributes.EXCLUDED, ConfigTypes.BOOLEAN).orElse(false)) {
            AbstractConfigListEntry<?> entry = factory.apply(value);
            if (entry instanceof TooltipListEntry<?>) {
                Optional<String> rawTooltip = value.getAttributeValue(ClothAttributes.TOOLTIP, ConfigTypes.STRING);
                Optional<String[]> tooltip;
                if (rawTooltip.isPresent()) {
                    tooltip = rawTooltip
                            .map(key -> key.isEmpty() ? entry.getFieldName() + "@Tooltip" : key)
                            .map(Fiber2ClothImpl::gatherLocalizedLines)
                            .map(l -> l.toArray(new String[0]));
                } else {
                    String comment = value instanceof Commentable ? ((Commentable) value).getComment() : null;
                    tooltip = Optional.ofNullable(comment).map(s -> s.split("\n"));
                }
                ((TooltipListEntry<?>) entry).setTooltipSupplier(() -> tooltip);
            }
            this.addPrefixText(category, value, entry.getFieldName());
            value.getAttributeValue(ClothAttributes.REQUIRES_RESTART, ConfigTypes.BOOLEAN)
                    .ifPresent(entry::setRequiresRestart);
            category.add(entry);
        }
        return category;
    }

    private void addPrefixText(List<AbstractConfigListEntry<?>> entries, ConfigNode value, String baseTranslationKey) {
        value.getAttributeValue(ClothAttributes.PREFIX_TEXT, ConfigTypes.STRING)
                .map(key -> key.isEmpty() ? baseTranslationKey + "@PrefixText" : key)
                .map(Fiber2ClothImpl::gatherLocalizedLines)
                .map(l -> String.join("\n", l))
                .map(txt -> configEntryBuilder.startTextDescription(txt).build())
                .ifPresent(entries::add);
    }
}
