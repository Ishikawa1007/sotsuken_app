# プロジェクト編集ルール

※編集中！！
このドキュメントは、プロジェクト内のどのファイル・フォルダを
「編集してよいか」「編集禁止か」「要相談か」をまとめたものです
---

## 🟥 編集禁止

### 理由

ビルド設定・自動生成物・環境依存ファイルのため、編集するとプロジェクトが壊れる可能性がある。
これ触ったら結構怒りますよ。

### 対象

- .gitignore
- build.gradle.kts
- gradle.properties
- gradlew
- gradlew.bat
- settings.gradle.kts
- tree.txt
- gradle/
- .idea/
- app/.gitignore
- app/build.gradle.kts
- app/bin/default/androidTest/
- app/bin/default/main/java/
- app/bin/default/main/res/drawable/
- app/bin/default/main/res/layout/
- app/bin/default/main/res/mipmap-anydpi-v26/
- app/bin/default/main/res/mipmap-hdpi/
- app/bin/default/main/res/mipmap-mdpi/
- app/bin/default/main/res/mipmap-xhdpi/
- app/bin/default/main/res/mipmap-xxhdpi/
- app/bin/default/main/res/mipmap-xxxhdpi/
- app/bin/default/main/res/values/colors.xml
- app/bin/default/main/res/strings.xml
- app/bin/default/test/
- gradle/

結構多いので要注意

---

## 🟨 要相談

### 理由

アプリ全体の挙動やデザインに大きく影響するため、変更には相談が必要。
無連絡で触らないでください。理由も必ず説明しましょう。

### 対象

- app/proguard-rules.pro
- app/bin/default/main/AndroidManifest.xml
- app/bin/default/main/res/values/themes.xml
- app/bin/default/main/res/values-night/
- app/bin/default/main/res/xml/
- app/src/main/AndroidManifest.xml
- app/src/main/res/values/themes.xml
- app/src/main/res/values-night/
- app/src/main/res/xml/

---

## 🟩 編集OK

### 理由

アプリの機能・UI・リソースを実装するための領域。

### 対象

- rule.md
- app/src/androidTest/
- app/src/res/editable.md
- app/src/res/drawable/ic_launcher_background.xml
- app/src/res/drawableic_launcher_foreground.xml
- app/src/res/layout/
- app/src/res/mipmap-anydpi-v26/
- app/src/res/mipmap-hdpi/
- app/src/res/mipmap-mdpi/
- app/src/res/mipmap-xhdpi/
- app/src/res/mipmap-xxhdpi/
- app/src/res/mipmap-xxxhdpi/
- app/src/res/values/colors.xml
- app/src/res/valuesstrings.xml
- app/src/test/
- src/

---

## 補足

- 迷った場合は、まずこのファイルを確認する。
- ファイルやフォルダを追加した場合は、これらに追加する。ただし、親フォルダが指定されている場合、はこの限りではない。
- 基本的に編集の可否が書いてあるmdファイルを置いている。置いてあるフォルダの内部はすべて書いてある内容の通り。

以下そのままの編集権限

