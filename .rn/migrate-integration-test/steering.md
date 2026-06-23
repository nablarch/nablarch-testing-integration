# Steering: nablarch-testing-integration 構築

## Goal

`nablarch-testing`（ブランチ `convert-testdata-excel-to-text`）から integration 対象ファイルを
本リポジトリへ移動・コピーし、YAMLサブ20クラスすべてが `mvn test` で単独実行緑になる状態にする。

## Rules

- **変更したら必ずプッシュする**（コミット後に `git push` を実行する）
- PR本文はステアリングへのリンクのみとする（内容の重複を防ぐ）

## Context

- 移動元: `/home/tie303177/work/nablarch/nablarch-testing`（ブランチ `convert-testdata-excel-to-text`）
  - **参照専用。本体への書き込み・削除は一切行わない**
- 移動先: `/home/tie303177/work/nablarch/nablarch-testing-integration`（本リポジトリ）
- 作業ブランチ: `feature/migrate-integration-test`
- yaml・converter は構築済み・`mvn install` 済み（`~/.m2` に配置済み）
- 実行環境: JDK21 / Maven3.9.9 / `ja_JP.UTF-8` / `TZ=Asia/Tokyo`
- DB系テストは H2 共有のため **必ず1クラス単独実行**（`-Dtest=<クラス名>`）
- 正本ファイル一覧: `.rn/migrate-integration-test/repo-split-filelist.md`（#列の番号で参照）
- 作業指示詳細: `.rn/migrate-integration-test/cc-integration-build.md`

### 処理ルール

- **移動**（#128–153）: 本体から integration へコピー（本体側は削除しない＝コピー動作）
- **コピー**（#21–51）: 本体に残しつつ integration にも複製
- 変更禁止: 物理コピー／package・import の機械的調整／pom 設定のみ許可
- ロジック・期待値・アサーション変更が要る場合は**手を止めてユーザーに報告**

## Tasks

- [x] #1 — pom.xml 作成
- [x] #2 — 基盤5件 + 共通設定 + 変換生成データの配置
- [x] #3 — TestSupportYamlTest 配置・緑確認
- [x] #4 — MultiResourceDataSetUpYamlTest 配置・緑確認
- [x] #5 — BatchRequestTestSupportYamlTest 配置・緑確認
- [x] #6 — DBtoDBBatchSampleYamlTest 配置・緑確認
- [x] #7 — FileToFileBatchSampleYamlTest 配置・緑確認
- [x] #8 — SimpleBatchSampleYamlTest 配置・緑確認
- [x] #9 — DbAccessTestSupportYamlTest 配置・緑確認
- [x] #10 — EntityTestSupportYamlTest 配置・緑確認
- [x] #11 — TestBeanYamlTest 配置・緑確認
- [x] #12 — TestEntityYamlTest 配置・緑確認
- [x] #13 — FileSupportWithDbLessTestDataParserYamlTest 配置・緑確認
- [x] #14 — FileSupportYamlTest 配置・緑確認
- [x] #15 — AbstractHttpRequestTestTemplateTest2YamlTest 配置・緑確認
- [x] #16 — AbstractHttpRequestTestTemplateYamlTest 配置・緑確認
- [x] #17 — MessagingReceiveTestSupportYamlTest 配置・緑確認
- [x] #18 — MessagingRequestTestSupportYamlTest 配置・緑確認
- [x] #19 — RequestTestingMessagingClientYamlTest 配置・緑確認
- [x] #20 — RequestTestingMessagingContextYamlTest 配置・緑確認
- [x] #21 — RequestTestingSendSyncBatchYamlTest 配置・緑確認
- [x] #22 — RequestTestingSendSyncSupportYamlTest 配置・緑確認

### タスク詳細

#### #1 pom.xml 作成

**Purpose**: Maven モジュールとして認識できる最小限の pom.xml を作成する。

**Steps**:
1. `pom.xml` を本リポジトリルートに作成
   - GAV: `com.nablarch.framework:nablarch-testing-integration:1.0.0-SNAPSHOT`
   - 親: `com.nablarch:nablarch-parent:6-NEXT-SNAPSHOT`
   - 依存（scope test 含む）:
     - `nablarch-testing-yaml:1.0.0-SNAPSHOT`
     - `nablarch-testing-converter:1.0.0-SNAPSHOT`
     - `nablarch-testing`（本体）最新
     - `poi-ooxml:3.8`
     - `snakeyaml-engine:3.0.1`
     - `json-schema-validator:3.0.2`
     - `junit:4.13.1`
   - converter の pom.xml（`/home/tie303177/work/nablarch/nablarch-testing-converter/pom.xml`）を参考にする
2. `mvn validate` で構文エラーがないことを確認

**Completion criteria**:
- `pom.xml` が存在し `mvn validate` が成功する
- GAV・親・依存が指示どおり設定されている

---

#### #2 基盤5件 + 共通設定 + 変換生成データの配置

**Purpose**: YAMLサブ20クラスが共通で使う基盤クラス・設定ファイル・変換生成データを1度だけ配置する。

**Steps**:
1. 基盤5件（移動, #149–153）を本体から同一パッケージパスで `src/test/java/...` に配置:
   - `ExcelToYamlEquivalenceTest.java`
   - `FormatAwareTestDataParser.java`
   - `TestDataFormat.java`
   - `YamlSchemaValidationTest.java`
   - `nablarch/test/core/reader/yaml/YamlModeTestBase.java`
