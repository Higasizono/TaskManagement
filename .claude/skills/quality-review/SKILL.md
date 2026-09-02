---
name: quality-review
description: このタスク管理アプリ（React + Spring Boot）の品質レビューを行う。自動チェック（Lint・型・フォーマット・静的解析・テスト）の実行と、React／Spring Boot のデファクトスタンダードからの逸脱、実装とドキュメントの乖離を観点リストに沿って点検する。「品質チェックして」「レビューして」「標準から外れてないか見て」「ドキュメントと実装がずれてないか確認して」等で使用する。
---

# 品質レビュー

このリポジトリの品質を、**(1) 自動チェック → (2) 観点レビュー → (3) ドキュメント整合性** の順で点検する。

## 1. 自動チェック（必ず最初に実行する）

```bash
# フロントエンド：Lint → 型チェック → ビルド
cd frontend && npm run check

# バックエンド：Spotless（フォーマット）→ Checkstyle → JUnit
cd backend && ./gradlew check
```

- `frontend/package.json` の `check` は `lint && typecheck && build` をまとめたもの。個別に見たいときは `npm run lint` / `npm run typecheck` / `npm run build`。
- `lint` は `oxlint --deny-warnings`。警告も CI を落とす。設定は `frontend/.oxlintrc.json`。
- `./gradlew check` は `spotlessCheck` + `checkstyleMain` + `checkstyleTest` + `test` を含む。フォーマット差分は `./gradlew spotlessApply` で自動修正できる。
- Checkstyle 設定は `backend/config/checkstyle/checkstyle.xml`。フォーマット系は Spotless が担うため、ここには入れない。
- 同じ内容が `.github/workflows/ci.yml` で PR ごとに走る。**ローカルで green にしてから push する。**

### Lint 設定を変更するときの判断基準

- oxlint の `pedantic` / `style` カテゴリは有効化しない。`max-lines-per-function`・`no-negated-condition`・`react-in-jsx-scope`（新JSXトランスフォームでは不要）などノイズが多く、シグナルが埋もれる。
- ルールを `off` にするときは**必ず理由をコメントまたはPR本文に残す**。「うるさいから」ではなく「この規約が意図的だから」「この構成では原理的に不要だから」で判断する。
- 意図的な規約と衝突した場合は、ルールを消すのではなく**規約に合わせて設定を緩める**（例：JUnit の `対象_シナリオ` 命名に合わせて Checkstyle の `MethodName` の書式を調整した）。

## 2. 観点レビュー

### React / フロントエンド

| 観点 | 見るべきこと |
|------|------------|
| 状態更新 | `setState(prev => ...)` の関数型更新を使っているか。クロージャで捕捉した state を次の更新の入力に使っていないか（連続操作で stale state を書き戻す） |
| 楽観更新 | ロールバック用スナップショットをクロージャではなく updater 内で取得しているか。失敗時に確実に元へ戻るか |
| useEffect の非同期処理 | fetch にキャンセル（破棄フラグ / `AbortController`）があるか。StrictMode の二重実行、パラメータ高速切替で古いレスポンスに上書きされないか |
| アクセシビリティ | クリック可能要素が `<button>` か（`<div>`/`<span>` + `onClick` はキーボードで到達できない）。ダイアログに `role="dialog"` / `aria-modal` / Esc / 初期フォーカスがあるか。`autoFocus` 属性ではなく `ref.current.focus()` を使っているか |
| 型安全 | `as` キャストが散在していないか。ライブラリが提供する型（例：dnd-kit の `DragStartEvent`）を使わず手書きしていないか |
| デッドコード | スキャフォールドの残骸（`App.css`・`assets/*`・テンプレートのREADME）が残っていないか。未参照ファイルが無いか |
| 責務分割 | 1コンポーネント1責務（要件定義書 3-4）。150行を超えるコンポーネントはカスタムフックへの切り出しを検討 |

### Spring Boot / バックエンド

