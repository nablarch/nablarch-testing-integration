# Step 4-08 再検証レポート — 修正後モジュールでの結合テスト

実施日: 2026-08-31 / 実行者: 担当CC

## 1. 結論

**赤。`Tests run: 546, Failures: 0, Errors: 7, Skipped: 18` / BUILD FAILURE。**

Errors 7件はすべて `nablarch.test.core.http.AbstractHttpRequestTestTemplateYamlTest`（YAML 版）1クラスに集中し、
例外は全件同一（`java.lang.IllegalArgumentException: Request parameter is not defined or request parameter list size is invalid.case no = [1]`）。
分類は全件 **(a) モジュールの是正起因**（converter `1915207`）。Failures は 0件。

Excel 版の同一テストクラス `AbstractHttpRequestTestTemplateTest` は `Tests run: 22, Failures: 0, Errors: 0` で緑。
YAML 版だけが割れている。

件数の内訳は 2026-06-25 基準（546 / 0 / 0 / 18）に対し、**総数 546・Skipped 18 は同一、Errors のみ 0 → 7**。

## 2. 使った jar の証拠

### 2-1. 本体 nablarch-testing（取り直し不要と判定）

指示書 1-2 の確認コマンドの実測結果:

```
$ javap -p -classpath ~/.m2/.../nablarch-testing-6-NEXT-SNAPSHOT.jar \
    nablarch.test.core.reader.TestDataParsingTemplate | grep -c "cachedParse\|tryLoadFromCache\|storeToCache"
3

$ unzip -p ~/.m2/.../nablarch-testing-6-NEXT-SNAPSHOT.jar META-INF/MANIFEST.MF | grep Build-Jdk
Build-Jdk: 17.0.19

$ ls -l --time-style=full-iso ~/.m2/.../nablarch-testing-6-NEXT-SNAPSHOT.jar
-rw-r--r-- 1 tie303177 tie303177 513063 2026-08-21 18:28:54.508000000 +0900
```

`cachedParse` 等 3メソッドが存在するため PR ブランチ `convert-testdata-excel-to-text` 由来。取り直しは行っていない。

### 2-2. yaml・converter（GitHub から新規 clone → install）

clone 先: `~/work/nablarch/tmp-step4-08/`（既存作業ツリーには触れていない）

| モジュール | ブランチ | clone HEAD | ピン | 一致 |
|---|---|---|---|---|
| nablarch-testing-yaml | `feature/ntf-yaml` | `4837713c2dd954f5426497b1b32355ac0fb713d9` | `4837713` | ○ |
| nablarch-testing-converter | `ntf-test-data-converter` | `a5f006c40114808fdeb00c36b02a60cc81ffc190` | `a5f006c` | ○ |

install は `JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn -B clean install` を yaml → converter の順で実行。
両方とも BUILD SUCCESS・テスト全緑（yaml `Tests run: 320, Failures: 0, Errors: 0, Skipped: 0` /
converter `Tests run: 682, Failures: 0, Errors: 0, Skipped: 0`）。

install 後の `~/.m2` の jar:

```
-rw-r--r-- 37569  2026-08-31 10:32:56.556000000 +0900  nablarch-testing-yaml-1.0.0-SNAPSHOT.jar        Build-Jdk: 17.0.19
-rw-r--r-- 120497 2026-08-31 10:33:42.976000000 +0900  nablarch-testing-converter-1.0.0-SNAPSHOT.jar   Build-Jdk: 17.0.19
```

converter jar は再検証当日（2026-08-31 10:33）の install に置き換わっており、
指示書が指摘した 2026-08-26 22:14 の古い版ではない。

## 3. Surefire summary（逐語）

`mvn -B clean test` の `[INFO] Results:` ブロックをそのまま引用する。

