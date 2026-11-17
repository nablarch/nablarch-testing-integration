package nablarch.test.core.entity;

import nablarch.test.core.reader.yaml.YamlModeTestBase;
import org.junit.BeforeClass;

/** {@link TestEntityTest} を YAML モードで再実行する等価性確認テスト。 */
public class TestEntityYamlTest extends TestEntityTest {

    @Override
    protected String getTestConfig() {
        return "unit-test-yaml.xml";
    }

    @BeforeClass
    public static void prepareYaml() {
        YamlModeTestBase.prepareYamlData(TestEntityYamlTest.class, TestEntityTest.class);
    }
}
