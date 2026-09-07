# CC 作業指示書 — nablarch-testing-integration 構築

## 0. あなたの役割と絶対ルール

- あなたは **integration 構築 CC**。新リポジトリ `nablarch-testing-integration` を起動ディレクトリとし、本体 `nablarch-testing`（ブランチ `convert-testdata-excel-to-text`）は**参照専用**（読むだけ・一切変更しない）。
- **実装変更は絶対禁止**。許されるのは「物理コピー／物理移動」「package 行・import 行の機械的調整」「pom 設定」のみ。
- ロジック・テスト期待値・アサーションの変更が要ると判断したら、**手を止めてユーザーに報告**。自己判断で直さない。
- **本体からの削除はこの指示の範囲外**。本体は移動元として全件残す。削除は integration 完成後に別 CC が行う。
- 正本は本体の `docs/pr75/divide/repo-split-filelist.md`。対象クラスはこのファイルの分類に従う（コピー31件・移動44件）。

## 1. 前提（環境）

- yaml・converter は完了済み。両者を `mvn install` 済みで `~/.m2` に配置されていること（integration はこれに依存）。
- JDK21 / Maven3.9.9 / locale `ja_JP.UTF-8` / `TZ=Asia/Tokyo`。
- DB系テストは H2 共有のため一括実行不可。**必ず1クラス単独実行**（`-Dtest=<クラス名>`）。

## 2. 進め方（1クラスずつの反復・厳守）

リソースを事前に全列挙しない。**YAMLサブ1クラス＋対応するExcelベース1組ずつ**回す。対応表は正本ファイルおよび引き継ぎ資料の20組表に従う。

各クラスについて以下を順に実行：

1. **静的に辿る**：YAMLサブ → 対応Excelベース → 参照する補助クラス（正本のコピー31件側）。本体 src/main 依存は pom で解決するためコピー不要。
2. **配置**：
   - YAMLサブ（正本 128–147）＝**移動**（本体から integration へ。本体側は触らない＝コピー動作でよい。削除は後工程）。
   - 対応Excelベース＋補助＝**コピー**（本体に残す）。
   - すべて**同一パッケージパス**で `src/test/java/...` に配置。
3. **単独実行**：`mvn test -Dtest=<YAMLサブ名>`。
4. **不足リソース補充**：落ちたら不足する設定・データ（`<ClassName>.xls`、`unit-test*.xml`、正本 154–164 のYAML設定、148 の変換生成データ等）を本体からコピーし再実行。**緑まで。コードは変えない**。
5. 緑を確認 → 次クラスへ。

### 止まって相談する条件

- 不足リソース補充だけでは緑にならない。
- import 調整・pom 設定の範囲を超える変更が必要に見える。
- 期待値／アサーション／ロジックの差異が原因に見える。

→ いずれも**自己判断で直さず、原因の見立てと該当箇所を添えてユーザーに報告**。ユーザーが解決指示を出す。

## 3. 基盤・共通設定（最初に1度だけ）

- 基盤5件（正本 149–153）：`ExcelToYamlEquivalenceTest` / `FormatAwareTestDataParser` / `TestDataFormat` / `YamlSchemaValidationTest` / `YamlModeTestBase` を同一パッケージパスで配置（移動）。
- 共通設定（`unit-test.xml`/`framework.xml`/`convertorSetting.xml`/`override.xml`/`unit-test-yaml.xml` 等）は **integration の共通 resources に1度だけ**。重複配置しない。

## 4. pom（converter 準拠）

- GAV：`com.nablarch.framework:nablarch-testing-integration:1.0.0-SNAPSHOT`、親 `com.nablarch:nablarch-parent:6-NEXT-SNAPSHOT`。
- 依存：`nablarch-testing-yaml:1.0.0-SNAPSHOT`、`nablarch-testing-converter:1.0.0-SNAPSHOT`、`nablarch-testing`（本体）、`poi-ooxml:3.8`、`snakeyaml-engine:3.0.1`、`json-schema-validator:3.0.2`、`junit:4.13.1`。

## 5. 完了条件と報告

- YAMLサブ20クラスすべてが単独実行で緑。
- 報告は「結果＋検証エビデンス」をセットで。**「差分ゼロ」「一致」は鵜呑み禁止**。コピー分は本体現ブランチとバイト照合で裏取り。
- 取りこぼし懸念があれば `strace -ff -e trace=openat` で実読込ファイルを観測し、配置漏れがないか確認。
- 完了したら push せず、緑ログと配置一覧を提示してユーザーの承認を待つ。