```
[INFO] Results:
[INFO] 
[ERROR] Errors: 
[ERROR]   AbstractHttpRequestTestTemplateYamlTest>AbstractHttpRequestTestTemplateTest.testAssertSqlResultSet:867 » IllegalArgument
[ERROR]   AbstractHttpRequestTestTemplateYamlTest>AbstractHttpRequestTestTemplateTest.testAssertTablesCRLF:891 » IllegalArgument
[ERROR]   AbstractHttpRequestTestTemplateYamlTest>AbstractHttpRequestTestTemplateTest.testCookieNormal:603 » IllegalArgument
[ERROR]   AbstractHttpRequestTestTemplateYamlTest>AbstractHttpRequestTestTemplateTest.testHttpMethod:677 » IllegalArgument
[ERROR]   AbstractHttpRequestTestTemplateYamlTest>AbstractHttpRequestTestTemplateTest.testIgnoreMessageIdAssertion:811 » IllegalArgument
[ERROR]   AbstractHttpRequestTestTemplateYamlTest>AbstractHttpRequestTestTemplateTest.testIgnoreRequestScopeAssertion:831 » IllegalArgument
[ERROR]   AbstractHttpRequestTestTemplateYamlTest>AbstractHttpRequestTestTemplateTest.testQueryParamsNormal:736 » IllegalArgument
[INFO] 
[ERROR] Tests run: 546, Failures: 0, Errors: 7, Skipped: 18
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD FAILURE
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  03:29 min
[INFO] Finished at: 2026-08-31T10:37:36+09:00
[INFO] ------------------------------------------------------------------------
```

## 4. Failures / Errors の全件分類

Failures: 0件。Errors: 7件（下表）。**7件とも同一原因の同一系統**であり、分類・根拠は全件共通。

| # | テスト | 分類 | 例外メッセージ |
|---|---|---|---|
| 1 | `AbstractHttpRequestTestTemplateYamlTest#testAssertSqlResultSet` | (a) | `IllegalArgumentException: Request parameter is not defined or request parameter list size is invalid.case no = [1]` |
| 2 | `AbstractHttpRequestTestTemplateYamlTest#testAssertTablesCRLF` | (a) | 同上 |
| 3 | `AbstractHttpRequestTestTemplateYamlTest#testCookieNormal` | (a) | 同上 |
| 4 | `AbstractHttpRequestTestTemplateYamlTest#testHttpMethod` | (a) | 同上 |
| 5 | `AbstractHttpRequestTestTemplateYamlTest#testIgnoreMessageIdAssertion` | (a) | 同上 |
| 6 | `AbstractHttpRequestTestTemplateYamlTest#testIgnoreRequestScopeAssertion` | (a) | 同上 |
| 7 | `AbstractHttpRequestTestTemplateYamlTest#testQueryParamsNormal` | (a) | 同上 |

### 4-1. 共通の見立て（根拠付き）

**converter が「マーカーカラムだけを持つブロック」の行を 0 行に落とすようになったため、
`requestParams` の LIST_MAP が YAML では空になり、本体が位置インデックスとして数える件数が失われた。**

証拠を上流から順に示す。

**(1) 変換元 Excel — `requestParams` はマーカーカラム `[no]` 1列のみ・データ4行**

`src/test/java/nablarch/test/core/http/AbstractHttpRequestTestTemplateTest.xls` シート `testCookieNormal`
（Apache POI で全セルを実ダンプ。行番号は 0 起点）:

```
23: [LIST_MAP=requestParams]
24: [[no]]
25: [1]
26: [2]
27: [3]
28: [4]
```

ヘッダ行 24 は `[no]` のみ。`[...]` で囲まれたカラムはフレームワーク用のマーカーカラムであり、
データカラムは 0 件。その下にデータ行が 4 行ある。

**(2) 変換後 YAML — 行が全部消えている**

`target/generated-test-yaml/nablarch/test/core/http/AbstractHttpRequestTestTemplateYamlTest/testCookieNormal.yaml`:

```yaml
  - id: "requestParams"
    rows: []
```

Excel の 4 行が 0 行になっている。

**(3) 落としている実装 — converter `XlsFormatReader#rowCount`**

`nablarch-testing-converter` `src/main/java/nablarch/test/tool/converter/xls/XlsFormatReader.java:619`
（ピン `a5f006c` 時点）:

```java
    private static int rowCount(List<String> columnNames, int rowCount) {
        return columnNames.isEmpty() ? 0 : rowCount;
    }
```

`columnNames` はマーカーカラムを除いたカラム名。`requestParams` は `[no]` しか持たないため
`columnNames` が空となり、フレームワークが返した行数 4 が 0 に潰される。

導入コミット: `1915207`「test: complete task #41 — 2-2 マーカーカラムだけに値があるエントリを残す」
（2026-08-29 16:52:59 +0900、`ntf-test-data-converter`）。
同 Javadoc（`XlsFormatReader.java:602-605`）は
「値が何であっても、フィールドを 1 つも持たないエントリは記法として意味を持たない」を根拠に置いている。

