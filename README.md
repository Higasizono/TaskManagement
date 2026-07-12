# タスク管理アプリ（Trello風）

React・Java/Spring Boot・PostgreSQL を用いたフルスタック開発の学習を目的としたスクール課題リポジトリ。
ボード内にカラム（未着手／進行中／完了）とカードを配置し、ドラッグ&ドロップでカードを移動できる Trello 風のタスク管理アプリを実装する。

## 目次

- [概要](#概要)
- [主な機能](#主な機能)
- [技術スタック](#技術スタック)
- [ディレクトリ構成](#ディレクトリ構成)
- [セットアップ・起動方法](#セットアップ起動方法)
- [動作確認](#動作確認)
- [テスト](#テスト)
- [ドキュメント](#ドキュメント)
- [開発フロー](#開発フロー)

## 概要

| 項目 | 内容 |
|------|------|
| 背景・目的 | コンポーネント設計・状態管理・ドラッグ&ドロップ・REST APIを介したデータ永続化を実践的に習得するスクール課題 |
| 対象ユーザー | 本人のみ（外部公開なし、認証なし） |
| 利用環境 | Webブラウザ（PCのみ、最小画面幅1024px以上） |

詳細は [要件定義書](docs/requirements.md) を参照。

## 主な機能

| 分類 | 機能 |
|------|------|
| ボード管理 | ボード一覧表示・作成・削除・選択 |
| カラム管理 | 「未着手」「進行中」「完了」の3種の固定カラムをボード作成時に自動生成 |
| カード管理 | カードの表示・追加・タイトル編集・削除・ドラッグ&ドロップによるカラム間移動 |
| データ保存 | REST API経由でPostgreSQLへ自動保存し、再読み込み・別端末からのアクセスでもデータを復元 |

機能ID（B-xx / C-xx / K-xx / D-xx）ごとの詳細は [機能一覧](docs/features.md) を参照。

## 技術スタック

| レイヤー | 技術 |
|----------|------|
| バックエンド | Java 21 / Spring Boot 3.5.16 / Spring Data JPA / Flyway / Gradle |
| データベース | PostgreSQL 16（Docker Compose） |
| フロントエンド | React 19 / TypeScript 6 / Vite 8 / Tailwind CSS 4 / React Router 7 |

バージョンと選定理由の詳細は [技術スタック](docs/tech-stack.md) を参照。

## ディレクトリ構成

```
.
├── backend/    # Spring Boot アプリケーション（Controller / Service / Repository / Entity）
├── frontend/   # React + Vite アプリケーション
├── docs/       # 要件定義・設計ドキュメント一式
├── prototype/  # 初期プロトタイプ（HTML）
└── docker-compose.yml  # ローカル用 PostgreSQL
```

## セットアップ・起動方法

### 前提

- Docker / Docker Compose
- Node.js（`frontend/package.json` に準拠したバージョン）
- Java本体は不要（Gradle Wrapperを同梱、`backend/gradlew` がJava 21ツールチェーンを解決）

### ポート方針（重要）

バックエンドは **8080**、フロントエンドは **5173** で起動する。これらはフロントエンドのAPIベースURL（`frontend/.env` の `VITE_API_BASE_URL`）とバックエンドのCORS許可オリジン（`app.cors.allowed-origins`）に直結しているため、ポート競合時も別ポートで代用せず、競合プロセスを停止してから指定ポートで起動すること。詳細は [.claude/skills/run/SKILL.md](.claude/skills/run/SKILL.md) を参照。

### 手順

1. 環境変数ファイルを用意する（未設定でも既定値で動作する）

   ```bash
   cp .env.example .env
   cp frontend/.env.example frontend/.env
   ```

2. PostgreSQLを起動する

   ```bash
   docker compose up -d postgres
   ```

3. バックエンドを起動する（ポート8080。起動時にFlywayマイグレーションが自動実行される）

   ```bash
   cd backend
   ./gradlew bootRun
   ```

4. フロントエンドを起動する（ポート5173、別ターミナルで）

   ```bash
   cd frontend
   npm install
   npm run dev
   ```

5. ブラウザで [http://localhost:5173](http://localhost:5173) を開く

## 動作確認

```bash
curl -s http://localhost:8080/api/boards
curl -s -o /dev/null -w "%{http_code}\n" http://localhost:5173/
```

バックエンドのAPI仕様はSwagger UI（`http://localhost:8080/swagger-ui.html`）でも確認できる。

## テスト

```bash
# バックエンド（JUnit 5 + Spring Boot Test）
cd backend && ./gradlew test

# フロントエンド（Lint）
cd frontend && npm run lint
```

## ドキュメント

要件定義から設計までのドキュメントは `docs/` 配下に一式揃っている。

| ドキュメント | 説明 |
|-------------|------|
| [要件定義書](docs/requirements.md) | プロジェクト概要・非機能要件・スコープ外事項 |
| [機能一覧](docs/features.md) | 機能ID（B-xx / C-xx / K-xx / D-xx）と概要の一覧 |
| [機能要件](docs/functional-requirements.md) | ユースケース・操作フロー・バリデーションルール |
| [画面要件](docs/screen-requirements.md) | 画面一覧・表示要件・インタラクション仕様 |
| [画面設計書](docs/screen-design.md) | コンポーネント構成・レイアウト詳細 |
| [画面遷移図](docs/screen-flow.md) | 画面間遷移・UIフロー（Mermaid） |
| [ER図](docs/er-diagram.md) | データモデル・エンティティ定義・DDL |
| [データベース設計](docs/database-design.md) | PostgreSQLのテーブル設計・DDL |
| [技術スタック](docs/tech-stack.md) | 採用技術・バージョン・選定理由 |

## 開発フロー

このリポジトリでは Issue駆動開発（`type/issue番号-説明` 形式のブランチ命名、PR経由でのマージ、mainへの直接push禁止）を採用している。詳細は [CLAUDE.md](CLAUDE.md) を参照。
