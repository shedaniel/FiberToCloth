package me.shedaniel.fiber2cloth.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.SubCategoryListEntry;
import me.shedaniel.fiber2cloth.api.Fiber2Cloth;
import me.zeroeightsix.fiber.constraint.Constraint;
import me.zeroeightsix.fiber.tree.ConfigLeaf;
import me.zeroeightsix.fiber.tree.ConfigNode;
import me.zeroeightsix.fiber.tree.ConfigValue;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resource.language.I18n;

import java.util.*;
import java.util.function.Function;

public class Fiber2ClothImpl implements Fiber2Cloth {
    
    private final String modId;
    private Screen parentScreen;
    private String defaultCategory = "config.fiber2cloth.default.category";
    private String title;
    private ConfigNode node;
    private Map<Class, Function<ConfigValue, AbstractConfigListEntry>> functionMap = Maps.newHashMap();
    private ConfigEntryBuilder configEntryBuilder = ConfigEntryBuilder.create();
    private Runnable saveRunnable;
    
    @Deprecated
    public Fiber2ClothImpl(Screen parentScreen, String modId, ConfigNode node, String title) {
        this.parentScreen = parentScreen;
        this.node = node;
        this.modId = modId;
        this.title = title;
        initDefaultFunctionMap();
    }
    
    public static <T> T cast(Object o, Class<T> clazz) {
        if (o == null)
            return null;
        return clazz.cast(o);
    }
    
    public static String[] splitLine(String s) {
        if (s == null)
            return null;
        return s.split("\n");
    }
    
    public static <T> List<T> list(T[] o) {
        if (o == null)
            return Lists.newArrayList();
        return Lists.newArrayList(o);
    }
    
    public static <T> Optional<String> error(List<Constraint> constraints, Object value, Class<T> clazz) {
        try {
            T cast = clazz.cast(value);
            for(Constraint constraint : constraints) {
                if (!constraint.test(cast))
                    return Optional.of(I18n.translate("error.fiber2cloth.invaild.value"));
            }
        } catch (ClassCastException e) {
            return Optional.of(I18n.translate("error.fiber2cloth.when.casting"));
        }
        return Optional.empty();
    }
    
    @Override
    public Fiber2Cloth setSaveRunnable(Runnable saveRunnable) {
        this.saveRunnable = saveRunnable;
        return this;
    }
    
    @Override
    public Fiber2Cloth registerConfigEntryFunction(Class clazz, Function function) {
        functionMap.put(clazz, function);
        return this;
    }
    
    @Override
    public Map<Class, Function> getFunctionMap() {
        return (Map<Class, Function>) (Object) functionMap;
    }
    
