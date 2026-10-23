Rn version: 0.8.0

# Goal

`nablarch-testing`（ブランチ `convert-testdata-excel-to-text`）から integration 対象ファイルを
本リポジトリへ移動・コピーし、YAMLサブ20クラスすべてが `mvn test` で単独実行緑になる状態にする。

# Acceptance criteria

- YAMLサブ20クラスすべてが単独実行で BUILD SUCCESS
- コピー分は本体現ブランチとバイト照合済み（差分ゼロ）
- push はユーザー承認後のみ実施

# Assumptions

- 移動元: `/home/tie303177/work/nablarch/nablarch-testing`（ブランチ `convert-testdata-excel-to-text`）
  - **参照専用。本体への書き込み・削除は一切行わない**
- 移動先: `/home/tie303177/work/nablarch/nablarch-testing-integration`（本リポジトリ）
- 作業ブランチ: `feature/migrate-integration-test`
- yaml・converter は構築済み・`mvn install` 済み（`~/.m2` に配置済み）
- 実行環境: JDK21 / Maven3.9.9 / `ja_JP.UTF-8` / `TZ=Asia/Tokyo`
- DB系テストは H2 共有のため **必ず1クラス単独実行**（`-Dtest=<クラス名>`）
- 正本ファイル一覧: `.rn/migrate-integration-test/repo-split-filelist.md`（#列の番号で参照）
- 作業指示詳細: `.rn/migrate-integration-test/cc-integration-build.md`

# Rules

- commit and push every change; one completion marker per task
- **変更したら必ずプッシュする**（コミット後に `git push` を実行する）
- PR本文はステアリングへのリンクのみとする（内容の重複を防ぐ）
- **移動**（#128–153）: 本体から integration へコピー（本体側は削除しない＝コピー動作）
- **コピー**（#21–51）: 本体に残しつつ integration にも複製
- 変更禁止: 物理コピー／package・import の機械的調整／pom 設定のみ許可
- ロジック・期待値・アサーション変更が要る場合は**手を止めてユーザーに報告**

# Tasks

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
- [x] #23 — Java 17 でコンパイル・テスト・インストール
- [x] #24 — Step 4-08 再検証（修正後 yaml・converter での結合テスト再実行・報告）
- [x] #25 — Step 4-08 再実行（#54 追随後 converter での結合テスト再実行・報告）
- [x] #26 — yaml `#51`・converter `#57` 追随後の結合テスト再実行・報告

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

---

#### #24 Step 4-08 再検証

**Purpose**: 是正後の `nablarch-testing-yaml`（`4837713`）・`nablarch-testing-converter`（`a5f006c`）で
結合テストを一括再実行し、どこが割れるかを観測して報告する。緑化は目的ではない。

**Prerequisites**: #23

**Steps**:
1. 本体 `nablarch-testing` jar が PR ブランチ由来かを javap / MANIFEST で確認
2. yaml・converter を GitHub から新規 clone し、ピン一致を確認のうえ Java 17 で `mvn clean install`
3. `JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn clean test` を実行
4. 落ちた全件に分類（(a) 是正起因 /(b) スキーマ起因 /(c) 環境起因）・根拠 `file:line`・コミットを付す
5. `.rn/step4-08-retest/report.md` に記録し、モジュール・integration とも無変更のまま停止

**Completion criteria**:
- 使った jar の証拠（javap / MANIFEST / clone HEAD / install 後タイムスタンプと Build-Jdk）が報告にある
- Surefire summary が逐語で報告にある
- Skipped 全件が由来（`@Ignore` / `Assume`）付きで列挙され、2026-06-25 基準との件数差が説明されている
- Failures / Errors 全件に分類・根拠・コミットが付いている
- `git status --short` が空

#### #25 Step 4-08 再実行（#54 追随後）

**Purpose**: 解説書の仕様変更 #54「マーカーカラムとその値を保って変換する」へ追随した
`nablarch-testing-converter`（`9ab6648`）で結合テストを一括再実行し、
#24 で観測した Errors 7件が解消したかを確認して報告する。

**Prerequisites**: #24