|   .gitignore   🟥  
|   build.gradle.kts   🟥  
|   gradle.properties   🟥  
|   gradlew   🟥  
|   gradlew.bat   🟥  
|   rule.md   🟩  
|   settings.gradle.kts   🟥  
|   tree.txt   🟥  
|
+---.gradle   🟥  
|   +---9.3.1   🟥  
|   |   |   gc.properties   🟥  
|   |   |
|   |   +---checksums   🟥  
|   |   |       checksums.lock   🟥  
|   |   |       md5-checksums.bin   🟥  
|   |   |       sha1-checksums.bin   🟥  
|   |   |
|   |   +---executionHistory   🟥  
|   |   |       executionHistory.lock   🟥  
|   |   |
|   |   +---expanded   🟥  
|   |   +---fileChanges   🟥  
|   |   |       last-build.bin   🟥  
|   |   |
|   |   +---fileHashes   🟥  
|   |   |       fileHashes.bin   🟥  
|   |   |       fileHashes.lock   🟥  
|   |   |
|   |   \---vcsMetadata   🟥  
|   |
|   +---buildOutputCleanup   🟥  
|   |       buildOutputCleanup.lock   🟥  
|   |       cache.properties   🟥  
|   |
|   \---vcs-1   🟥  
|           gc.properties   🟥  
|
+---.idea   🟥  
|       .gitignore   🟥  
|       .name   🟥  
|       AndroidProjectSystem.xml   🟥  
|       appInsightsSettings.xml   🟥  
|       compiler.xml   🟥  
|       deploymentTargetSelector.xml   🟥  
|       deviceManager.xml   🟥  
|       gradle.xml   🟥  
|       misc.xml   🟥  
|       runConfigurations.xml   🟥  
|       vcs.xml   🟥  
|
+---app
|   |   .gitignore   🟥  
|   |   build.gradle.kts   🟥  
|   |   proguard-rules.pro   🟨  
|   |
|   +---bin   🟥  
|   |   \---default   🟥  
|   |       +---androidTest   🟥  
|   |       |   \---java   🟥  
|   |       |       \---com   🟥  
|   |       |           \---example   🟥  
|   |       |               \---app411   🟥  
|   |       |                       ExampleInstrumentedTest.class   🟥  
|   |       |
|   |       +---main   🟥  
|   |       |   |   AndroidManifest.xml   🟨  
|   |       |   |
|   |       |   +---java   🟥  
|   |       |   |   \---com   🟥  
|   |       |   |       \---example   🟥  
|   |       |   |           \---app411   🟥  
|   |       |   |                   MainActivity.class   🟥  
|   |       |   |
|   |       |   \---res   🟥  
|   |       |       +---drawable   🟥  
|   |       |       |       ic_launcher_background.xml   🟥  
|   |       |       |       ic_launcher_foreground.xml   🟥  
|   |       |       |
|   |       |       +---layout   🟥  
|   |       |       |       page1.xml   🟥  
|   |       |       |       page2.xml   🟥  
|   |       |       |       page3.xml   🟥  
|   |       |       |       page4.xml   🟥  
|   |       |       |       page5.xml   🟥  
|   |       |       |
|   |       |       +---mipmap-anydpi-v26   🟥  
|   |       |       |       ic_launcher.xml   🟥  
|   |       |       |       ic_launcher_round.xml   🟥  
|   |       |       |
|   |       |       +---mipmap-hdpi   🟥  
|   |       |       |       ic_launcher.webp   🟥  
|   |       |       |       ic_launcher_round.webp   🟥  
|   |       |       |
|   |       |       +---mipmap-mdpi   🟥  
|   |       |       |       ic_launcher.webp   🟥  
|   |       |       |       ic_launcher_round.webp   🟥  
|   |       |       |
|   |       |       +---mipmap-xhdpi   🟥  
|   |       |       |       ic_launcher.webp   🟥  
|   |       |       |       ic_launcher_round.webp   🟥  
|   |       |       |
|   |       |       +---mipmap-xxhdpi   🟥  
|   |       |       |       ic_launcher.webp   🟥  
|   |       |       |       ic_launcher_round.webp   🟥  
|   |       |       |
|   |       |       +---mipmap-xxxhdpi   🟥  
|   |       |       |       ic_launcher.webp   🟥  
|   |       |       |       ic_launcher_round.webp   🟥  
|   |       |       |
|   |       |       +---values   🟥  
|   |       |       |       colors.xml   🟥  
|   |       |       |       strings.xml   🟥  
|   |       |       |       themes.xml   🟨  
|   |       |       |
|   |       |       +---values-night   🟥  
|   |       |       |       themes.xml   🟨  
|   |       |       |  
|   |       |       \---xml   🟥  
|   |       |               backup_rules.xml   🟨  
|   |       |               data_extraction_rules.xml   🟨  
|   |       |
|   |       \---test   🟥  
|   |           \---java   🟥  
|   |               \---com   🟥  
|   |                   \---example   🟥  
|   |                       \---app411   🟥  
|   |                               ExampleUnitTest.class   🟥  
|
|   \---src  
|       +---androidTest  
|       |   \---java  
|       |       \---com  
|       |           \---example  
|       |               \---app411  
|       |                       editable.md   🟩  
|       |                       ExampleInstrumentedTest.java   🟩  
|       |
|       +---main  
|       |   |   AndroidManifest.xml   🟨  
|       |   |  
|       |   +---java  
|       |   |   \---com  
|       |   |       \---example  
|       |   |           \---app411  
|       |   |                   editable.md   🟩  
|       |   |                   MainActivity.java   🟩  
|       |   |  
|       |   \---res  
|       |       |   editable.md   🟩  
|       |       |
|       |       +---drawable  
|       |       |       ic_launcher_background.xml   🟩  
|       |       |       ic_launcher_foreground.xml   🟩  
|       |       |  
|       |       +---layout  
|       |       |       page1.xml   🟩  
|       |       |       page2.xml   🟩  
|       |       |       page3.xml   🟩  
|       |       |       page4.xml   🟩  
|       |       |       page5.xml   🟩  
|       |       |
|       |       +---mipmap-anydpi-v26  
|       |       |       ic_launcher.xml   🟩  
|       |       |       ic_launcher_round.xml   🟩  
|       |       |
|       |       +---mipmap-hdpi  
|       |       |       ic_launcher.webp   🟩  
|       |       |       ic_launcher_round.webp   🟩  
|       |       |
|       |       +---mipmap-mdpi  
|       |       |       ic_launcher.webp   🟩  
|       |       |       ic_launcher_round.webp   🟩  
|       |       |
|       |       +---mipmap-xhdpi  
|       |       |       ic_launcher.webp   🟩  
|       |       |       ic_launcher_round.webp   🟩  
|       |       |
|       |       +---mipmap-xxhdpi  
|       |       |       ic_launcher.webp   🟩  
|       |       |       ic_launcher_round.webp   🟩  
|       |       |
|       |       +---mipmap-xxxhdpi  
|       |       |       ic_launcher.webp   🟩  
|       |       |       ic_launcher_round.webp   🟩  
|       |       |
|       |       +---values  
|       |       |       colors.xml   🟩  
|       |       |       strings.xml   🟩  
|       |       |       themes.xml   🟨  
|       |       |
|       |       +---values-night
|       |       |       themes.xml   🟨  
|       |       |
|       |       \---xml
|       |               backup_rules.xml   🟨
|       |               data_extraction_rules.xml   🟨  
|       |
|       \---test
|           \---java
|               \---com
|                   \---example
|                       \---app411
|                               editable.md   🟩
|                               ExampleUnitTest.java   🟩
|
+---gradle
|   |   gradle-daemon-jvm.properties   🟥
|   |   libs.versions.toml   🟥
|   |
|   \---wrapper
|           gradle-wrapper.jar   🟥
|           gradle-wrapper.properties   🟥
|
\---src   🟩
