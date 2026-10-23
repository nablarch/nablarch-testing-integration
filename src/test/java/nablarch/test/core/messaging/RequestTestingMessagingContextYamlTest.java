package nablarch.test.core.messaging;

import nablarch.test.core.reader.yaml.YamlModeTestBase;
import nablarch.test.core.standalone.TestShot;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

/** {@link RequestTestingMessagingContextTest} を YAML モードで再実行する等価性確認テスト。 */
public class RequestTestingMessagingContextYamlTest extends RequestTestingMessagingContextTest {

    private static final String YAML_CONFIG = "unit-test-format-aware.xml";

    @Override
    protected String getTestConfig() {
        return YAML_CONFIG;
    }

    @BeforeClass
    public static void prepareYaml() {
        YamlModeTestBase.prepareYamlData(
                RequestTestingMessagingContextYamlTest.class,
                RequestTestingMessagingContextTest.class);
    }

    @Before
    public void switchToYaml() {
        YamlModeTestBase.switchToYaml(YAML_CONFIG);
    }

    @After
    public void clearYaml() {
        YamlModeTestBase.clearYaml();
    }

    @Override
    protected TestShot.TestShotAround createTestShotAround(Class<?> testClass) {
        return YamlModeTestBase.wrapForYaml(super.createTestShotAround(testClass), getTestConfig());
    }
}