**Steps**:
1. 本体 `nablarch-testing` jar の PR ブランチ由来判定（javap / MANIFEST）
2. yaml・converter を GitHub から新規 clone。yaml は `4837713` を checkout、converter は HEAD が `9ab6648` であることを確認
3. yaml → converter の順に Java 17 で `mvn clean install`
4. `JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn -B clean test`
5. `.rn/step4-08-retest/report.md` に「再実行（#54 追随後）」の節として追記（既存本文は書き換えない）

**Completion criteria**:
- 使った jar の証拠（javap / MANIFEST / clone HEAD / install 後タイムスタンプと Build-Jdk）が報告にある
- Surefire summary が逐語で報告にある
- Skipped 全件が由来付きで列挙され、2026-06-25 基準との件数差が説明されている
- Failures / Errors 全件に分類・根拠・コミットが付いている（0件なら明記）
- `git status --short` が空

#### #26 yaml `#51`・converter `#57` 追随後の結合テスト再実行

**Purpose**: YAML スキーマの Excel との対称性の是正（`rows: []` を認める／ディレクティブの値を文字列でも書ける）に
追随した yaml・converter で結合テストを一括再実行し、全緑を確認して報告する。

**由来**: `/home/tie303177/work/cowork/nablarch/ntf-doc-renewal/指示/ntf-step4-18-schema-excel-parity.md` §5

**Prerequisites**: #25

**Steps**:
1. `~/work/nablarch/nablarch-testing-yaml`（`feature/ntf-yaml` / `a404126`）を Java 17 で `mvn -DskipTests install`。
   `~/.m2` の jar 内 `nablarch/test/ntf-testdata-yaml-schema.json` の `$defs.record_fragment.properties.rows` に
   `minItems` が無いことを確認
2. `~/work/nablarch/nablarch-testing-converter`（`ntf-test-data-converter` / `8e4410c`）を Java 17 で `mvn clean install`
3. `JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn -B clean test`
4. `.rn/step4-08-retest/report.md` に「再実行（yaml `#51`・converter `#57` 追随後）」の節として追記

**Completion criteria**:
- 使った jar の証拠（javap / MANIFEST / HEAD / install 後タイムスタンプ）が報告にある
- Surefire summary が逐語で報告にある
- Skipped 全件が列挙され、基準との件数差が説明されている
- Failures / Errors 全件に分類・根拠が付いている（0件なら明記）
- `git status --short` が空


**承認**: 2026-09-07、ディレクター（`ntf-doc-renewal-b5`）が `1fcd2f9` を承認。
根拠は独立実測 —— scratchpad に clone（src は `d2353b7` と差分なし）し、yaml `a404126`・
converter `223f954` を install して `mvn clean test` を実行、`546 件 / Failures 0 / Errors 0 / Skipped 18` を確認。
本タスクの報告（`.rn/step4-08-retest/report.md` の S-1・S-3）と一致した。
なお本タスクが install した converter は `8e4410c`。ディレクターの `223f954` は `8e4410c` の後に
`.rn/ntf-test-data-converter/steering.md` だけを変える docs コミット
（`git diff --stat 8e4410c 223f954` は同ファイル1件のみ）であり、`src` に差分はない。

# State

- **Status**: paused
- **Date**: 2026-09-07
- **Last completed**: #26 yaml `#51`（`a404126`）・converter `#57` 追随後の結合テスト再実行（`1fcd2f9`）。
  2026-09-07 にディレクター承認済み（`5d3c573`）
- **Next**: なし（#1–#26 すべて完了・承認済み）。PR #1（→ develop）のマージ待ち
- **Notes**:
  - ブランチ `feature/migrate-integration-test`（remote と一致）。次の具体アクションは PR #1（→ develop）のマージのみ。
  - ブロッカー・未決事項なし。user-deferred な未追跡パスなし。
  - 再実行の証拠（jar・Surefire 逐語・Skipped 全件）は `.rn/step4-08-retest/report.md`。
    再現に使うモジュールは yaml `a404126` / converter `8e4410c`（`223f954` とは docs のみの差）。
