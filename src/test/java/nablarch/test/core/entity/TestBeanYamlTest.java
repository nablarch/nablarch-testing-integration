package nablarch.test.core.entity;

import nablarch.test.core.reader.yaml.YamlModeTestBase;
import org.junit.BeforeClass;

/** {@link TestBeanTest} を YAML モードで再実行する等価性確認テスト。 */
public class TestBeanYamlTest extends TestBeanTest {

    @Override
    protected String getTestConfig() {
        return "unit-test-yaml.xml";
    }

    @BeforeClass
    public static void prepareYaml() {
        YamlModeTestBase.prepareYamlData(TestBeanYamlTest.class, TestBeanTest.class);
    }
}
