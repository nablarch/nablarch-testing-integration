package nablarch.test.core.db;

import nablarch.test.core.reader.yaml.YamlModeTestBase;
import org.junit.BeforeClass;

/** {@link DbAccessTestSupportTest} を YAML モードで再実行する等価性確認テスト。 */
public class DbAccessTestSupportYamlTest extends DbAccessTestSupportTest {

    @Override
    protected String getTestConfig() {
        return "nablarch/test/core/db/DbAccessTestSupportYamlTest.xml";
    }

    @BeforeClass
    public static void prepareYaml() {
        YamlModeTestBase.prepareYamlData(DbAccessTestSupportYamlTest.class, DbAccessTestSupportTest.class);
    }
}