**(4) 本体はその件数を位置インデックスとして使っている**

`nablarch-testing` `nablarch/test/core/http/TestCaseInfo.java:344-351`
（`~/.m2` の `nablarch-testing-6-NEXT-SNAPSHOT-sources.jar` を展開して確認）:

```java
        int caseNo = Integer.parseInt(getTestCaseNo());
        if (request.size() < caseNo) {
            throw new IllegalArgumentException(Builder.concat(
                    "Request parameter is not defined or request parameter list size is invalid.",
                    "case no = [" + caseNo + "]"));
        }

        return request.get(caseNo - 1);
```

`request`（= `requestParams` の LIST_MAP）は `AbstractHttpRequestTestTemplate.java:392` で
`TestCaseInfo` に渡され、**ケース番号でのランダムアクセスに使われる**。
フィールドを 1 つも持たないエントリであっても、「ケース no に対応する位置」という意味を持っている。
Excel 経路では 4 エントリが残るので `request.size()=4 >= caseNo` が成立し緑。
YAML 経路では 0 エントリなので `0 < 1` となり全ケースで例外。

したがって **(3) の Javadoc が置いた前提「フィールドを1つも持たないエントリは記法として意味を持たない」は、
本体 `TestCaseInfo#getRequestParameters` の実装と矛盾する。**

**(5) 影響範囲 — 同クラス内で `rows: []` になったシートは10件、うち割れたのは7件**

変換後 YAML で `requestParams` が `rows: []` になったシート:
`testAssertSqlResultSet` / `testAssertTablesCRLF` / `testCookieFailed` / `testCookieNormal` /
`testHttpMethod` / `testIgnoreMessageIdAssertion` / `testIgnoreRequestScopeAssertion` /
`testQueryParamsFailed` / `testQueryParamsNormal` / `testSessionScopedVarOverwrite`。

このうち `testCookieFailed` と `testQueryParamsFailed` は、`getRequestParameters()` に到達する前に
Cookie / クエリパラメータの LIST_MAP 未発見で例外になることを期待するテスト
（`AbstractHttpRequestTestTemplateTest.java:654-661` / `786-793` が `Cookie LIST_MAP was not found. name = [cookie0]` /
`Query parameter LIST_MAP was not found. name = [queryParams0]` を assert）であるため、この変化の影響を受けず緑のまま。

**(6) 他モジュールの単体テストでは検知できていない**

converter は `Tests run: 682` 全緑、yaml は `Tests run: 320` 全緑。
`1915207` は同時に `XlsMarkerOnlyEntryTest` を新設しているが、その期待値は
「本体が読む3件と一致」というコミットメッセージの記述にあるとおり converter 側の想定に閉じており、
`TestCaseInfo#getRequestParameters` の位置インデックス用法は検証範囲に入っていない。
結合テストで初めて表面化した性質の乖離である。

## 5. Skipped 全件（18件）

2026-06-25 基準の 18件と**同数**。内訳は下表のとおりで、`@Ignore` 8件・`Assume` 相当 10件。

