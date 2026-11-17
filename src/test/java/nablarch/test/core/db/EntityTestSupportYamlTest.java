package nablarch.test.core.db;

import nablarch.test.core.reader.yaml.YamlModeTestBase;
import org.junit.BeforeClass;

/** {@link EntityTestSupportTest} を YAML モードで再実行する等価性確認テスト。 */
public class EntityTestSupportYamlTest extends EntityTestSupportTest {

    @Override
    protected String getTestConfig() {
        return "unit-test-yaml.xml";
    }

    @BeforeClass
    public static void prepareYaml() {
        YamlModeTestBase.prepareYamlData(EntityTestSupportYamlTest.class, EntityTestSupportTest.class);
    }
}
