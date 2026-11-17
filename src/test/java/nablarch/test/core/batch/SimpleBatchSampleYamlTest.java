package nablarch.test.core.batch;

import nablarch.test.RepositoryInitializer;
import nablarch.test.core.reader.yaml.YamlModeTestBase;
import nablarch.test.core.standalone.TestShot;
import org.junit.Before;
import org.junit.BeforeClass;

/** {@link SimpleBatchSampleTest} を YAML モードで再実行する等価性確認テスト。 */
public class SimpleBatchSampleYamlTest extends SimpleBatchSampleTest {

    @Override
    protected String getTestConfig() {
        return "unit-test-yaml.xml";
    }

    @BeforeClass
    public static void prepareYaml() {
        YamlModeTestBase.prepareYamlData(SimpleBatchSampleYamlTest.class, SimpleBatchSampleTest.class);
    }

    // リクエスト単体テストは、実行後にリポジトリをデフォルトの設定（unit-test.xml。本リポジトリでは Excel 用）へ戻す。
    //   YAML 用の設定で流すため、テストの前（@Before）と Main の実行後（wrapForYaml）に YAML 用の設定へ切り替える。
    //   本体の不具合ではなく、本リポジトリが両形式の設定を同じクラスパスに置くために要る処理（YamlModeTestBase#wrapForYaml 参照）。
    @Before
    public void switchToYaml() {
        RepositoryInitializer.reInitializeRepository(getTestConfig());
    }

    @Override
    protected TestShot.TestShotAround createTestShotAround(Class<?> testClass) {
        return YamlModeTestBase.wrapForYaml(super.createTestShotAround(testClass), getTestConfig());
    }
}