| # | テスト | 由来 | 理由 |
|---|---|---|---|
| 1 | `messaging.RequestTestingMessagingClientTest#testAssertFailNoMatchHeader` | `Assume` | `Assume.assumeThat(System.getProperty("java.specification.version"), anyOf(is("1.6"), is("1.7")))`（`RequestTestingMessagingClientTest.java:438`）。実行 JDK は 17 |
| 2 | `messaging.RequestTestingMessagingClientTest#testAssertFailNoMatchBody` | `Assume` | 同上 |
| 3 | `messaging.RequestTestingMessagingClientYamlTest#testAssertFailNoMatchHeader` | `Assume` | 同上（YAML 版は同メソッドを継承） |
| 4 | `messaging.RequestTestingMessagingClientYamlTest#testAssertFailNoMatchBody` | `Assume` | 同上 |
| 5 | `db.DbAccessTestSupportTest#testSetUpDbOnInvalidExcel_Oracle` | `@TargetDb`（Assume 相当・DB 種別フィルタ） | `@TargetDb(include = TargetDb.Db.ORACLE)`（`DbAccessTestSupportTest.java:277`）。実行 DB は H2 |
| 6 | `db.DbAccessTestSupportTest#testSetUpDbOnInvalidExcel_Postgres` | 同上 | `@TargetDb(include = TargetDb.Db.POSTGRE_SQL)`（同 `:311`） |
| 7 | `db.DbAccessTestSupportYamlTest#testSetUpDbOnInvalidExcel_Oracle` | 同上 | 同上（YAML 版は同メソッドを継承） |
| 8 | `db.DbAccessTestSupportYamlTest#testSetUpDbOnInvalidExcel_Postgres` | 同上 | 同上 |
| 9 | `TestSupportYamlTest#testGetPathOf` | `@Ignore` | 「パス解決ロジックの単体テスト。形式非依存のため YAML 版対象外」（`TestSupportYamlTest.java:23`） |
| 10 | `TestSupportYamlTest#testGetPathResourceExisting` | `@Ignore` | 同上（`TestSupportYamlTest.java:26`） |
| 11 | `file.FileSupportYamlTest#testSetUpFixedWithDuplicateName` | `@Ignore` | 「異常系データは変換不可。固定長の重複名→本体YAML経路の弾きは `YamlFileBuilderTest#testBuildFileList_fixedDuplicateFieldNameThrowsException` で担保」（`FileSupportYamlTest.java:24`） |
| 12 | `file.FileSupportYamlTest#testSetUpVariableWithDuplicateName` | `@Ignore` | 同系（可変長） |
| 13 | `file.FileSupportYamlTest#testAssertFixedWithDuplicateName` | `@Ignore` | 同系（固定長） |
| 14 | `file.FileSupportYamlTest#testAssertVariableWithDuplicateName` | `@Ignore` | 同系（可変長） |
| 15 | `file.FileSupportWithDbLessTestDataParserYamlTest#testSetUpFixedWithDuplicateName` | `@Ignore` | 同系 |
| 16 | `file.FileSupportWithDbLessTestDataParserYamlTest#testSetUpVariableWithDuplicateName` | `@Ignore` | 同系 |
| 17 | `file.FileSupportWithDbLessTestDataParserYamlTest#testAssertFixedWithDuplicateName` | `@Ignore` | 同系 |
| 18 | `file.FileSupportWithDbLessTestDataParserYamlTest#testAssertVariableWithDuplicateName` | `@Ignore` | 同系 |

2026-06-25 基準（Skipped 18）との件数差は **0**。増減なし。

## 6. 判断を仰ぐ事項

指示書 2 に従い、**モジュール・integration ともに一切変更していない**。修正案は実施せず、判断を仰ぐ。

### 事項1: `XlsFormatReader#rowCount` の「カラム名 0 件 → 0 行」を維持するか

- 現状: converter は「フィールドを 1 つも持たないエントリは記法として意味を持たない」として行を落とす（`XlsFormatReader.java:619`、`1915207`）
- 実測との矛盾: 本体 `TestCaseInfo#getRequestParameters`（`TestCaseInfo.java:344-351`）は
  `requestParams` を **ケース no による位置インデックス**として使うため、フィールド 0 件のエントリにも
  「その位置が存在する」という意味がある
- 選択肢
  - **(A) converter を戻す/条件を緩める** — マーカーカラムのみのブロックでも行数を保つ。
    実 Excel（`AbstractHttpRequestTestTemplateTest.xls`）が現に依存している記法なので、
    Excel→YAML 等価性という本移行の目的には最も素直
  - (B) 本体 `TestCaseInfo` 側を変える — `request.size() < caseNo` の扱いを緩める。
    ただし本体は今回の変更対象外であり、影響範囲が読めない
  - (C) integration のフィクスチャ（Excel）を変える — `requestParams` に実データカラムを足す。
    「既存 Excel をそのまま変換できる」という前提を崩すため推奨しない
- **推奨は (A)。** 理由: 割れているのは converter の出力であって、Excel も本体も従来どおりの記法・実装のまま。
  等価性を壊した側を戻すのが筋であり、(B)(C) は本移行の前提（既存資産をそのまま変換する）を削る

### 事項2: converter 側にこの用法の回帰テストを足すか

`1915207` が新設した `XlsMarkerOnlyEntryTest` は converter 内部の想定に閉じており、
本体の位置インデックス用法を突いていない。事項1 をどう決めるにせよ、
「マーカーカラムのみのブロックの行数」が本体挙動と結びついていることを converter 側の
テストで固定しておくか、判断を仰ぐ。

## 7. 実行条件

