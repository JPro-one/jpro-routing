# jpro-web

### publish

bintray:
```gradle
./gradlew :core:bintrayUpload
```

local:
```gradle
./gradlew :core:publishToMavenLocal
```

### run
```gradle
./gradlew example:run
```

```gradle
./gradlew example:jproRun
```

### clear cache:
```
find ~/.m2 ~/.gradle | grep jpro-web | grep 0.8.0 | xargs rm -r
```