    public void initDefaultFunctionMap() {
        functionMap.put(Integer.class, configValue -> {
            return configEntryBuilder.startIntField("config." + modId + "." + configValue.getName(), (int) configValue.getValue())
                    .setDefaultValue(cast(configValue.getDefaultValue(), Integer.class))
                    .setTooltip(splitLine(configValue.getComment()))
                    .setSaveConsumer(var -> configValue.setValue(var))
                    .setErrorSupplier(var -> error(configValue.getConstraints(), var, Integer.class))
                    .build();
        });
        functionMap.put(int.class, functionMap.get(Integer.class));
        functionMap.put(Long.class, configValue -> {
            return configEntryBuilder.startLongField("config." + modId + "." + configValue.getName(), (long) configValue.getValue())
                    .setDefaultValue(cast(configValue.getDefaultValue(), Long.class))
                    .setTooltip(splitLine(configValue.getComment()))
                    .setSaveConsumer(var -> configValue.setValue(var))
                    .setErrorSupplier(var -> error(configValue.getConstraints(), var, Long.class))
                    .build();
        });
        functionMap.put(long.class, functionMap.get(Long.class));
        functionMap.put(Double.class, configValue -> {
            return configEntryBuilder.startDoubleField("config." + modId + "." + configValue.getName(), (double) configValue.getValue())
                    .setDefaultValue(cast(configValue.getDefaultValue(), Double.class))
                    .setTooltip(splitLine(configValue.getComment()))
                    .setSaveConsumer(var -> configValue.setValue(var))
                    .setErrorSupplier(var -> error(configValue.getConstraints(), var, Double.class))
                    .build();
        });
        functionMap.put(double.class, functionMap.get(Double.class));
        functionMap.put(Float.class, configValue -> {
            return configEntryBuilder.startFloatField("config." + modId + "." + configValue.getName(), (float) configValue.getValue())
                    .setDefaultValue(cast(configValue.getDefaultValue(), Float.class))
                    .setTooltip(splitLine(configValue.getComment()))
                    .setSaveConsumer(var -> configValue.setValue(var))
                    .setErrorSupplier(var -> error(configValue.getConstraints(), var, Float.class))
                    .build();
        });
        functionMap.put(float.class, functionMap.get(Float.class));
        functionMap.put(Boolean.class, configValue -> {
            String s = "config." + modId + "." + configValue.getName();
            return configEntryBuilder.startBooleanToggle(s, (boolean) configValue.getValue())
                    .setDefaultValue(cast(configValue.getDefaultValue(), Boolean.class))
                    .setTooltip(splitLine(configValue.getComment()))
                    .setSaveConsumer(var -> configValue.setValue(var))
                    .setErrorSupplier(var -> error(configValue.getConstraints(), var, Boolean.class))
                    .setYesNoTextSupplier(bool -> {
                        if (I18n.hasTranslation(s + ".boolean." + bool))
                            return I18n.translate(s + ".boolean." + bool);
                        return bool ? "§aYes" : "§cNo";
                    })
                    .build();
        });
        functionMap.put(boolean.class, functionMap.get(Boolean.class));
        functionMap.put(String.class, configValue -> {
            return configEntryBuilder.startStrField("config." + modId + "." + configValue.getName(), (String) configValue.getValue())
                    .setDefaultValue(cast(configValue.getDefaultValue(), String.class))
                    .setTooltip(splitLine(configValue.getComment()))
                    .setSaveConsumer(var -> configValue.setValue(var))
                    .setErrorSupplier(var -> error(configValue.getConstraints(), var, String.class))
                    .build();
        });
        functionMap.put(Integer[].class, configValue -> {
            return configEntryBuilder.startIntList("config." + modId + "." + configValue.getName(), Lists.newArrayList((Integer[]) configValue.getValue()))
                    .setDefaultValue(list(cast(configValue.getDefaultValue(), Integer[].class)))
                    .setTooltip(splitLine(configValue.getComment()))
                    .setExpended(true)
                    .setSaveConsumer(var -> configValue.setValue(var.toArray(new Integer[0])))
                    .setErrorSupplier(var -> error(configValue.getConstraints(), var.toArray(new Integer[0]), Integer[].class))
                    .build();
        });
        functionMap.put(Long[].class, configValue -> {
            return configEntryBuilder.startLongList("config." + modId + "." + configValue.getName(), Lists.newArrayList((Long[]) configValue.getValue()))
                    .setDefaultValue(list(cast(configValue.getDefaultValue(), Long[].class)))
                    .setTooltip(splitLine(configValue.getComment()))
                    .setExpended(true)
                    .setSaveConsumer(var -> configValue.setValue(var.toArray(new Long[0])))
                    .setErrorSupplier(var -> error(configValue.getConstraints(), var.toArray(new Long[0]), Long[].class))
                    .build();
        });
        functionMap.put(Double[].class, configValue -> {
            return configEntryBuilder.startDoubleList("config." + modId + "." + configValue.getName(), Lists.newArrayList((Double[]) configValue.getValue()))
                    .setDefaultValue(list(cast(configValue.getDefaultValue(), Double[].class)))
                    .setTooltip(splitLine(configValue.getComment()))
                    .setExpended(true)
                    .setSaveConsumer(var -> configValue.setValue(var.toArray(new Double[0])))
                    .setErrorSupplier(var -> error(configValue.getConstraints(), var.toArray(new Double[0]), Double[].class))
                    .build();
        });
        functionMap.put(Float[].class, configValue -> {
            return configEntryBuilder.startFloatList("config." + modId + "." + configValue.getName(), Lists.newArrayList((Float[]) configValue.getValue()))
                    .setDefaultValue(list(cast(configValue.getDefaultValue(), Float[].class)))
                    .setTooltip(splitLine(configValue.getComment()))
                    .setExpended(true)
                    .setSaveConsumer(var -> configValue.setValue(var.toArray(new Float[0])))
                    .setErrorSupplier(var -> error(configValue.getConstraints(), var.toArray(new Float[0]), Float[].class))
                    .build();
        });
        functionMap.put(String[].class, configValue -> {
            return configEntryBuilder.startStrList("config." + modId + "." + configValue.getName(), Lists.newArrayList((String[]) configValue.getValue()))
                    .setDefaultValue(list(cast(configValue.getDefaultValue(), String[].class)))
                    .setTooltip(splitLine(configValue.getComment()))
                    .setExpended(true)
                    .setSaveConsumer(var -> configValue.setValue(var.toArray(new String[0])))
                    .setErrorSupplier(var -> error(configValue.getConstraints(), var.toArray(new String[0]), String[].class))
                    .build();
        });
    }
    
