package nablarch.test.core.messaging;

import nablarch.test.core.reader.yaml.YamlModeTestBase;
import nablarch.test.core.standalone.TestShot;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

/** {@link RequestTestingSendSyncBatchTest} を YAML モードで再実行する等価性確認テスト。 */
public class RequestTestingSendSyncBatchYamlTest extends RequestTestingSendSyncBatchTest {

    private static final String YAML_CONFIG = "unit-test-format-aware.xml";

    @Override
    protected String getTestConfig() {
        return YAML_CONFIG;
    }

    @BeforeClass
    public static void prepareYaml() {
        YamlModeTestBase.prepareYamlData(
                RequestTestingSendSyncBatchYamlTest.class,
                RequestTestingSendSyncBatchTest.class);
        // 期待ログにテストデータのパス／クラス名が埋め込まれているため、YAML 版の実値へ正規化する。
        YamlModeTestBase.normalizeEmbeddedTestPath(
                RequestTestingSendSyncBatchYamlTest.class,
                RequestTestingSendSyncBatchTest.class);
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

    /** 期待するエラーメッセージに含まれるテストデータのパスを、変換した YAML の置き場所にする。 */
    @Override
    protected String getTestDataDir() {
        return YamlModeTestBase.YAML_ROOT + "/nablarch/test/core/messaging";
    }
}
