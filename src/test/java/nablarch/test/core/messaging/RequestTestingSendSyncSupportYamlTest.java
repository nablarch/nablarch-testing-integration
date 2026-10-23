package nablarch.test.core.messaging;

import nablarch.test.core.reader.yaml.YamlModeTestBase;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

/** {@link RequestTestingSendSyncSupportTest} を YAML モードで再実行する等価性確認テスト。 */
public class RequestTestingSendSyncSupportYamlTest extends RequestTestingSendSyncSupportTest {

    private static final String YAML_CONFIG = "unit-test-format-aware.xml";

    @Override
    protected String getTestConfig() {
        return YAML_CONFIG;
    }

    @BeforeClass
    public static void prepareYaml() {
        YamlModeTestBase.prepareYamlData(
                RequestTestingSendSyncSupportYamlTest.class,
                RequestTestingSendSyncSupportTest.class);
    }

    @Before
    public void switchToYaml() {
        YamlModeTestBase.switchToYaml(YAML_CONFIG);
    }

    @After
    public void clearYaml() {
        YamlModeTestBase.clearYaml();
    }
}