    @Override
    public ConfigNode getNode() {
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
            if (saveRunnable != null)
                builder.setSavingRunnable(saveRunnable);
            Screen screen = builder.build();
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
    
    public void addNode(ConfigBuilder builder, ConfigNode configNode) {
        configNode.getItems().stream().filter(item -> item instanceof ConfigValue).map(item -> (ConfigValue) item).sorted(Comparator.comparing(ConfigLeaf::getName)).forEach(value -> {
            Class type = value.getType();
            if (functionMap.containsKey(type)) {
                builder.getOrCreateCategory(getDefaultCategoryKey()).addEntry(functionMap.get(type).apply(value));
            }
        });
        configNode.getItems().stream().filter(item -> item instanceof ConfigNode).map(item -> (ConfigNode) item).sorted(Comparator.comparing(ConfigLeaf::getName)).forEach(node -> {
            addNodeFirstLayer(builder, builder.getOrCreateCategory("config." + modId + "." + node.getName()), node.getName(), node);
        });
    }
    
    @SuppressWarnings("deprecation")
    public void addNodeFirstLayer(ConfigBuilder builder, ConfigCategory category, String categoryName, ConfigNode configNode) {
        configNode.getItems().stream().filter(item -> item instanceof ConfigValue).map(item -> (ConfigValue) item).sorted(Comparator.comparing(ConfigLeaf::getName)).forEach(value -> {
            Class type = value.getType();
            if (functionMap.containsKey(type)) {
                category.addEntry(functionMap.get(type).apply(value));
            }
        });
        configNode.getItems().stream().filter(item -> item instanceof ConfigNode).map(item -> (ConfigNode) item).sorted(Comparator.comparing(ConfigLeaf::getName)).forEach(nestedNode -> {
            String s = "config." + modId + "." + categoryName + "." + nestedNode.getName();
            SubCategoryListEntry entry = null;
            for(Object o : category.getEntries()) {
                if (o instanceof SubCategoryListEntry) {
                    if (((SubCategoryListEntry) o).getFieldName().equals(s)) {
                        entry = (SubCategoryListEntry) o;
                        break;
                    }
                }
            }
            if (entry == null) {
                entry = configEntryBuilder.startSubCategory(s, Lists.newArrayList()).setExpended(true).setTooltip(splitLine(nestedNode.getComment())).build();
                category.addEntry(entry);
            }
            addNodeSecondLayer(builder, entry, categoryName + "." + nestedNode.getName(), nestedNode);
        });
    }
    
    public void addNodeSecondLayer(ConfigBuilder builder, SubCategoryListEntry subCategory, String categoryName, ConfigNode nestedNode) {
        nestedNode.getItems().stream().filter(item -> item instanceof ConfigValue).map(item -> (ConfigValue) item).sorted(Comparator.comparing(ConfigLeaf::getName)).forEach(value -> {
            Class type = value.getType();
            if (functionMap.containsKey(type)) {
                AbstractConfigListEntry entry = functionMap.get(type).apply(value);
                subCategory.getValue().add(entry);
                ((List) subCategory.children()).add(entry);
            }
        });
        nestedNode.getItems().stream().filter(item -> item instanceof ConfigNode).map(item -> (ConfigNode) item).sorted(Comparator.comparing(ConfigLeaf::getName)).forEach(nestedNestedNode -> {
            String s = "config." + modId + "." + categoryName + "." + nestedNestedNode.getName();
            SubCategoryListEntry entry = null;
            for(AbstractConfigListEntry o : subCategory.getValue()) {
                if (o instanceof SubCategoryListEntry) {
                    if (((SubCategoryListEntry) o).getFieldName().equals(s)) {
                        entry = (SubCategoryListEntry) o;
                        break;
                    }
                }
            }
            if (entry == null) {
                entry = configEntryBuilder.startSubCategory(s, Lists.newArrayList()).setExpended(true).setTooltip(splitLine(nestedNestedNode.getComment())).build();
                subCategory.getValue().add(entry);
                ((List) subCategory.children()).add(entry);
            }
            addNodeSecondLayer(builder, entry, categoryName + "." + nestedNestedNode.getName(), nestedNestedNode);
        });
    }
    
}
