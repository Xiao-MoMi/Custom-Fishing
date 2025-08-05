# Custom-Fishing 🎣

![CodeFactor Grade](https://img.shields.io/codefactor/grade/github/Xiao-MoMi/Custom-Fishing)
<a href="https://mo-mi.gitbook.io/xiaomomi-plugins/plugin-wiki/customfishing" alt="GitBook">
<img src="https://img.shields.io/badge/docs-gitbook-brightgreen" alt="Gitbook"/>
</a>
[![Scc Count Badge](https://sloc.xyz/github/Xiao-MoMi/Custom-Fishing/?category=codes)](https://github.com/Xiao-MoMi/Custom-Fishing/)
![Code Size](https://img.shields.io/github/languages/code-size/Xiao-MoMi/Custom-Fishing)
![bStats Servers](https://img.shields.io/bstats/servers/16648)
![bStats Players](https://img.shields.io/bstats/players/16648)
![GitHub](https://img.shields.io/github/license/Xiao-MoMi/Custom-Fishing)

## 📌 About CustomFishing
CustomFishing is a **Paper plugin** designed to provide a variety of **fishing minigames** and a powerful **condition and action system**. It introduces a unique **weight system**, offering unparalleled customization while maintaining optimal performance.

### 🔥 Key Features:
- Extensive **customization** options for fishing mechanics.
- Ability to **register custom** mechanisms, actions, conditions, games, and configuration parsers.
- Supports **innovative fishing experiences**, such as **lava fishing** or **void fishing**.
- Provides a **robust API** for developers to extend functionality easily.

---
## 🔧 How to Build

### 💻 Command Line
1. Install **JDK 17 & 21**.
2. Open a terminal and navigate to the project directory.
3. Run:

   ```sh
   ./gradlew build
   ```
4. The artifact will be available in the **/target** folder.

### 🛠️ Using an IDE
1. Import the project into your preferred IDE.
2. Execute the **Gradle build** action.
3. Find the artifact in the **/target** folder.

---
## 🤝 How to Contribute

### 🌍 Translations
1. Clone this repository.
2. Create a new language file in:
   ```
   /core/src/main/resources/translations
   ```
3. Once done, submit a **pull request** for review. We appreciate your contributions!

---
## 💖 Support the Developer
If you enjoy using CustomFishing, consider supporting the developer!

- **Polymart**: [CustomFishing on Polymart](https://polymart.org/resource/customfishing.2723/)
- **BuiltByBit**: [CustomFishing on BuiltByBit](https://builtbybit.com/resources/customfishing.36361/)
- **Afdian**: [Support via Afdian](https://afdian.com/@xiaomomi/)

---
## 📚 CustomFishing API

### 📌 Repository
```kotlin
repositories {
    maven("https://repo.momirealms.net/releases/")
}
```

### 📌 Dependency
```kotlin
dependencies {
    compileOnly("net.momirealms:custom-fishing:2.3.7")
}
```

---
## 🎉 Fun Fact
I misspelled "mechanism" as "mechanic"—I should have caught that earlier! 😆

