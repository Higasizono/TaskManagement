---
name: run
description: Launch this task management app's backend (Spring Boot) and frontend (Vite) dev servers on their fixed default ports for manual verification. Use whenever asked to start, run, or check the app is working end-to-end.
---

# アプリの起動

## 固定ポート方針(最重要)

このアプリはポート番号がハードコードで結びついている。

- バックエンド(Spring Boot): **8080**(`backend/src/main/resources/application.yml` の `server.port`)
- フロントエンド(Vite): **5173**(Viteのデフォルト。`frontend/.env` の `VITE_API_BASE_URL` がバックエンドの `http://localhost:8080` を指す。バックエンドのCORS設定 `app.cors.allowed-origins` も `http://localhost:5173` のみを許可)

**ポートが競合しても、別のポートで起動してはいけない。** 別ポートで動かすと、フロントエンド→バックエンドの通信(APIベースURL・CORS許可オリジン)が食い違い、正しく動作しない。

ポート競合を検知したら、必ず次の手順を踏む。

1. 競合しているプロセスを特定して停止する
   ```bash
   lsof -ti:8080 | xargs -r kill    # バックエンド用ポートを解放
   lsof -ti:5173 | xargs -r kill    # フロントエンド用ポートを解放
   ```
   これで停止しない場合のみ `kill -9` を使う:
   ```bash
   lsof -ti:8080 | xargs -r kill -9
   lsof -ti:5173 | xargs -r kill -9
   ```
2. ポートが空いたことを確認する(出力が空であればOK)
   ```bash
   lsof -i:8080 -i:5173
   ```
3. 空いた上で、指定ポート(8080 / 5173)で改めて起動する。

## 起動手順

1. PostgreSQLを起動
   ```bash
   docker compose up -d postgres
   ```
2. バックエンドを起動(ポート8080、バックグラウンド)
   ```bash
   cd backend && ./gradlew bootRun
   ```
3. フロントエンドを起動(ポート5173、バックグラウンド。`.env` が無ければ `.env.example` をコピーする)
   ```bash
   cd frontend && npm run dev
   ```
4. 動作確認
   ```bash
   curl -s http://localhost:8080/api/boards
   curl -s -o /dev/null -w "%{http_code}\n" http://localhost:5173/
   ```

## 補足

- CORS設定を含むバックエンドの変更がまだ `main` にマージされていない場合、動作確認したいブランチを `git worktree` で別ディレクトリにチェックアウトしてバックエンドを起動するとよい(現在のブランチのフロントエンド作業を止めずに済む)。
