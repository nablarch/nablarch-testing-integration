package nablarch.test.core.http;

import nablarch.test.RepositoryInitializer;
import nablarch.test.core.reader.yaml.YamlModeTestBase;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

/** {@link AbstractHttpRequestTestTemplateTest} を YAML モードで再実行する等価性確認テスト。 */
public class AbstractHttpRequestTestTemplateYamlTest extends AbstractHttpRequestTestTemplateTest {

    private static final String YAML_CONFIG =
            "nablarch/test/core/http/http-test-configuration-format-aware.xml";

    @Override
    protected String getTestConfig() {
        return YAML_CONFIG;
    }

    @BeforeClass
    public static void prepareYaml() {
        YamlModeTestBase.prepareYamlData(
                AbstractHttpRequestTestTemplateYamlTest.class,
                AbstractHttpRequestTestTemplateTest.class);
    }

    @Before
    public void switchToYamlFormat() {
        YamlModeTestBase.switchToYaml(getTestConfig());
    }

    @After
    public void clearYamlFormat() {
        YamlModeTestBase.clearYaml();
    }


    @Override
    protected String getHtmlCheckConfig() {
        return "nablarch/test/core/http/http-test-configuration-with-htmlcheck-format-aware.xml";
    }

    // 親は testGetEmptyTestCase でデフォルトのリポジトリ（unit-test.xml）に初期化するため、YAML 版では設定が戻る。
    //   YAML 形式用の設定で初期化し、終わったら後続のテストのためにデフォルトのリポジトリへ戻す。
    @Override
    protected void initializeRepositoryForEmptyTestCase() {
        RepositoryInitializer.reInitializeRepository(getTestConfig());
    }

    @Override
    protected void restoreRepositoryAfterEmptyTestCase() {
        RepositoryInitializer.initializeDefaultRepository();
    }
}
