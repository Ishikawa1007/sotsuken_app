# プロジェクト編集ルール
※編集中！！
このドキュメントは、プロジェクト内のどのファイル・フォルダを
「編集してよいか」「編集禁止か」「要相談か」をまとめたものです。
---

## 🟥 編集禁止

### 理由
ビルド設定・自動生成物・環境依存ファイルのため、編集するとプロジェクトが壊れる可能性がある。
これ触ったら結構怒りますよ。

### 対象
- `app/build/`（自動生成物）
- `app/.gradle/`
- `build.gradle.kts (Project)`
- `build.gradle.kts (Module: app)`
- `settings.gradle.kts`
- `gradle.properties`
- `local.properties`
- `gradle-wrapper.properties`
- `.idea/`（IDE設定）
- `.gradle/`（キャッシュ）

---

## 🟨 要相談

### 理由
アプリ全体の挙動やデザインに大きく影響するため、変更には相談が必要。
無連絡で触らないでください。理由も必ず説明しましょう。
### 対象
- `app/src/main/AndroidManifest.xml`
- `app/src/main/res/values/themes.xml`
- `app/src/main/res/values/themes.xml (night)`
- `app/src/main/res/xml/`（バックアップ設定など）
- `proguard-rules.pro`（難読化設定）

---

## 🟩 編集OK

### 理由
アプリの機能・UI・リソースを実装するための領域。

### 対象
- `app/src/main/java/`
- `app/src/main/res/layout/`
- `app/src/main/res/drawable/`
- `app/src/main/res/mipmap/`
- `app/src/main/res/values/strings.xml`
- `app/src/main/res/values/colors.xml`
---

##  補足

- **編集禁止 → 要相談 → 編集OK** の順で優先度が高い。
- 迷った場合は、まずこのファイルを確認してから編集する。
- ファイルやフォルダを追加した場合は、これらに追加する。ただし、親フォルダが指定されている場合、はこの限りではない。
- 基本的に編集の可否が書いてあるmdファイルを置いている。置いてあるフォルダの内部はすべて書いてある内容の通り。