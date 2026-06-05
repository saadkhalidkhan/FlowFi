# Mizafi — Play Store release

This branch prepares **Mizafi** for Google Play publishing. The open-source repository remains **[FlowFi](https://github.com/saadkhalidkhan/FlowFi)**; only the shipped app identity changes.

| Item | Value |
|------|--------|
| **Play Store app name** | Mizafi |
| **Package name** | `com.saadproductlabs.mizafi` |
| **Developer / publisher** | Saad Product Labs |

## Build a release APK or AAB

```bash
./gradlew :app:bundleRelease
```

Output: `app/build/outputs/bundle/release/app-release.aab`

For a local APK:

```bash
./gradlew :app:assembleRelease
```

## Install debug build

```bash
./gradlew :app:installDebug
adb shell am start -n com.saadproductlabs.mizafi/.MainActivity
```

## Before you upload

1. **Signing** — Configure a release keystore in `app/build.gradle.kts` (not committed).
2. **Store listing** — App name **Mizafi**, developer **Saad Product Labs**.
3. **Privacy policy** — Required if you collect data; this app stores data locally only (Room).
4. **Screenshots** — Capture from this branch (Mizafi branding).
5. **Version** — Bump `versionCode` / `versionName` in `app/build.gradle.kts` for each release.

## Copyright

Source is licensed under the [MIT License](LICENSE):

> Copyright (c) 2026 Saad Khalid Khan

Publishing on Play Store under **Saad Product Labs** does not change the license or GitHub repo name.
