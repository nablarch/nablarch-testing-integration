package nablarch.test.core.batch;

import nablarch.test.core.reader.yaml.YamlModeTestBase;
import org.junit.BeforeClass;

/** {@link BatchRequestTestSupportTest} を YAML モードで再実行する等価性確認テスト。 */
public class BatchRequestTestSupportYamlTest extends BatchRequestTestSupportTest {

    @Override
    protected String getTestConfig() {
        return "unit-test-yaml.xml";
    }

    @BeforeClass
    public static void prepareYaml() {
        YamlModeTestBase.prepareYamlData(BatchRequestTestSupportYamlTest.class, BatchRequestTestSupportTest.class);
    }
}
