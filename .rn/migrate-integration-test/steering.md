# Steering: nablarch-testing から integration test を移動

## Goal

`nablarch-testing` リポジトリの `nablarch.test.core.integration` パッケージを
`nablarch-testing-integration` リポジトリへ移動し、独立した Maven モジュールとして公開できる状態にする。

## Context

- 移動元: `/home/tie303177/work/nablarch/nablarch-testing`
  - `src/main/java/nablarch/test/core/integration/IntegrationTestSupport.java`
  - `src/main/java/nablarch/test/core/integration/package-info.java`
- 移動先: `/home/tie303177/work/nablarch/nablarch-testing-integration` (このリポジトリ)
  - 現時点では空の Maven モジュール（LICENSE, README.md, .gitignore のみ）
- 作業ブランチ: `feature/migrate-integration-test`

## Tasks

<!-- タスクリストは作業指示を受け取り次第、追記する -->

- [ ] #1 — 作業指示の受け取りと詳細タスク設計

## Acceptance criteria

- `nablarch-testing-integration` が単独でビルド・テストできる（`mvn verify` が通る）
- `IntegrationTestSupport` が `nablarch-testing-integration` に存在し、
  `nablarch-testing` 側からは削除されている（または非推奨マーク済み）
- パッケージ名・クラス名・公開 API の後方互換性が維持されている

## State

<!--
last_completed: (none)
next_task: #1
notes: 作業指示待ち
-->
