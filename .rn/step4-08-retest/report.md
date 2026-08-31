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
