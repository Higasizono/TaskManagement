# 技術スタック

| 項目 | 内容 |
|------|------|
| 文書バージョン | 2.3 |
| 作成日 | 2026-06-29 |
| 更新日 | 2026-07-13 |
| 作成者 | Higasizono |

---

> **注記（v2.0での変更点）**: フロントエンド完結（localStorage）構成から、Java/Spring Boot バックエンド + PostgreSQL データベースを用いたフルスタック構成に方針転換した。Next.jsは今回のフロントエンドフレームワークとして対象外とし、React + Vite の構成を継続する。
> **注記（v2.1での変更点）**: バックエンドのビルドツールをMavenからGradleに変更した。バックエンドの雛形は `backend/` ディレクトリに作成済み（Spring Boot 3.5.16 / Java 21 / Gradle Wrapper同梱）。
> **注記（v2.2での変更点）**: フロントエンドの雛形を `frontend/` ディレクトリに作成した。`npm create vite@latest` 時点の最新メジャーバージョン（React 19 / Vite 8 / Tailwind CSS 4 / React Router 7）を採用したため、下表のバージョンを実際の採用値に更新した。
> **注記（v2.3での変更点）**: バックエンド・フロントエンドの実装が一通り揃ったため、`./gradlew dependencies`（バックエンド）および `package-lock.json`（フロントエンド）で実際に解決されたバージョンを確認し、下表を確定値に更新した。

## バックエンド

| レイヤー | 採用技術 | バージョン | 選定理由 |
|----------|----------|-----------|----------|
| 言語 | Java | 21（LTS） | 現行LTS。Spring Boot 3系の推奨環境で長期サポートが受けられる |
| フレームワーク | Spring Boot | 3.5.16 | デファクトスタンダード。エコシステム・学習リソースが豊富 |
| ビルドツール | Gradle（Groovy DSL） | 8.14.1（Gradle Wrapper同梱） | Gradle Wrapper（`gradlew`）を同梱し、ローカルにGradle本体が無くても再現性高くビルド可能 |
| API方式 | REST API（Spring Web／Spring Framework 6.2.19） | — | フロントエンドとの疎結合。JSON形式でのやり取りがシンプル |
| データアクセス | Spring Data JPA 3.5.13（Hibernate ORM 6.6.53.Final） | — | リポジトリパターンでSQL記述量を削減。既存ER図のエンティティ定義をそのままEntityへ落とし込める |
| バリデーション | Jakarta Bean Validation（Hibernate Validator 8.0.3.Final） | — | アノテーションベースでサーバーサイドの入力チェックを簡潔に実装（フロントエンドのバリデーションルールと二重化） |
| DBマイグレーション | Flyway（flyway-core / flyway-database-postgresql） | 11.7.2 | 既存のDDL（[データベース設計](database-design.md)）をバージョン管理されたマイグレーションとして運用 |
| APIドキュメント | springdoc-openapi（Swagger UI） | 2.8.5 | API仕様を可視化し、フロントエンド開発時の動作確認を容易にする |
| DBドライバ | PostgreSQL JDBC Driver | 42.7.11 | Spring Data JPA経由でPostgreSQLへ接続するための標準ドライバ |
| テスト | JUnit Jupiter 5.12.2 + Spring Boot Test 3.5.16（Mockito 5.17.0） | — | Spring Boot標準のテスト基盤 |

## データベース

| レイヤー | 採用技術 | バージョン | 選定理由 |
|----------|----------|-----------|----------|
| DBMS | PostgreSQL | 16（Dockerイメージ `postgres:16`） | 指定技術。既存のDDL・ER図（[ER図](er-diagram.md)）がそのまま利用可能 |
| ローカル実行環境 | Docker Compose | — | ローカルでのPostgreSQL起動を再現性高く簡潔に行う標準的な方法 |

## フロントエンド

| レイヤー | 採用技術 | バージョン | 選定理由 |
|----------|----------|-----------|----------|
| UIフレームワーク | React / React DOM | 19.2.7 | コンポーネントベースの設計を学ぶ。学習目標に直結するため選定（Next.jsは今回対象外とし、素のReact構成を維持） |
| ビルドツール | Vite（`@vitejs/plugin-react` 6.0.3 使用） | 8.1.4 | セットアップが速く、HMRによる開発体験が良い |
| 言語 | TypeScript | 6.0.3 | 型安全なコードを書く習慣を身につける。データ設計の明示にも有効 |
| スタイリング | Tailwind CSS（`@tailwindcss/vite` 使用） | 4.3.2 | クラス名でスタイルを完結させ、CSS設計の複雑さを排除。学習コストが低い |
| Lint | oxlint | 1.73.0 | Rust製の高速Linter。追加設定なしで基本的な静的解析を実施 |
| ドラッグ&ドロップ | dnd-kit | 未導入（導入予定バージョン系列 6.x） | React向けの軽量ライブラリ。アクセシビリティ対応済み・APIがシンプル（カードの並び替え機能実装時に導入予定。現時点では未導入） |
| ルーティング | React Router（`react-router-dom`） | 7.18.1 | SPAの画面遷移（ボード一覧 ↔ ボード詳細）を実装するために使用 |
| HTTPクライアント | Fetch API（ブラウザ標準） | — | 追加ライブラリ不要。CRUD通信程度であれば標準APIで十分 |
| データ保存 | PostgreSQL（Spring Boot REST API経由） | — | サーバー・DBと連携し、ブラウザを跨いだ永続化を実現 |
| ID生成 | サーバー側生成（PostgreSQLの`gen_random_uuid()`） | — | IDはバックエンドのDB側で採番する方式に統一 |