| 項目 | 値 |
|---|---|
| 作業場 | `/home/tie303177/work/nablarch/nablarch-testing-integration` |
| ブランチ / HEAD | `feature/migrate-integration-test` / `8d92f7c8088fbc919e50ab8b6325960379621b46`（remote 先端と一致） |
| JAVA_HOME | `/usr/lib/jvm/temurin-17-jdk-amd64` |
| コマンド | `JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn -B clean test` |
| surefire 設定 | pom のまま（`reuseForks=false` / `forkCount=1`）。変更なし |
| 所要 | 03:29 min（2026-08-31T10:37:36+09:00 終了） |

テスト実行で `work/` 配下に生成・削除される作業ファイル（`work/input.txt` 等）は
`git checkout -- work/` と生成物削除で復元済み。`git status --short` は空。

---

# 再実行（#54 追随後）— 2026-08-31

## R-1. 結論

**全緑。`Tests run: 546, Failures: 0, Errors: 0, Skipped: 18` / BUILD SUCCESS。**
2026-06-25 基準（`69125c3`）と件数が完全一致し、初回再検証で出た Errors 7件は解消した。
Failures / Errors は **0件**。モジュール・integration とも一切変更していない。

初回再検証で報告した原因（converter がマーカーカラムだけのブロックの行を落とす）は、
converter の `ce86a6d`「feat: カラム名の行がマーカーカラムだけのブロックを、名前と値を保って読む（辺①）」
（`src/main/java/nablarch/test/tool/converter/xls/XlsFormatReader.java` ほか 10 ファイル・+533/−59）
および `cd83fd2`（辺②・YAML 読み側の対称化）で是正されている。

## R-2. 使った jar の証拠

### R-2-1. 本体 nablarch-testing（取り直し不要と判定）

```
$ javap -p -classpath ~/.m2/repository/com/nablarch/framework/nablarch-testing/6-NEXT-SNAPSHOT/nablarch-testing-6-NEXT-SNAPSHOT.jar \
    nablarch.test.core.reader.TestDataParsingTemplate | grep -c "cachedParse\|tryLoadFromCache\|storeToCache"
3

$ unzip -p ...nablarch-testing-6-NEXT-SNAPSHOT.jar META-INF/MANIFEST.MF | grep Build-Jdk
Build-Jdk: 17.0.19

$ ls -l --time-style=long-iso ...nablarch-testing-6-NEXT-SNAPSHOT.jar
-rw-r--r-- 1 tie303177 tie303177 513063 2026-08-21 18:28 .../nablarch-testing-6-NEXT-SNAPSHOT.jar
```

3メソッドが揃うため PR ブランチ由来。指示書 §1-2 の判定基準どおり取り直し不要。

### R-2-2. yaml・converter（GitHub から新規 clone → install）

clone 先: `~/work/nablarch/tmp-step4-08/`（既存の他担当作業ツリーには触れていない）

| モジュール | clone 時 HEAD | install 対象 | ピン | 一致 |
|---|---|---|---|---|
| nablarch-testing-yaml (`feature/ntf-yaml`) | `6175639` | `git checkout 4837713` 実施後 `4837713c2dd954f5426497b1b32355ac0fb713d9` | `4837713` | ○ |
| nablarch-testing-converter (`ntf-test-data-converter`) | `9ab66481ba9deecda641d5224637e2e884ec7503` | 同左（checkout 不要） | `9ab6648` | ○ |

yaml の remote 先端は指示書 §5-1 が記した `2b35561` からさらに `6175639` へ進んでいたが、
§5-1 の指示どおり `4837713` を checkout してから install したため、検証対象はピンどおりである。

install（`JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn -B clean install`、yaml → converter の順）:

- yaml: BUILD SUCCESS（`Total time: 19.389 s` / `Finished at: 2026-08-31T15:37:43+09:00`）
- converter: BUILD SUCCESS（`Tests run: 731, Failures: 0, Errors: 0, Skipped: 0` / `Total time: 43.955 s` / `Finished at: 2026-08-31T15:38:42+09:00`）

install 後の `~/.m2`:

```
-rw-r--r-- 1 tie303177 tie303177 122508 2026-08-31 15:38 .../nablarch-testing-converter-1.0.0-SNAPSHOT.jar
-rw-r--r-- 1 tie303177 tie303177  37570 2026-08-31 15:37 .../nablarch-testing-yaml-1.0.0-SNAPSHOT.jar
```

