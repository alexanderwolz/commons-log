# Commons Log

![GitHub release (latest by date)](https://img.shields.io/github/v/release/alexanderwolz/commons-log)
![Maven Central Version](https://img.shields.io/maven-central/v/de.alexanderwolz/commons-log)
![GitHub](https://img.shields.io/github/license/alexanderwolz/commons-log)
![GitHub code size in bytes](https://img.shields.io/github/languages/code-size/alexanderwolz/commons-log)
![GitHub all releases](https://img.shields.io/github/downloads/alexanderwolz/commons-log/total?color=informational)

## 🧑‍💻 About

This repository provides common logging utilities.

## 🛠️ Build
1. Create jar resource using ```./gradlew clean build```
2. Copy  ```/build/libs/*.jar``` into your project
3. Use the log classes

## 📦 Getting the latest release

You can pull the latest binaries from the central Maven repositories:

with Gradle
```kotlin
implementation("de.alexanderwolz:commons-log:1.1.0")
```
with Maven
```xml
<dependency>
  <groupId>de.alexanderwolz</groupId>
  <artifactId>commons-log</artifactId>
    <version>1.1.0</version>
</dependency>
```

## 🪄 Example

```kotlin
val logger = Logger(javaClass)
logger.trace { "This is a trace log" }
```

- - -

Made with ❤️ in Bavaria
<br>
© 2025, <a href="https://www.alexanderwolz.de"> Alexander Wolz
