# Custom-Fishing

![CodeFactor Grade](https://img.shields.io/codefactor/grade/github/Xiao-MoMi/Custom-Fishing)
[![](https://jitpack.io/v/Xiao-MoMi/Custom-Fishing.svg)](https://jitpack.io/#Xiao-MoMi/Custom-Fishing)
<a href="https://mo-mi.gitbook.io/xiaomomi-plugins/plugin-wiki/customfishing" alt="GitBook">
<img src="https://img.shields.io/badge/docs-gitbook-brightgreen" alt="Gitbook"/>
</a>
[![Scc Count Badge](https://sloc.xyz/github/Xiao-MoMi/Custom-Fishing/?category=codes)](https://github.com/Xiao-MoMi/Custom-Fishing/)
![Code Size](https://img.shields.io/github/languages/code-size/Xiao-MoMi/Custom-Fishing)
![bStats Servers](https://img.shields.io/bstats/servers/16648)
![bStats Players](https://img.shields.io/bstats/players/16648)
![GitHub](https://img.shields.io/github/license/Xiao-MoMi/Custom-Fishing)

CustomFishing is a Paper plugin designed to offer an extensive range of minigames and a robust condition and action system for fishing. Introducing a novel weight system concept, CustomFishing provides unparalleled customization opportunities while ensuring optimal performance. The plugin goes beyond standard features by allowing you to register custom mechanism, actions, conditions, games, and even parsers for configuration file formats. This flexibility makes it a powerful API for developers, enabling the creation of innovative fishing experiences, such as lava fishing or void fishing, tailored to your specific needs.
## How to Build

#### Command Line
Install JDK 17 & 21. \
Start terminal and change directory to the project folder.\
Execute ".\gradlew build" and get the artifact under /target folder

#### IDE
Import the project and execute gradle build action. \
Get the artifact under /target folder

## How to Contribute

#### Translations
Clone this project and create a new language file in the /common/src/main/resources/translations directory. \
Once your changes are ready, open a pull request for review. We appreciate your works!

## Support the Developer

Polymart: https://polymart.org/resource/customfishing.2723/ \
BuiltByBit: https://builtbybit.com/resources/customfishing.36361/ \
Afdian: https://afdian.com/@xiaomomi/

## CustomFishing API

### Maven

```html
<repositories>
  <repository>
    <id>jitpack</id>
    <url>https://jitpack.io/</url>
  </repository>
</repositories>
```
```html
<dependencies>
  <dependency>
    <groupId>com.github.Xiao-MoMi</groupId>
    <artifactId>Custom-Fishing</artifactId>
    <version>{VERSION}</version>
    <scope>provided</scope>
  </dependency>
</dependencies>
```
### Gradle (Groovy)

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}
```
```groovy
dependencies {
    compileOnly 'com.github.Xiao-MoMi:Custom-Fishing:{VERSION}'
}
```
### Gradle (Kotlin)

```kotlin
repositories {
    maven("https://jitpack.io/")
}
```
```kotlin
dependencies {
    compileOnly("com.github.Xiao-MoMi:Custom-Fishing:{VERSION}")
}
```
#### Fun Facts
I misspelled mechanism as mechanic. I should have realized this earlier XD