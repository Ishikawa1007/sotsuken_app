# Git ブランチ運用マニュアル（Android Studio GUI版）

## 目的

複数人で安全に開発するため、以下の運用を統一する。

- master を安定版として維持する
- 作業は個人ブランチで行う
- 完成後に master へ統合する
- GitHub 上へ定期的に push する

---

# 基本構成

```text
master
 ├─ feature/ui
 ├─ feature/graph
 └─ feature/data
```

- master
  - 安定版
  - GitHub Pages 公開用
  - 直接編集しない

- feature/○○
  - 個人作業用ブランチ
  - 作業ごとに作成

---

# 基本作業フロー

## 1. 最新状態を取得

作業開始前に必ず同期する。
やり忘れると8で困る。

### Android Studio

上部メニュー：

```text
Git → Pull
```

または：

```text
Git → Update Project
```

---

# 2. 新しいブランチを作成

## 手順

1. Git Log を開く
2. `master` を右クリック
3. `New Branch from Selected...`
4. ブランチ名を入力

例：

```text
feature/ui
feature/graph
feature/fix
```

### 内容がわかるように

5. Create

---

# 3. ブランチへ移動（Checkout）

## 手順

1. 作成したブランチをダブルクリック

または：

1. ブランチ右クリック
2. `Checkout`

---

# 4. 作業

通常通り編集する。

例：

- XML 修正
- Java 修正

---

# 5. Commit

小さい単位で commit する。

## 推奨

- 1機能
- 1修正
- 1バグ修正

ごとに commit。

---

## 手順

### Commit 画面を開く

```text
Git → Commit
```

または：

```text
Ctrl + K
```

---

## Commit Message 例

```text
ヘッダー修正
グラフ描画修正
レスポンシブ対応
```

### AIに任せて編集ファイルだけ書かないように

5.6をまとめてやることもできる。

---

# 6. Push

commit 後はできるだけ早く push する。

## 手順

```text
Git → Push
```

または：

```text
Ctrl + Shift + K
```

---

# 5.6. 融合

コミットしてプッシュボタンをクリック。

# 7. master へ戻る

## 手順

1. `master` をダブルクリック

または：

1. `master` を右クリック
2. `Checkout`

---

# 8. ブランチを master に Merge

## 手順

1. merge したいブランチを右クリック
2. `Merge into Current`

例：

```text
Merge 'feature/ui' into 'master'
```

---

# 9. Merge 後の確認

merge 後は必ず確認する。

## 確認項目

- ページ表示
- Console エラー
- JavaScript 動作
- CSS 崩れ

---

# 10. master を Push

merge 後：

```text
Git → Push
```

を実行。

GitHub 側へ反映される。

---

# 11. 不要ブランチ削除

merge 完了後、不要なら削除。

## 手順

1. ブランチ右クリック
2. `Delete`

---

# 注意事項

## master を直接編集しない

必ず個人ブランチを使用する。

---

## 作業前に Pull

Pull しないと：

- push reject
- conflict
- 上書き事故

が発生しやすい。

---

## force push を使わない

以下は禁止。

```bash
git push --force
```

共同開発では非常に危険。
~~自分が編集している領域なら最悪いい~~

---

## conflict 発生時

Git が自動統合できない場合：

```text
CONFLICT
```

が表示される。

その場合は：

- どちらを残すか確認（たいていできないと思うので、自分のものを優先しても良い。ただし、上塗りをされる可能性もあるので、コメントアウトを書いて、内容をわかりやすくすること。）
- 手動修正
- 再commit

を行う。

---

# 推奨運用

## 良い運用

```text
小commit
＋
頻繁push
＋
小さいmerge
```

---

## 悪い運用

```text
数日ローカル作業
↓
大量変更を一括merge
```

大量 conflict の原因になる。

---

# 用語

| 用語 | 意味 |
|---|---|
| Commit | 作業記録 |
| Push | GitHubへ送信 |
| Pull | GitHubから取得 |
| Branch | 作業分岐 |
| Checkout | 作業ブランチ切替 |
| Merge | 内容統合 |
| Conflict | 競合 |
