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
implementation("de.alexanderwolz:commons-log:1.4.0")
```
with Maven
```xml
<dependency>
  <groupId>de.alexanderwolz</groupId>
  <artifactId>commons-log</artifactId>
    <version>1.4.0</version>
</dependency>
```

## 🪄 Example

```kotlin
val logger = Logger(javaClass) // or logger<MyService>()
logger.trace { "This is a trace log" }
logger.warn(exception) { "Something went wrong" }
```

### Event listeners

Listeners receive log events independently of the configured SLF4J log level,
including the caller, the throwable, a timestamp and the thread name.

```kotlin
val logger = logger<MyService> { event ->
    println("${event.timestamp} [${event.threadName}] ${event.level} ${event.caller}: ${event.message}")
}

// only receive warnings and errors
logger.addEventListener(Level.WARN) { event -> alerting.send(event.message, event.error) }
```

Exceptions thrown by a listener are caught and logged, they neither prevent the log output nor other listeners.

- - -

Made with ❤️ in Bavaria
<br>
© 2025, <a href="https://www.alexanderwolz.de"> Alexander Wolz
