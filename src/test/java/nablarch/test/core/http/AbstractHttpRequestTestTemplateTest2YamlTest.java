package nablarch.test.core.http;

import nablarch.test.core.reader.yaml.YamlModeTestBase;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

/** {@link AbstractHttpRequestTestTemplateTest2} を YAML モードで再実行する等価性確認テスト。 */
public class AbstractHttpRequestTestTemplateTest2YamlTest extends AbstractHttpRequestTestTemplateTest2 {

    private static final String YAML_CONFIG =
            "nablarch/test/core/http/http-test-configuration-format-aware.xml";

    @Override
    protected String getTestConfig() {
        return YAML_CONFIG;
    }

    @BeforeClass
    public static void prepareYaml() {
        YamlModeTestBase.prepareYamlData(
                AbstractHttpRequestTestTemplateTest2YamlTest.class,
                AbstractHttpRequestTestTemplateTest2.class);
    }

    @Before
    public void switchToYamlFormat() {
        YamlModeTestBase.switchToYaml(YAML_CONFIG);
    }

    @After
    public void clearYamlFormat() {
        YamlModeTestBase.clearYaml();
    }
}