2. 共通設定（移動, #154–164）を `src/test/resources/...` に配置:
   - `nablarch/test/core/db/DbAccessTestSupportYamlTest.xml`
   - `nablarch/test/core/http/http-test-configuration-format-aware.xml`
   - `nablarch/test/core/http/http-test-configuration-with-htmlcheck-format-aware.xml`
   - `nablarch/test/core/http/http-test-configuration-yaml.xml`
   - `nablarch/test/core/messaging/XmlAssertAsStringTest-yaml.xml`
   - `nablarch/test/core/messaging/web/web-component-configuration-request-testing-yaml.xml`
   - `unit-test-format-aware.config`
   - `unit-test-format-aware.xml`
   - `unit-test-yaml-dbless.xml`
   - `unit-test-yaml.config`
   - `unit-test-yaml.xml`
3. 変換生成データ（移動, #148）を配置:
   - `src/test/java/nablarch/test/core/reader/BasicTestDataParserTest/*`（8件）
4. `mvn compile -Dmaven.test.skip=true` でコンパイルエラーがないことを確認

**Completion criteria**:
- 上記全ファイルが所定パスに存在する
- `mvn compile -Dmaven.test.skip=true` が成功する

---

#### #3–#22 YAMLサブ各クラスの配置・緑確認

各タスクの共通手順:
1. **静的に辿る**: YAMLサブクラスの import → 対応Excelベース → 補助クラスを確認
2. **YAMLサブを配置**（移動扱い）: 本体から同一パッケージパスで `src/test/java/...` に配置
3. **Excelベース + 補助を配置**（コピー）: 対応するExcelベースと補助クラスを配置
4. **単独実行**: `mvn test -Dtest=<YAMLサブ名>`
5. **不足補充**: 失敗した場合は `.xls`・`unit-test*.xml`・設定等を本体からコピーし再実行
6. **緑確認後、次タスクへ**

| タスク | YAMLサブ（移動） | 対応Excelベース（コピー） | 補助（コピー） |
|---|---|---|---|
| #3 | TestSupportYamlTest (#128) | TestSupportTest (#21) | Trap (#22) |
| #4 | MultiResourceDataSetUpYamlTest (#129) | MultiResourceDataSetUpTest (#23) | — |
| #5 | BatchRequestTestSupportYamlTest (#130) | BatchRequestTestSupportTest (#24) | — |
| #6 | DBtoDBBatchSampleYamlTest (#131) | DBtoDBBatchSample (#25), DBtoDBBatchSampleTest (#26) | — |
| #7 | FileToFileBatchSampleYamlTest (#132) | FileToFileBatchSampleTest (#27) | SimpleWriter (#42) |
| #8 | SimpleBatchSampleYamlTest (#133) | SimpleBatchSample (#28), SimpleBatchSampleTest (#29) | — |
| #9 | DbAccessTestSupportYamlTest (#134) | DbAccessTestSupportTest (#30) | HogeTable (#32), HogeTableSsdMaster (#33), TableDataSorterTest (#34), TestTable (#35) |
| #10 | EntityTestSupportYamlTest (#135) | EntityTestSupportTest (#31) | HogeTable (#32), TestTable (#35) |
| #11 | TestBeanYamlTest (#136) | TestBeanTest (#37) | TestBean (#36) |
| #12 | TestEntityYamlTest (#137) | TestEntityTest (#39) | TestEntity (#38) |
| #13 | FileSupportWithDbLessTestDataParserYamlTest (#138) | FileSupportWithDbLessTestDataParserTest (#41) | SimpleWriter (#42) |
| #14 | FileSupportYamlTest (#139) | FileSupportTest (#40) | SimpleWriter (#42) |
| #15 | AbstractHttpRequestTestTemplateTest2YamlTest (#140) | AbstractHttpRequestTestTemplateTest2 (#44) | MockHttpRequestTestTemplate (#45) |
| #16 | AbstractHttpRequestTestTemplateYamlTest (#141) | AbstractHttpRequestTestTemplateTest (#43) | MockHttpRequestTestTemplate (#45) |
| #17 | MessagingReceiveTestSupportYamlTest (#142) | MessagingReceiveTestSupportTest (#46) | — |
| #18 | MessagingRequestTestSupportYamlTest (#143) | MessagingRequestTestSupportTest (#47) | — |
| #19 | RequestTestingMessagingClientYamlTest (#144) | RequestTestingMessagingClientTest (#48) | — |
| #20 | RequestTestingMessagingContextYamlTest (#145) | RequestTestingMessagingContextTest (#49) | — |
| #21 | RequestTestingSendSyncBatchYamlTest (#146) | RequestTestingSendSyncBatchTest (#50) | — |
| #22 | RequestTestingSendSyncSupportYamlTest (#147) | RequestTestingSendSyncSupportTest (#51) | — |

各タスクの **Completion criteria**:
- YAMLサブ・ExcelベースがそれぞれのパスにKに配置されている
- `mvn test -Dtest=<YAMLサブ名>` が BUILD SUCCESS
- テスト結果のログ（Tests run / Failures / Errors / Skipped）を提示

## Acceptance criteria

- YAMLサブ20クラスすべてが単独実行で BUILD SUCCESS
- コピー分は本体現ブランチとバイト照合済み（差分ゼロ）
- push はユーザー承認後のみ実施

## State

<!--
last_completed: #22
next_task: done
notes: 全22タスク完了。YAMLサブ20クラスすべて BUILD SUCCESS 確認済み。
-->
