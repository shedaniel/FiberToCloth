# Fiber2Cloth
A simple library that converts Fiber’s Config Node to Cloth Config Screen in 1 line.

Requires Cloth Config 2 v0.5.2 or above

**NOTE: This is still an experimental library, and there aren’t a lot of features. I will be using this library on my own mods and I will see how this goes.**

### Licensing
Fiber2Cloth and ClothConfig is unlicensed, no crediting is required.
Fiber is licensed under [Apache License 2.0](https://github.com/DaemonicLabs/fiber/blob/master/LICENSE). Make sure that you properly comply with their license.

### Maven
Latest Version of Fiber2Cloth: ![image](https://api.bintray.com/packages/shedaniel/fiber2cloth/fiber2cloth/images/download.svg)

Latest Version of ClothConfig 2: ![image](https://api.bintray.com/packages/shedaniel/cloth-config-2/config-2/images/download.svg)

Latest Version of Fiber: [Click Me](https://maven.fabricmc.net/me/zeroeightsix/fiber/maven-metadata.xml)

```groovy
dependencies {
    modApi ("me.shedaniel.cloth:config-2:ABC") {
    	transitive = false
    }
    modApi ("me.shedaniel.cloth:fiber2cloth:ABC") {
        transitive = false
    }
    modApi "me.zeroeightsix:fiber:ABC"
    include "me.shedaniel.cloth:config-2:ABC"
    include "me.shedaniel.cloth:fiber2cloth:ABC"
    include "me.zeroeightsix:fiber:ABC"
}
```
### Default Supported Types
- Integer
- Long
- Float
- Double
- Boolean
- String
- Integer[]
- Long[]
- Float[]
- Double[]
- String[]
### API Usage
This is the one line that will convert your config node into a cloth config screen.
```java
Fiber2Cloth.create(screen, modid, configNode, configScreenTitleKey).setSaveRunnable(() -> {
    // Here you should serialise the node into the config file.
}).build().getScreen();
```

A default category will be created (name is customisable) if there is a config value without a forked parent.

Multiple layers of forking is allowed, and sub categories will be used starting from the second layer.

### Using Unsupported Types
If you have an object type outside the default supported types, you can use `Fiber2Cloth#registerConfigEntryFunction`, it takes the class of the object type and a function that turns `ConfigValue` into `AbstractConfigListEntry`

You can see examples in `Fiber2ClothImpl#initDefaultFunctionMap`

### Changing the default `Yes / No` text for boolean toggles
Declare the yes / no text in lang files with key `config.{modid}.{valueName}.boolean.{true/false}`, and if the key is found translated, it will automatically switch to using that instead of the default.
##### Example
Fiber2Cloth's example lang has `config.fiber2cloth.exampleBool.boolean.true` set to `yo this is true`, which means that `config.fiber2cloth.exampleBool`'s Yes text is changed to `yo this is true` automatically.
