# jpro-web

**jpro-web** is a minimalistic framework for [JavaFX](https://openjfx.io/).
Is uses a minimalistic Route pattern.

Main Features:
 * Write Webpages with JavaFX and [JPro](https://www.jpro.one/)
 * Page is index by Google
 * Current link opens the same Page.
 * Works on Desktop and with [Gluon Mobile](https://gluonhq.com/products/mobile/). 
 * Minimal design with minimal opinionated API.

## Add jpro-web as dependency
### Gradle
```
repositories {
    maven {
        url "https://sandec.jfrog.io/artifactory/repo"
    }
}
dependencies {
    implementation "com.sandec.jpro:jpro-routing-core:0.11.0"
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
        <groupId>com.sandec.jpro</groupId>
        <artifactId>jpro-web-core</artifactId>
        <version>0.11.0</version>
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
./gradlew :core:publishToMavenLocal :dev:publishToMavenLocal
```

internal:
```gradle
./gradlew :core:publish :dev:publish
```

### clear cache:
```
find ~/.m2 ~/.gradle | grep jpro-web | grep <version> | xargs rm -r
```