両 jar の MANIFEST とも `Build-Jdk: 17.0.19`。

## R-3. Surefire summary（逐語）

```
[INFO] 
[INFO] Results:
[INFO] 
[WARNING] Tests run: 546, Failures: 0, Errors: 0, Skipped: 18
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  03:18 min
[INFO] Finished at: 2026-08-31T15:42:21+09:00
[INFO] ------------------------------------------------------------------------
```

## R-4. Failures / Errors の全件分類

**0件。** 初回再検証で (a) モジュールの是正起因に分類した
`AbstractHttpRequestTestTemplateYamlTest` の Errors 7件は解消した。
本再実行では同クラスが `Tests run: 22, Failures: 0, Errors: 0, Skipped: 0`（`Time elapsed: 5.645 s`）である。

## R-5. Skipped 全件（18件）

**「5. Skipped 全件（18件）」の表と同一の18件**（surefire XML から再抽出して照合済み。クラス・メソッド名・件数とも一致）。
2026-06-25 基準（Skipped 18）との件数差は **0**。

由来の内訳（surefire XML の skipped message とソースを突き合わせて再確認）:

- `Assume` 4件（messaging）: skipped message は `got: "17", expected: (is "1.6" or is "1.7")`。
  `RequestTestingMessagingClientTest.java:438` / `:479` の
  `Assume.assumeThat(System.getProperty("java.specification.version"), anyOf(is("1.6"), is("1.7")))`。実行 JDK は 17。
  YAML 版 `RequestTestingMessagingClientYamlTest` は同メソッドを継承（前掲表 #1–#4）
- `@TargetDb`（Assume 相当）4件（db）: skipped message は空。
  `DbAccessTestSupportTest.java:277`（`include = TargetDb.Db.ORACLE`）・`:311`（`include = TargetDb.Db.POSTGRE_SQL`）。
  実行 DB は H2。YAML 版は同メソッドを継承（前掲表 #5–#8）
- `@Ignore` 10件: `TestSupportYamlTest.java:23` / `:26`（2件）、`FileSupportYamlTest.java`（4件・`:24` ほか）、
  `FileSupportWithDbLessTestDataParserYamlTest`（4件）。
  各 `@Ignore` の理由文字列は surefire の skipped message と一致（前掲表 #9–#18）

**前掲表の集計行の訂正**: 「5. Skipped 全件（18件）」冒頭は「`@Ignore` 8件・`Assume` 相当 10件」と書いているが、
同表の行を数えると `@Ignore` が #9–#18 の 10件、`Assume` 相当（`Assume` 4件 + `@TargetDb` 4件）が 8件である。
表の各行の分類は正しく、集計行の 8/10 が入れ替わっている。表本体は時点の証拠として書き換えず、ここで訂正する。

## R-6. 判断を仰ぐ事項

**なし。** 初回再検証の「事項1（`rowCount` を戻すか）」は行を保つ方向で決着済み、
「事項2（converter 側の回帰テスト）」は converter `ce86a6d` が `XlsMarkerOnlyBlockTest`（255行・新設）を
追加して担保しており、本結合テストでも全緑を確認した。

## R-7. 実行条件

| 項目 | 値 |
|---|---|
| 作業場 | `/home/tie303177/work/nablarch/nablarch-testing-integration` |
| ブランチ / HEAD | `feature/migrate-integration-test` / `e276792b30675e3b282b4381c1de913277ee928e`（remote 先端と一致） |
| JAVA_HOME | `/usr/lib/jvm/temurin-17-jdk-amd64` |
| コマンド | `JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn -B clean test` |
| surefire 設定 | pom のまま（`reuseForks=false` / `forkCount=1`）。変更なし |
| 所要 | 03:18 min（2026-08-31T15:42:21+09:00 終了） |

テスト実行で `work/` 配下に生成・削除される作業ファイルは `git checkout -- work/` と生成物削除で復元済み。
`git status --short` は空。

---

# 再実行（yaml `#51`・converter `#57` 追随後）— 2026-09-07

由来: `/home/tie303177/work/cowork/nablarch/ntf-doc-renewal/指示/ntf-step4-18-schema-excel-parity.md` §5（台帳 `#26`）

## S-1. 結論

