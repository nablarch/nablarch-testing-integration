# nablarch-testing-integration

## このリポジトリの目的

Nablarch Testing Framework（NTF）が対応する **複数のテストデータ形式**（Excel / YAML）と、それらを変換する **nablarch-testing-converter** の結合テストを行うリポジトリです。

各テストライブラリを単体で検証するのではなく、「Excel形式のテストデータをYAMLに変換したうえで、同じテストロジックが両形式で同一の結果をもたらすか」を確認することを目的としています。

## テストコードの出自

テストコードは nablarch-testing の既存テストコード（[`nablarch/nablarch-testing@6aa6989`](https://github.com/nablarch/nablarch-testing/commit/6aa6989)）をベースにしています。結合テストを新規作成するのではなく、NTF本体の既存テストコードを流用することで、本体テストと同一のテストロジックを両形式で実行できるようにしています。

## テストの構造

各テスト機能について、以下の2クラスが対になっています。

| クラス名 | 役割 | テストデータ形式 |
|---|---|---|
| `XxxTest`（Excelベース） | NTF本体から流用した既存テストクラス | Excel（`.xls`/`.xlsx`） |
| `XxxYamlTest`（YAMLサブ） | ExcelベースクラスをYAML設定でオーバーライド | YAML（`target/generated-test-yaml/` に自動生成） |

YAMLサブクラスは `@BeforeClass` で `YamlModeTestBase.prepareYamlData()` を呼び出し、Excelファイルを実行時にYAMLへ変換してからテストを実行します。

## nablarch-testing 本体からの変更点

流用にあたり、以下の**機械的な変更のみ**を加えています。ロジック・テスト期待値・アサーションの変更はありません。

### Excelベースクラス（`XxxTest`）への変更

テスト設定ファイルをサブクラスから切り替え可能にするため、ハードコードされていた設定ファイル名をオーバーライド可能なメソッド経由に変更しています。

```java
// 変更前
@Rule
public SystemRepositoryResource repositoryResource = new SystemRepositoryResource("unit-test.xml");

// 変更後
@Rule
public SystemRepositoryResource repositoryResource = new SystemRepositoryResource(getTestConfig());

protected String getTestConfig() {
    return "unit-test.xml";
}
```

また、エラーメッセージの文字列比較で環境依存のパス部分を含む箇所は、前方一致（`whichMessageContains`）で検証できる範囲に絞っています。

### YAMLサブクラス（`XxxYamlTest`）での変更内容

Excelベースクラスを継承し、以下を追加しています。

```java
@BeforeClass
public static void prepareYaml() {
    YamlModeTestBase.prepareYamlData(XxxYamlTest.class, XxxTest.class);
}

@Override
protected String getTestConfig() {
    return "unit-test-yaml.xml";  // YAMLパーサーを使う設定に切り替え
}
```

## 実行方法

H2インメモリDBを共有するため、**1クラスずつ単独実行**してください。

```bash
mvn test -Dtest=TestSupportYamlTest
mvn test -Dtest=RequestTestingMessagingClientYamlTest
# ...
```

## 依存関係

| ライブラリ | 役割 |
|---|---|
| `nablarch-testing` | NTF本体（Excelベースのテストインフラ） |
| `nablarch-testing-yaml` | YAMLテストデータパーサー |
| `nablarch-testing-converter` | Excel → YAML 変換ツール |
