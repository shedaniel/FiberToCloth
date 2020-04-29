package me.shedaniel.fiber2cloth.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import me.shedaniel.clothconfig2.api.*;
import me.shedaniel.clothconfig2.gui.entries.SubCategoryListEntry;
import me.shedaniel.fiber2cloth.api.ClothAttributes;
import me.shedaniel.fiber2cloth.api.Fiber2Cloth;
import me.zeroeightsix.fiber.api.constraint.Constraint;
import me.zeroeightsix.fiber.api.tree.ConfigBranch;
import me.zeroeightsix.fiber.api.tree.ConfigLeaf;
import me.zeroeightsix.fiber.api.tree.ConfigNode;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.Identifier;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class Fiber2ClothImpl implements Fiber2Cloth {
    
    private final String modId;
    private final Screen parentScreen;
    private String defaultCategory = "config.fiber2cloth.default.category";
    private String title;
    private final ConfigBranch node;
    private ConfigBranch defaultCategoryNode;
    private final Map<Class<?>, Function<ConfigLeaf<?>, AbstractConfigListEntry<?>>> functionMap = Maps.newHashMap();
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

    private static String[] splitLine(String s) {
        if (s == null)
            return null;
        return s.split("\n");
    }
    
    private static <T> List<T> list(T[] o) {
        if (o == null)
            return Lists.newArrayList();
        return Lists.newArrayList(o);
    }
    
    private static <T> Optional<String> error(List<Constraint<? super T>> constraints, Object value, Class<T> clazz) {
        try {
            T cast = clazz.cast(value);
            for(Constraint<? super T> constraint : constraints) {
                if (!constraint.test(cast))
                    return Optional.of(I18n.translate("error.fiber2cloth.invaild.value"));
            }
        } catch (ClassCastException e) {
            return Optional.of(I18n.translate("error.fiber2cloth.when.casting"));
        }
        return Optional.empty();
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
    public Fiber2Cloth registerNodeEntryFunction(Class<?> clazz, Function<ConfigLeaf<?>, AbstractConfigListEntry<?>> function) {
        functionMap.put(clazz, Objects.requireNonNull(function));
        return this;
    }
    
    @Override
    public Map<Class<?>, Function<ConfigLeaf<?>, AbstractConfigListEntry<?>>> getFunctionMap() {
        return functionMap;
    }

    private <T> void putFunction(Class<? super T> cls, Function<ConfigLeaf<T>, AbstractConfigListEntry<T>> function) {
        @SuppressWarnings("unchecked") Function<ConfigLeaf<?>, AbstractConfigListEntry<?>> f = (Function<ConfigLeaf<?>, AbstractConfigListEntry<?>>) (Function<?, ?>) function;
        functionMap.put(cls, f);
    }
    private <T> void putListFunction(Class<T[]> cls, Function<ConfigLeaf<T[]>, AbstractConfigListEntry<List<T>>> function) {
        @SuppressWarnings("unchecked") Function<ConfigLeaf<?>, AbstractConfigListEntry<?>> f = (Function<ConfigLeaf<?>, AbstractConfigListEntry<?>>) (Function<?, ?>) function;
        functionMap.put(cls, f);
    }

    private void initDefaultFunctionMap() {
        Function<ConfigLeaf<Integer>, AbstractConfigListEntry<Integer>> intFunc = configValue -> configEntryBuilder.startIntField("config." + modId + "." + configValue.getName(), configValue.getValue()).setDefaultValue(configValue.getDefaultValue()).setTooltip(splitLine(configValue.getComment())).setSaveConsumer(configValue::setValue).setErrorSupplier(var -> error(configValue.getConstraints(), var, Integer.class)).build();
        putFunction(Integer.class, intFunc);
        putFunction(int.class, intFunc);
        Function<ConfigLeaf<Long>, AbstractConfigListEntry<Long>> longFunc = configValue -> configEntryBuilder.startLongField("config." + modId + "." + configValue.getName(), configValue.getValue()).setDefaultValue(configValue.getDefaultValue()).setTooltip(splitLine(configValue.getComment())).setSaveConsumer(configValue::setValue).setErrorSupplier(var -> error(configValue.getConstraints(), var, Long.class)).build();
        putFunction(Long.class, longFunc);
        putFunction(long.class, longFunc);
        Function<ConfigLeaf<Double>, AbstractConfigListEntry<Double>> doubleFunc = configValue -> configEntryBuilder.startDoubleField("config." + modId + "." + configValue.getName(), configValue.getValue()).setDefaultValue(configValue.getDefaultValue()).setTooltip(splitLine(configValue.getComment())).setSaveConsumer(configValue::setValue).setErrorSupplier(var -> error(configValue.getConstraints(), var, Double.class)).build();
        putFunction(Double.class, doubleFunc);
        putFunction(double.class, doubleFunc);
        Function<ConfigLeaf<Float>, AbstractConfigListEntry<Float>> floatFunc = configValue -> configEntryBuilder.startFloatField("config." + modId + "." + configValue.getName(), configValue.getValue()).setDefaultValue(configValue.getDefaultValue()).setTooltip(splitLine(configValue.getComment())).setSaveConsumer(configValue::setValue).setErrorSupplier(var -> error(configValue.getConstraints(), var, Float.class)).build();
        putFunction(Float.class, floatFunc);
        putFunction(float.class, floatFunc);
        Function<ConfigLeaf<Boolean>, AbstractConfigListEntry<Boolean>> boolFunc = configValue -> {
            String s = "config." + modId + "." + configValue.getName();
            return configEntryBuilder.startBooleanToggle(s, configValue.getValue()).setDefaultValue(configValue.getDefaultValue()).setTooltip(splitLine(configValue.getComment())).setSaveConsumer(configValue::setValue).setErrorSupplier(var -> error(configValue.getConstraints(), var, Boolean.class)).setYesNoTextSupplier(bool -> {
                if (I18n.hasTranslation(s + ".boolean." + bool))
                    return I18n.translate(s + ".boolean." + bool);
                return bool ? "§aYes" : "§cNo";
            }).build();
        };
        putFunction(Boolean.class, boolFunc);
        putFunction(boolean.class, boolFunc);
        putFunction(String.class, configValue -> configEntryBuilder.startStrField("config." + modId + "." + configValue.getName(), configValue.getValue()).setDefaultValue(configValue.getDefaultValue()).setTooltip(splitLine(configValue.getComment())).setSaveConsumer(configValue::setValue).setErrorSupplier(var -> error(configValue.getConstraints(), var, String.class)).build());
        putListFunction(Integer[].class, configValue -> configEntryBuilder.startIntList("config." + modId + "." + configValue.getName(), Lists.newArrayList(configValue.getValue())).setDefaultValue(list(configValue.getDefaultValue())).setTooltip(splitLine(configValue.getComment())).setExpanded(true).setSaveConsumer(var -> configValue.setValue(var.toArray(new Integer[0]))).setErrorSupplier(var -> error(configValue.getConstraints(), var.toArray(new Integer[0]), Integer[].class)).build());
        putListFunction(Long[].class, configValue -> configEntryBuilder.startLongList("config." + modId + "." + configValue.getName(), Lists.newArrayList(configValue.getValue())).setDefaultValue(list(configValue.getDefaultValue())).setTooltip(splitLine(configValue.getComment())).setExpanded(true).setSaveConsumer(var -> configValue.setValue(var.toArray(new Long[0]))).setErrorSupplier(var -> error(configValue.getConstraints(), var.toArray(new Long[0]), Long[].class)).build());
        putListFunction(Double[].class, configValue -> configEntryBuilder.startDoubleList("config." + modId + "." + configValue.getName(), Lists.newArrayList(configValue.getValue())).setDefaultValue(list(configValue.getDefaultValue())).setTooltip(splitLine(configValue.getComment())).setExpanded(true).setSaveConsumer(var -> configValue.setValue(var.toArray(new Double[0]))).setErrorSupplier(var -> error(configValue.getConstraints(), var.toArray(new Double[0]), Double[].class)).build());
        putListFunction(Float[].class, configValue -> configEntryBuilder.startFloatList("config." + modId + "." + configValue.getName(), Lists.newArrayList(configValue.getValue())).setDefaultValue(list(configValue.getDefaultValue())).setTooltip(splitLine(configValue.getComment())).setExpanded(true).setSaveConsumer(var -> configValue.setValue(var.toArray(new Float[0]))).setErrorSupplier(var -> error(configValue.getConstraints(), var.toArray(new Float[0]), Float[].class)).build());
        putListFunction(String[].class, configValue -> configEntryBuilder.startStrList("config." + modId + "." + configValue.getName(), Lists.newArrayList(configValue.getValue())).setDefaultValue(list(configValue.getDefaultValue())).setTooltip(splitLine(configValue.getComment())).setExpanded(true).setSaveConsumer(var -> configValue.setValue(var.toArray(new String[0]))).setErrorSupplier(var -> error(configValue.getConstraints(), var.toArray(new String[0]), String[].class)).build());
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
            addNode(builder, getNode());
            getNode().getAttributeValue(ClothAttributes.BACKGROUND, Identifier.class).ifPresent(builder::setDefaultBackgroundTexture);
            String defaultS = defaultCategoryNode == node ? getDefaultCategoryKey() : "config." + modId + "." + defaultCategoryNode.getName();
            if (builder.hasCategory(defaultS)) {
                builder.setFallbackCategory(builder.getOrCreateCategory(defaultS));
            } else {
                try {
                    if (defaultCategoryNode != node)
                        throw new IllegalStateException("Illegal default config category!");
                } catch (IllegalStateException e) {
                    e.printStackTrace();
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
    
    private void addNode(ConfigBuilder builder, ConfigBranch configNode) {
        for (ConfigNode item : configNode.getItems()) {
            if (treeEntryMap.containsKey(item)) {
                appendToDefaultCategory(builder, item, treeEntryMap.get(item));
            } else if (item instanceof ConfigLeaf<?>) {
                ConfigLeaf<?> value = (ConfigLeaf<?>) item;
                appendToDefaultCategory(builder, value, functionMap.get(value.getType()));
            } else if (item instanceof ConfigBranch) {
                ConfigBranch configBranch = (ConfigBranch) item;
                ConfigCategory category = builder.getOrCreateCategory("config." + modId + "." + configBranch.getName());
                configBranch.getAttributeValue(ClothAttributes.CATEGORY_BACKGROUND, Identifier.class).ifPresent(category::setCategoryBackground);
                addNodeFirstLayer(builder, category, configBranch.getName(), configBranch);
            }
        }
    }

    private <T extends ConfigNode> void appendToDefaultCategory(ConfigBuilder builder, T value, Function<T, AbstractConfigListEntry<?>> factory) {
        if (factory != null) {
            AbstractConfigListEntry<?> entry = factory.apply(value);
            ConfigCategory defaultCategory = builder.getOrCreateCategory(getDefaultCategoryKey());
            defaultCategory.addEntry(entry);
            node.getAttributeValue(ClothAttributes.CATEGORY_BACKGROUND, Identifier.class).ifPresent(defaultCategory::setCategoryBackground);
        }
    }

    @SuppressWarnings("deprecation")
    private void addNodeFirstLayer(ConfigBuilder builder, ConfigCategory category, String categoryName, ConfigBranch configNode) {
        for (ConfigNode item : configNode.getItems()) {
            if (treeEntryMap.containsKey(item)) {
                if (treeEntryMap.get(item) != null) {
                    category.addEntry(treeEntryMap.get(item).apply(item));
                }
            } else if (item instanceof ConfigLeaf<?>) {
                ConfigLeaf<?> value = (ConfigLeaf<?>) item;
                Class<?> type = value.getType();
                if (functionMap.containsKey(type)) {
                    category.addEntry(functionMap.get(type).apply(value));
                }
            } else if (item instanceof ConfigBranch) {
                ConfigBranch nestedNode = (ConfigBranch) item;
                String s = "config." + modId + "." + categoryName + "." + nestedNode.getName();
                SubCategoryListEntry entry = null;
                for (Object o : category.getEntries()) {
                    if (o instanceof SubCategoryListEntry) {
                        if (((SubCategoryListEntry) o).getFieldName().equals(s)) {
                            entry = (SubCategoryListEntry) o;
                            break;
                        }
                    }
                }
                if (entry == null) {
                    entry = configEntryBuilder.startSubCategory(s, Lists.newArrayList()).setExpanded(true).setTooltip(splitLine(nestedNode.getComment())).build();
                    category.addEntry(entry);
                }
                addNodeSecondLayer(builder, entry, categoryName + "." + nestedNode.getName(), nestedNode);
            }
        }
    }
    
    private void addNodeSecondLayer(ConfigBuilder builder, SubCategoryListEntry subCategory, String categoryName, ConfigBranch nestedNode) {
        for(ConfigNode item : nestedNode.getItems()) {
            if (treeEntryMap.containsKey(item)) {
                appendToSubCategory(subCategory, item, treeEntryMap.get(item));
            } else if (item instanceof ConfigLeaf<?>) {
                ConfigLeaf<?> value = (ConfigLeaf<?>) item;
                appendToSubCategory(subCategory, value, functionMap.get(value.getType()));
            } else if (item instanceof ConfigBranch) {
                ConfigBranch branch = (ConfigBranch) item;
                SubCategoryListEntry entry = appendToSubCategory(subCategory, branch, nestedNestedNode -> {
                    String s = "config." + modId + "." + categoryName + "." + nestedNestedNode.getName();
                    for (AbstractConfigListEntry<?> o : subCategory.getValue()) {
                        if (o instanceof SubCategoryListEntry) {
                            if (o.getFieldName().equals(s)) {
                                return (SubCategoryListEntry) o;
                            }
                        }
                    }
                    return configEntryBuilder.startSubCategory(s, Lists.newArrayList()).setExpanded(true).setTooltip(splitLine(nestedNestedNode.getComment())).build();
                });
                addNodeSecondLayer(builder, entry, categoryName + "." + branch.getName(), branch);
            }
        }
    }

    private <T extends ConfigNode, E extends AbstractConfigListEntry<?>> E appendToSubCategory(SubCategoryListEntry subCategory, T value, Function<T, E> factory) {
        if (factory != null) {
            E entry = factory.apply(value);
            subCategory.getValue().add(entry);
            ((List<Element>) subCategory.children()).add(entry);
            return entry;
        }
        return null;
    }
}
