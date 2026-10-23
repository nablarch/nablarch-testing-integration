package nablarch.test.core;

import nablarch.test.core.reader.yaml.YamlModeTestBase;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

/** {@link MultiResourceDataSetUpTest} を YAML モードで再実行する等価性確認テスト。 */
public class MultiResourceDataSetUpYamlTest extends MultiResourceDataSetUpTest {

    private static final String YAML_CONFIG = "unit-test-format-aware.xml";

    @BeforeClass
    public static void prepareYaml() {
        YamlModeTestBase.prepareYamlData(
                MultiResourceDataSetUpYamlTest.class,
                MultiResourceDataSetUpTest.class);
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