**全緑。`Tests run: 546, Failures: 0, Errors: 0, Skipped: 18` / BUILD SUCCESS。**
2026-06-25 基準（`69125c3`）および前節「再実行（#54 追随後）」と件数が完全一致する。
Failures / Errors は **0件**。モジュール・integration とも一切変更していない。

## S-2. 使った jar の証拠

### S-2-1. 本体 nablarch-testing（取り直し不要と判定）

```
$ javap -p -classpath ~/.m2/repository/com/nablarch/framework/nablarch-testing/6-NEXT-SNAPSHOT/nablarch-testing-6-NEXT-SNAPSHOT.jar \
    nablarch.test.core.reader.TestDataParsingTemplate | grep -c "cachedParse\|tryLoadFromCache\|storeToCache"
3

$ unzip -p ...nablarch-testing-6-NEXT-SNAPSHOT.jar META-INF/MANIFEST.MF | grep Build-Jdk
Build-Jdk: 17.0.19

$ ls -l --time-style=long-iso ...nablarch-testing-6-NEXT-SNAPSHOT.jar
-rw-r--r-- 1 tie303177 tie303177 513063 2026-08-21 18:28 .../nablarch-testing-6-NEXT-SNAPSHOT.jar
```

3メソッドが揃うため PR ブランチ由来。取り直しは行っていない（前節と同一の jar）。

### S-2-2. yaml・converter（既存作業ツリーを install）

本節では検証用 clone を作らず、既存作業ツリーをそのまま使った。いずれも install 直前に
`git status --short` が空・`git rev-parse HEAD` が remote 先端と一致することを確認済み。

| モジュール | 作業ツリー | ブランチ | HEAD | remote 先端と一致 |
|---|---|---|---|---|
| nablarch-testing-yaml | `~/work/nablarch/nablarch-testing-yaml` | `feature/ntf-yaml` | `a404126d4c25bf568916d80d453ab557536946eb` | ○（`origin/feature/ntf-yaml`） |
| nablarch-testing-converter | `~/work/nablarch/nablarch-testing-converter` | `ntf-test-data-converter` | `8e4410cb1013866e2e3ba96ec7a344fe5febda97` | ○（`origin/ntf-test-data-converter`） |

install（JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64）:

- yaml: `mvn -B -DskipTests clean install` → BUILD SUCCESS（`Total time: 04:23 min` / `Finished at: 2026-09-07T21:29:18+09:00`）
- converter: `mvn -B clean install` → BUILD SUCCESS（`Total time: 04:43 min` / `Finished at: 2026-09-07T21:34:21+09:00`）。
  surefire XML から集計した converter の単体テストは `Tests run: 732, Failures: 0, Errors: 0, Skipped: 0`

install 後の `~/.m2`:

```
-rw-r--r-- 1 tie303177 tie303177  38592 2026-09-07 21:29 .../nablarch-testing-yaml-1.0.0-SNAPSHOT.jar
-rw-r--r-- 1 tie303177 tie303177 122569 2026-09-07 21:34 .../nablarch-testing-converter-1.0.0-SNAPSHOT.jar
```

yaml jar の MANIFEST は `Build-Jdk: 17.0.19`。

### S-2-3. スキーマの是正が jar に入っていることの確認

install 前の jar（2026-09-07 16:56 の版）は `$defs.record_fragment.properties.rows` に `"minItems": 1` を持っていた。
install 後の jar を展開して JSON をパースした結果:

```
$ unzip -p ...nablarch-testing-yaml-1.0.0-SNAPSHOT.jar nablarch/test/ntf-testdata-yaml-schema.json | \
    python3 -c '...json.load(...)["$defs"]["record_fragment"]["properties"]["rows"]...'
rows keys: ['type', 'description', 'items']
minItems in rows: False
```

`minItems` は無い。指示書 §5-1 の確認条件を満たす。

## S-3. Surefire summary（逐語）

```
[INFO] Results:
[INFO] 
[WARNING] Tests run: 546, Failures: 0, Errors: 0, Skipped: 18
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  07:48 min
[INFO] Finished at: 2026-09-07T21:42:33+09:00
[INFO] ------------------------------------------------------------------------
```

## S-4. Failures / Errors の全件分類

**0件。** 分類対象なし。
`#24` で Errors 7件が集中した `AbstractHttpRequestTestTemplateYamlTest` は本再実行でも
`Tests run: 22, Failures: 0, Errors: 0, Skipped: 0`（`Time elapsed: 5.093 s`）で緑。

