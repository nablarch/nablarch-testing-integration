package nablarch.test.core.file;

import nablarch.test.core.reader.yaml.YamlModeTestBase;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/** {@link FileSupportTest} を YAML モードで再実行する等価性確認テスト。 */
public class FileSupportYamlTest extends FileSupportTest {

    @Override
    protected String getTestConfig() {
        return "unit-test-yaml.xml";
    }

    @BeforeClass
    public static void prepareYaml() {
        // 重複フィールド名の異常系データは中間モデル化できないため変換対象外。
        // 異常系（重複名→エラー）は YAML ユニットテスト（YamlTestDataValidatorTest 等）でカバー済み。
        YamlModeTestBase.prepareYamlData(FileSupportYamlTest.class, FileSupportTest.class,
                "testFixedDuplicateName", "testVariableDuplicateName");
    }

    @Override @Test @Ignore("異常系データは変換不可。固定長の重複名→本体YAML経路の弾きは YamlFileBuilderTest#testBuildFileList_fixedDuplicateFieldNameThrowsException で担保")
    public void testSetUpFixedWithDuplicateName() {}

    @Override @Test @Ignore("異常系データは変換不可。固定長の重複名→本体YAML経路の弾きは YamlFileBuilderTest#testBuildFileList_fixedDuplicateFieldNameThrowsException で担保")
    public void testAssertFixedWithDuplicateName() {}

    @Override @Test @Ignore("異常系データは変換不可。可変長の重複名→本体YAML経路の弾きは YamlFileBuilderTest#testBuildFileList_variableDuplicateFieldNameThrowsException で担保")
    public void testSetUpVariableWithDuplicateName() {}

    @Override @Test @Ignore("異常系データは変換不可。可変長の重複名→本体YAML経路の弾きは YamlFileBuilderTest#testBuildFileList_variableDuplicateFieldNameThrowsException で担保")
    public void testAssertVariableWithDuplicateName() {}
}
