# [JPro Routing](https://github.com/JPro-one/jpro-routing) has moved to [JPro Platform](https://github.com/JPro-one/jpro-platform)!

🚨 **IMPORTANT NOTICE:** This repository has been relocated. All the content, issues, and activity have been transferred to our new repository.

## Why did we move?
We've decided to consolidate our repositories into a single one to better manage and streamline our projects.
This will allow us to better prioritize and address issues and pull requests, as well as to release new versions faster.
___

# jpro-routing

**jpro-routing** is a minimalistic framework for [JavaFX](https://openjfx.io/).
Is uses a minimalistic Route pattern.

Main Features:
 * Write Webpages with JavaFX and [JPro](https://www.jpro.one/)
 * Page is index by Google
 * Current link opens the same Page.
 * Works on Desktop and with [Gluon Mobile](https://gluonhq.com/products/mobile/). 
 * Minimal design with minimal opinionated API.

## Add jpro-routing as dependency
### Gradle
```
repositories {
    maven {
        url "https://sandec.jfrog.io/artifactory/repo"
    }
}
dependencies {
    implementation "one.jpro:jpro-routing-core:0.15.0"
}
```
### Maven
```
<repositories>
    <repository>
        <id>jpro - sandec repository</id>
        <url>https://sandec.jfrog.io/artifactory/repo/</url>
    </repository>
</repositories>
..
dependency>
        <groupId>one.jpro</groupId>
        <artifactId>jpro-routing-core</artifactId>
        <version>0.15.0</version>
        <scope>compile</scope>
</dependency>
```




## Development Documentation

### run
```gradle
./gradlew example:run
```

```gradle
./gradlew example:jproRun
```

### publishing
local:
```gradle
./gradlew publishToMavenLocal
```

internal:
```gradle
./gradlew publish
```

### clear cache:
```
./deleteCache.sh
```