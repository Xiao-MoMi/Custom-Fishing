# Custom-Fishing

![CodeFactor Grade](https://img.shields.io/codefactor/grade/github/Xiao-MoMi/Custom-Fishing)
![bStats Servers](https://img.shields.io/bstats/servers/16648)
![bStats Players](https://img.shields.io/bstats/players/16648)
![GitHub](https://img.shields.io/github/license/Xiao-MoMi/Custom-Fishing)
[![](https://jitpack.io/v/Xiao-MoMi/Custom-Fishing.svg)](https://jitpack.io/#Xiao-MoMi/Custom-Fishing)
<a href="https://mo-mi.gitbook.io/xiaomomi-plugins/plugin-wiki/customfishing" alt="GitBook">
<img src="https://img.shields.io/badge/docs-gitbook-brightgreen" alt="Gitbook"/>
</a>

CustomFishing is a Paper plugin that provides minigames and a powerful condition & action library for fishing.
With the new concept of weight system, CustomFishing brings unlimited customization possibilities and best performance.

## How to build

### Windows

#### Command Line
Install JDK 17 and set the JDK installation path to JAVA_HOME as an environment variable.\
Start powershell and change directory to the project folder.\
Execute ".\gradlew build" and get the jar at /target/CustomFishing-plugin-version.jar.

#### IDE
Import the project and execute gradle build action.

##### About Proxy
If you are using a proxy, configurate the proxy in gradle.properties. Otherwise comment the lines in gradle.properties.

## Support the developer

Polymart: https://polymart.org/resource/customfishing.2723 \
Afdian: https://afdian.net/@xiaomomi

## Use CustomFishing API

### Maven

```
<repositories>
  <repository>
    <id>jitpack</id>
    <url>https://jitpack.io/</url>
  </repository>
</repositories>
```
```
<dependencies>
  <dependency>
    <groupId>com.github.Xiao-MoMi</groupId>
    <artifactId>Custom-Fishing</artifactId>
    <version>{LATEST}</version>
    <scope>provided</scope>
  </dependency>
</dependencies>
```
### Gradle (Groovy)

```
repositories {
    maven { url 'https://jitpack.io' }
}
```
```
dependencies {
    compileOnly 'com.github.Xiao-MoMi:Custom-Fishing:{LATEST}'
}
```
### Gradle (Kotlin)

```
repositories {
    maven("https://jitpack.io/")
}
```
```
dependencies {
    compileOnly("com.github.Xiao-MoMi:Custom-Fishing:{LATEST}")
}
```