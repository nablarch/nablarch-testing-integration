package nablarch.test.core.http;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

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

    // 本体内で initializeDefaultRepository()（非ラッパー）に戻すため YAML 版では設定が戻る。
    //   format-aware 設定で再初期化して実テストし、末尾で後続のためデフォルトに戻す。
    @org.junit.Test
    @Override
    public void testGetEmptyTestCase() {
        RepositoryInitializer.reInitializeRepository(getTestConfig());

        target = createDefaultMock();
        try {
            // ExcelのLIST_MAP=testCasesは空
            target.execute("testGetEmptyTestCase");
            fail();
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), containsString("testShots (LIST_MAP=testShots) must have one or more test " +
                    "shots"));
        }
        // 後続テストのためデフォルトリポジトリに戻す（元テストの initializeDefaultRepository と同じ後始末）
        RepositoryInitializer.initializeDefaultRepository();
    }
}