## S-5. Skipped 全件（18件）

surefire XML から抽出した18件（クラス名昇順）。**前節「再実行（#54 追随後）」の18件と完全一致**
（クラス・メソッド名・件数とも同一）。2026-06-25 基準（Skipped 18）との件数差は **0**。

| # | テスト | 由来 |
|---|---|---|
| 1 | `nablarch.test.TestSupportYamlTest#testGetPathOf` | `@Ignore` |
| 2 | `nablarch.test.TestSupportYamlTest#testGetPathResourceExisting` | `@Ignore` |
| 3 | `nablarch.test.core.db.DbAccessTestSupportTest#testSetUpDbOnInvalidExcel_Oracle` | `@TargetDb`（Assume 相当） |
| 4 | `nablarch.test.core.db.DbAccessTestSupportTest#testSetUpDbOnInvalidExcel_Postgres` | `@TargetDb`（Assume 相当） |
| 5 | `nablarch.test.core.db.DbAccessTestSupportYamlTest#testSetUpDbOnInvalidExcel_Oracle` | `@TargetDb`（Assume 相当） |
| 6 | `nablarch.test.core.db.DbAccessTestSupportYamlTest#testSetUpDbOnInvalidExcel_Postgres` | `@TargetDb`（Assume 相当） |
| 7 | `nablarch.test.core.file.FileSupportWithDbLessTestDataParserYamlTest#testAssertFixedWithDuplicateName` | `@Ignore` |
| 8 | `nablarch.test.core.file.FileSupportWithDbLessTestDataParserYamlTest#testAssertVariableWithDuplicateName` | `@Ignore` |
| 9 | `nablarch.test.core.file.FileSupportWithDbLessTestDataParserYamlTest#testSetUpFixedWithDuplicateName` | `@Ignore` |
| 10 | `nablarch.test.core.file.FileSupportWithDbLessTestDataParserYamlTest#testSetUpVariableWithDuplicateName` | `@Ignore` |
| 11 | `nablarch.test.core.file.FileSupportYamlTest#testAssertFixedWithDuplicateName` | `@Ignore` |
| 12 | `nablarch.test.core.file.FileSupportYamlTest#testAssertVariableWithDuplicateName` | `@Ignore` |
| 13 | `nablarch.test.core.file.FileSupportYamlTest#testSetUpFixedWithDuplicateName` | `@Ignore` |
| 14 | `nablarch.test.core.file.FileSupportYamlTest#testSetUpVariableWithDuplicateName` | `@Ignore` |
| 15 | `nablarch.test.core.messaging.RequestTestingMessagingClientTest#testAssertFailNoMatchBody` | `Assume`（JDK 1.6/1.7 限定） |
| 16 | `nablarch.test.core.messaging.RequestTestingMessagingClientTest#testAssertFailNoMatchHeader` | `Assume`（JDK 1.6/1.7 限定） |
| 17 | `nablarch.test.core.messaging.RequestTestingMessagingClientYamlTest#testAssertFailNoMatchBody` | `Assume`（JDK 1.6/1.7 限定） |
| 18 | `nablarch.test.core.messaging.RequestTestingMessagingClientYamlTest#testAssertFailNoMatchHeader` | `Assume`（JDK 1.6/1.7 限定） |

内訳: `@Ignore` 10件 / `Assume` 相当 8件（`Assume` 4件 + `@TargetDb` 4件）。各由来の根拠 `file:line` は
前節「R-5」および「5. Skipped 全件（18件）」の表に記載したものと同一。

## S-6. 判断を仰ぐ事項

**なし。**

## S-7. 実行条件

| 項目 | 値 |
|---|---|
| 作業場 | `/home/tie303177/work/nablarch/nablarch-testing-integration` |
| ブランチ / HEAD | `feature/migrate-integration-test` / `d2353b7`（テスト実行時点） |
| JAVA_HOME | `/usr/lib/jvm/temurin-17-jdk-amd64` |
| コマンド | `JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn -B clean test` |
| surefire 設定 | pom のまま（`reuseForks=false` / `forkCount=1`）。変更なし |
| 所要 | 07:48 min（2026-09-07T21:42:33+09:00 終了） |

テスト実行で生成・削除される作業ファイル（`work/` 配下、`tmp/`）は
`git checkout -- work/` と生成物削除で復元済み。`git status --short` は空。
