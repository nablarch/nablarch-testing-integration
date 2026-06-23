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

    // FIXME: Main 実行が RepositoryInitializer 経由でグローバル SystemRepository を unit-test.xml に
    //   戻すため、setup と wrapForYaml で YAML 版設定を再適用して打ち消している（対症療法）。
    //   本来は RepositoryInitializer のグローバル汚染を根本で断つのがあるべき姿（released 本体変更を伴う）。
    @Before
    public void switchToYaml() {
        RepositoryInitializer.reInitializeRepository(getTestConfig());
    }

    @Override
    protected TestShot.TestShotAround createTestShotAround(Class<?> testClass) {
        return YamlModeTestBase.wrapForYaml(super.createTestShotAround(testClass), getTestConfig());
    }
}