| 観点 | 見るべきこと |
|------|------------|
| DI | コンストラクタインジェクションか。`@Value` や `@Autowired` のフィールドインジェクションは避ける。設定値は `@ConfigurationProperties` にまとめる |
| バリデーション | 必須パラメータが primitive になっていないか（`int` は JSON から欠落しても 0 で通り `@Min` が機能しない）。ラッパ型 + `@NotNull` にする |
| JPQL / フェッチ | `JOIN FETCH` した子コレクションの並び順を `@OrderBy` の暗黙適用に任せていないか。`ORDER BY` で明示する。N+1 が出ていないか |
| 例外処理 | `@RestControllerAdvice` に想定外例外のフォールバックがあるか。スタックトレースをレスポンスに載せていないか。ログ出力があるか |
| カプセル化 | エンティティのゲッタが内部の可変コレクションを直接返していないか |
| レイヤ | Controller / Service / Repository の責務が分離されているか（要件定義書 3-4）。1クラスが複数リソースを抱え込んでいないか |
| トランザクション | クラスに `@Transactional(readOnly = true)`、更新メソッドに `@Transactional` が付いているか |
| 設定 | `open-in-view: false`、`ddl-auto: validate`、スキーマ変更は Flyway 経由か |

### 既知の未対応事項（レポート済み・意図的に見送っている）

再指摘の前に、下記が既に判断済みであることを確認する。状況が変わったら Issue を切る。

- `BoardController` / `BoardService` が board・column・card の3リソースを1クラスで抱えている（`CardController` / `CardService` への分割余地）
- `updated_at` が DB トリガーと Java 側の二重管理。API レスポンスは Java 側の値を返すため DB 値と一致しない（JPA Auditing への統一 or トリガーへの統一が必要）
- エンティティに `equals` / `hashCode` が無い
- 結合テスト（`@SpringBootTest` / `@DataJpaTest`）が無い。Testcontainers が必要になるため見送り
- `V2__seed_data.sql` が本番マイグレーションパスにある（開発用シードはプロファイル分離が一般的）
- 楽観ロック（`@Version`）が無い
- フロントエンドのユニットテスト（Vitest + Testing Library）が無い
- データ取得の状態管理が手書き（TanStack Query 等の導入余地。ただし素の React を学ぶ目的のため妥当）

## 3. ドキュメント整合性

`docs/` は実装と同期していることが前提。**乖離を見つけたら、原則として実装を正としてドキュメントを直す。**

ただし例外がある：**要件定義書 3-3（セキュリティ）・3-4（コード品質）・3-5（アクセシビリティ）の非機能要件に反している場合は、ドキュメントを下げるのではなく実装を直す。**

チェック対象と、特にずれやすい箇所：

| ドキュメント | 照合先 | ずれやすい点 |
|-------------|--------|------------|
| `docs/screen-design.md` | `frontend/src/components/`・`pages/` | コンポーネント構成ツリーの名前。実在しないコンポーネント名が書かれがち |
| `docs/functional-requirements.md` | 各フォームコンポーネント | ボタンラベル、Esc などのキー操作、バリデーションのクライアント／サーバー分担、エラーメッセージ文言 |
| `docs/screen-requirements.md` | `pages/` | 空状態・エラー状態の有無、レイアウト方式 |
| `docs/screen-flow.md` | `pages/`・`App.tsx` | 実装に無い分岐ノードが残りがち |
| `docs/er-diagram.md`・`docs/database-design.md` | `backend/src/main/resources/db/migration/V1__init.sql` | DDL の逐語一致 |
| `docs/tech-stack.md` | `frontend/package-lock.json`・`./gradlew dependencies` | バージョン番号は解決済みの実値と一致させる |
| `README.md` | `package.json` の scripts・Gradle タスク | コマンドの記載 |

各ドキュメント冒頭の**文書バージョン表を更新**し、変更点の注記を残す。

## 4. 進め方

`CLAUDE.md` の Issue駆動フローに従う。Issue 作成 → `<type>/<issue番号>-<説明>` ブランチ → PR（本文に `Closes #<番号>`）→ **マージはユーザー承認後**。

レビュー結果を報告するときは、**修正したもの**と**レポートのみに留めたもの**を分けて示し、後者は判断理由を添える。
