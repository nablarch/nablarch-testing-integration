package nablarch.test;

import nablarch.test.core.reader.yaml.YamlModeTestBase;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/** {@link TestSupportTest} を YAML モードで再実行する等価性確認テスト。 */
public class TestSupportYamlTest extends TestSupportTest {

    @Override
    protected String getTestConfig() {
        return "unit-test-yaml.xml";
    }

    @BeforeClass
    public static void prepareYaml() {
        YamlModeTestBase.prepareYamlData(TestSupportYamlTest.class, TestSupportTest.class);
    }

    // getPathOf 等はパス解決ロジックの単体テスト（形式非依存・リソース名とパスをハードコード）。
    // YAML 等価性確認の対象外。
    @Override @Test @Ignore("パス解決ロジックの単体テスト。形式非依存のため YAML 版対象外")
    public void testGetPathOf() {}

    @Override @Test @Ignore("パス解決ロジックの単体テスト。形式非依存のため YAML 版対象外")
    public void testGetPathResourceExisting() {}
}
