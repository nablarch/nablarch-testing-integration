package nablarch.test.core.batch;

import nablarch.test.RepositoryInitializer;
import nablarch.test.core.reader.yaml.YamlModeTestBase;
import nablarch.test.core.standalone.TestShot;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 * {@link DBtoDBBatchSampleTest} を YAML モードで再実行する等価性確認テスト。
 *
 * <p>親は @ClassRule（static）でリポジトリを構築するため、設定ファイル名のプルアップ
 * （インスタンスメソッドの override）は static の hiding により効かない。よって @ClassRule は
 * 親のまま（unit-test.xml）とし、YAML 版への切り替えは @Before の reInitializeRepository が担う。</p>
 */
public class DBtoDBBatchSampleYamlTest extends DBtoDBBatchSampleTest {

    /** YAML 版リポジトリ設定（このクラス内で一元管理）。 */
    private static final String YAML_CONFIG = "unit-test-yaml.xml";

    @BeforeClass
    public static void prepareYaml() {
        YamlModeTestBase.prepareYamlData(DBtoDBBatchSampleYamlTest.class, DBtoDBBatchSampleTest.class);
    }

    // FIXME: Main 実行が RepositoryInitializer 経由でグローバル SystemRepository を unit-test.xml に
    //   戻すため、setup と wrapForYaml で YAML 版設定を再適用して打ち消している（対症療法）。
    //   本来は RepositoryInitializer のグローバル汚染を根本で断つのがあるべき姿（released 本体変更を伴う）。
    @Before
    public void switchToYaml() {
        RepositoryInitializer.reInitializeRepository(YAML_CONFIG);
    }

    @Override
    protected TestShot.TestShotAround createTestShotAround(Class<?> testClass) {
        return YamlModeTestBase.wrapForYaml(super.createTestShotAround(testClass), YAML_CONFIG);
    }
}
