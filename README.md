# nablarch-testing-integration

## このリポジトリの目的

Nablarch Testing Framework（NTF）が対応する **複数のテストデータ形式**（Excel / YAML）と、それらを変換する **nablarch-testing-converter** の結合テストを行うリポジトリです。

各テストライブラリを単体で検証するのではなく、「Excel形式のテストデータをYAMLに変換したうえで、同じテストロジックが両形式で同一の結果をもたらすか」を確認することを目的としています。

Maven Central にはデプロイしません（`src/main` はありません）。

## テストコードの出自

テストコードは nablarch-testing の既存テストコード（[`nablarch/nablarch-testing@6aa6989`](https://github.com/nablarch/nablarch-testing/commit/6aa6989)）をベースにしています。結合テストを新規作成するのではなく、NTF本体の既存テストコードを流用することで、本体テストと同一のテストロジックを両形式で実行できるようにしています。

## テストの構造

各テスト機能について、以下の2クラスが対になっています。

| クラス名 | 役割 | テストデータ形式 |
|---|---|---|
| `XxxTest`（Excel 版） | NTF 本体から流用したテストクラス | Excel（`.xls`/`.xlsx`） |
| `XxxYamlTest`（YAML 版） | Excel 版を継承し、設定だけを YAML 用に切り替えたクラス | YAML（`target/generated-test-yaml/` に実行時に生成） |

YAML 版は `@BeforeClass` で `YamlModeTestBase.prepareYamlData()` を呼び出し、Excel 版と同名の Excel を実行時に YAML へ変換してから、親のテストをそのまま流します。テストのコードは Excel 版と共通で、YAML 版はテストを書きません。

このほか、本リポジトリ独自のテストがあります。

| クラス | 確かめること |
|---|---|
| `ExcelToYamlEquivalenceTest` | 実行時に `BasicTestDataParserTest.xls` を変換し、Excel を `BasicTestDataParser` で、変換した YAML を `YamlTestDataParser` で読んだ結果（テーブル名・カラム・行数・値）が一致すること |
| `YamlSchemaValidationTest` | YAML のテストデータが JSON Schema に適合すること |

## YAML 版の作り方

本体のテストに合わせて YAML 版を足すときは、次の順に当てはまる型を探してください。どの型でも、**親（Excel 版）のテストメソッドを写して書き換えることはしません**。親を写すと、本体のテストが変わったときに追従できなくなります。親の処理を YAML 版で変える必要がある場合は、親に `protected` のメソッドを足し、YAML 版でそのメソッドを上書きします（型 5・6）。

### 型 1: 基本形

親が `getTestConfig()` で設定ファイル名を返す場合は、それを上書きし、`@BeforeClass` で変換するだけです。

```java
public class BatchRequestTestSupportYamlTest extends BatchRequestTestSupportTest {

    @Override
    protected String getTestConfig() {
        return "unit-test-yaml.xml";  // testDataParser を YamlTestDataParser にした設定
    }

    @BeforeClass
    public static void prepareYaml() {
        YamlModeTestBase.prepareYamlData(BatchRequestTestSupportYamlTest.class, BatchRequestTestSupportTest.class);
    }
}
```

親の `@Rule` がファイル名を直接書いている場合は、親を次のように変えてから上書きします（本体からの変更点の 1）。

```java
@Rule
public SystemRepositoryResource repositoryResource = new SystemRepositoryResource(getTestConfig());

protected String getTestConfig() {
    return "unit-test.xml";
}
```

### 型 2: 形式に応じて委譲するパーサを使う

Excel と YAML の両方のデータを読む設定（`unit-test-format-aware.xml` など。`testDataParser` が `FormatAwareTestDataParser`）を使う場合は、`@Before` で `YamlModeTestBase.switchToYaml(設定ファイル名)` を、`@After` で `YamlModeTestBase.clearYaml()` を呼びます。テストが YAML として読むことを宣言し、実際に読んだ形式と違えば例外にします（YAML のつもりで Excel を読む取り違えを見逃さないため）。

`FormatAwareTestDataParser` は本リポジトリの `src/test` にだけあるクラスです。解説書は 1 つのプロジェクトで Excel と YAML を混在させませんが、本リポジトリは両形式のテストを同じ設定で流すために使っています。

### 型 3: リクエスト単体テストを実行する

親がリクエスト単体テスト（`TestShot` で `Main` を実行するもの）を流す場合は、`createTestShotAround` を上書きし、`YamlModeTestBase.wrapForYaml(...)` で包みます。リクエスト単体テストは、実行後にリポジトリを `unit-test.xml`（本リポジトリでは Excel 用の設定）へ戻すため、そのあとの検証で YAML 用の設定に戻す必要があります。

これは本体の不具合への対処ではありません。利用者のプロジェクトでは `unit-test.xml` 自体に YAML 形式用の `testDataParser` を設定するので、デフォルトの設定に戻っても YAML のままです。本リポジトリが Excel 用と YAML 用の設定を同じクラスパスに置き、`unit-test.xml` を Excel 用にしているために要る処理です。本体の `MainForRequestTesting` がデフォルトの設定へ戻す動作を変えた場合は、`wrapForYaml` を見直してください。

### 型 4: 親が `@ClassRule`（static）で設定を作る

static のフィールドは上書きできないため、`getTestConfig()` は効きません。`@Before` で `RepositoryInitializer.reInitializeRepository(設定ファイル名)` を呼んで YAML 用の設定に切り替えるか、同名の `@ClassRule` を YAML 版で宣言し直します。

### 型 5: 親がテストの中で設定を作り直す

親がテストメソッドの中で設定ファイル名を書いてリポジトリを作り直している場合は、親に設定ファイル名（またはリポジトリの初期化）を返す `protected` のメソッドを足し、YAML 版で上書きします。

### 型 6: 期待値にテストデータのパスやクラス名を含む

エラーメッセージの期待値にテストデータのパスやテストクラス名が入る場合は、親の期待値を `getClass().getName()`・`getClass().getSimpleName()` と、テストデータのディレクトリを返す `protected` のメソッドで組み立て、YAML 版でディレクトリを上書きします。テストデータ自身にパスやクラス名が入っている場合は、`YamlModeTestBase.normalizeEmbeddedTestPath(...)` で変換後の YAML を書き換えます。

### 型 7: 変換できないシートがある

変換ツールの中間モデルにできない異常系のデータ（重複したフィールド名など）は、`prepareYamlData` の 3 つ目以降の引数でシートを変換の対象から外し、そのシートを使うテストを `@Ignore` にします。`@Ignore` には、YAML でその異常を確かめている別のテストを書きます。

### 型 8: データの形式に依存しないテスト

パスの解決など、データの形式によらないテストは、YAML 版では `@Ignore` にします。

## YAML 版の一覧

| YAML 版 | 設定ファイル | 型 |
|---|---|---|
| `TestSupportYamlTest` | `unit-test-yaml.xml` | 1・8 |
| `MultiResourceDataSetUpYamlTest` | `unit-test-format-aware.xml` | 2 |
| `BatchRequestTestSupportYamlTest` | `unit-test-yaml.xml` | 1 |
| `DBtoDBBatchSampleYamlTest` | `unit-test-yaml.xml` | 3・4 |
| `FileToFileBatchSampleYamlTest` | `unit-test-yaml.xml` | 1・3 |
| `SimpleBatchSampleYamlTest` | `unit-test-yaml.xml` | 1・3 |
| `DbAccessTestSupportYamlTest` | `nablarch/test/core/db/DbAccessTestSupportYamlTest.xml` | 1 |
| `EntityTestSupportYamlTest` | `unit-test-yaml.xml` | 1 |
| `TestBeanYamlTest` | `unit-test-yaml.xml` | 1 |
| `TestEntityYamlTest` | `unit-test-yaml.xml` | 1 |
| `FileSupportYamlTest` | `unit-test-yaml.xml` | 1・7 |
| `FileSupportWithDbLessTestDataParserYamlTest` | `unit-test-yaml-dbless.xml` | 1・7 |
| `AbstractHttpRequestTestTemplateYamlTest` | `nablarch/test/core/http/http-test-configuration-format-aware.xml` | 1・2・5 |
| `AbstractHttpRequestTestTemplateTest2YamlTest` | `nablarch/test/core/http/http-test-configuration-format-aware.xml` | 1・2 |
| `MessagingReceiveTestSupportYamlTest` | `unit-test-yaml.xml` | 3・4・5 |
| `MessagingRequestTestSupportYamlTest` | `unit-test-yaml.xml` | 3・4 |
| `RequestTestingMessagingClientYamlTest` | `nablarch/test/core/messaging/web/web-component-configuration-request-testing-yaml.xml` | 2・5 |
| `RequestTestingMessagingContextYamlTest` | `unit-test-format-aware.xml` | 1・2・3 |
| `RequestTestingSendSyncBatchYamlTest` | `unit-test-format-aware.xml` | 1・2・3・6 |
| `RequestTestingSendSyncSupportYamlTest` | `unit-test-format-aware.xml` | 1・2 |

### `@Ignore` にしているテスト

| YAML 版 | テスト | 理由 |
|---|---|---|
| `TestSupportYamlTest` | `testGetPathOf`・`testGetPathResourceExisting` | パスの解決のテストで、データの形式によらない（型 8） |
| `FileSupportYamlTest`・`FileSupportWithDbLessTestDataParserYamlTest` | `testSetUpFixedWithDuplicateName`・`testAssertFixedWithDuplicateName`・`testSetUpVariableWithDuplicateName`・`testAssertVariableWithDuplicateName`（各 4 件） | 重複したフィールド名のデータは変換ツールの中間モデルにできない（型 7）。YAML で重複名を弾くことは、nablarch-testing-yaml の `YamlFileBuilderTest` で確かめている |

## nablarch-testing 本体からの変更点

Excel 版は本体のテストを写したものですが、YAML 版から設定や期待値を切り替えられるよう、次の変更を加えています。テストの検証の内容は変えていません。

1. 設定ファイル名を `getTestConfig()` から取るようにした（15 クラス）。`AbstractHttpRequestTestTemplateTest` は、HTML チェック用の設定も `getHtmlCheckConfig()` から取る
2. YAML 版から使うフィールドを `protected` にした（`AbstractHttpRequestTestTemplateTest#target`、`MessagingRequestTestSupportTest#support` など）
3. YAML 版で上書きするメソッドを足した（型 5・6）

   | クラス | メソッド | 使い道 |
   |---|---|---|
   | `RequestTestingMessagingClientTest` | `getRequestTestingConfig()`・`getXmlAssertAsStringConfig()` | テストの中でリポジトリを作り直すときの設定 |
   | `AbstractHttpRequestTestTemplateTest` | `initializeRepositoryForEmptyTestCase()`・`restoreRepositoryAfterEmptyTestCase()` | `testGetEmptyTestCase` の前後のリポジトリの初期化 |
   | `MessagingReceiveTestSupportTest` | `createUnExtendedSupport()` | `testUnExtends` で使う `MessagingReceiveTestSupport` の生成 |
   | `RequestTestingSendSyncBatchTest` | `getTestDataDir()` | 期待するエラーメッセージに含まれるテストデータのパス（クラス名は `getClass()` から組み立てる） |

4. `TestSupportTest` は、エラーメッセージに含まれる探索パスが YAML 版では違うため、パスの手前までを比べるようにした
5. `TestBeanTest` は、メッセージが実行環境の言語に左右されないよう、言語を日本語に固定した
6. `TestCaseInfoTest` に、異常系のテストを 5 件足した

## 実行方法

```bash
mvn test
```

surefire で、テストクラスごとに JVM を分けています（`reuseForks=false`・`forkCount=1`）。H2 のインメモリ DB と、static な状態（`SystemRepository`、本体のキャッシュ）が次のクラスに残らないようにするためです。1 クラスだけ流す場合は `-Dtest=` で指定してください。

```bash
mvn test -Dtest=TestSupportYamlTest
```

## 今後の課題

### テストの構成の見直し

今の構成は、Excel 版と YAML 版の設定を同じクラスパスに置き、YAML 版のサブクラスで設定を切り替えています。このため、YAML 版のサブクラス、Excel 版の `getTestConfig()` などの変更、`wrapForYaml`、`FormatAwareTestDataParser` といった、本リポジトリ独自の仕組みが要ります。

同じテストクラスを、Excel 用と YAML 用の 2 回、それぞれ別の `unit-test.xml` で実行する構成（surefire の実行を 2 つに分ける、またはモジュールを分ける。YAML 用の実行の前に、変換ツールの Maven プラグインでテストデータを変換する）にすれば、これらは不要になります。利用者のプロジェクトと同じ構成で確かめられ、本体のテストを写し直すだけで追従できるようになります。テストの中で設定ファイル名やパスを直接書いている箇所、`@Ignore` の扱いを確かめる必要があるため、試作してから判断します。

## 依存関係

| ライブラリ | 役割 |
|---|---|
| `nablarch-testing` | NTF本体（Excelベースのテストインフラ） |
| `nablarch-testing-yaml` | YAMLテストデータパーサー |
| `nablarch-testing-converter` | Excel → YAML 変換ツール |
