package nablarch.test.core.messaging;

import nablarch.test.RepositoryInitializer;
import nablarch.test.core.reader.yaml.YamlModeTestBase;
import nablarch.test.core.standalone.TestShot;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 * {@link MessagingReceiveTestSupportTest} を YAML モードで再実行する等価性確認テスト。
 *
 * <p>親は @ClassRule（static）でリポジトリを構築するため、設定ファイル名のプルアップ
 * （インスタンスメソッドの override）は static の hiding により効かない。よって @ClassRule は
 * 親のまま（unit-test.xml）とし、YAML 版への切り替えは @Before の reInitializeRepository が担う。</p>
 */
public class MessagingReceiveTestSupportYamlTest extends MessagingReceiveTestSupportTest {

    /** YAML 版リポジトリ設定（このクラス内で一元管理）。 */
    private static final String YAML_CONFIG = "unit-test-yaml.xml";

    @BeforeClass
    public static void prepareYaml() {
        YamlModeTestBase.prepareYamlData(
                MessagingReceiveTestSupportYamlTest.class,
                MessagingReceiveTestSupportTest.class);
    }

    // リクエスト単体テストは、実行後にリポジトリをデフォルトの設定（unit-test.xml。本リポジトリでは Excel 用）へ戻す。
    //   YAML 用の設定で流すため、テストの前（@Before）と Main の実行後（wrapForYaml）に YAML 用の設定へ切り替える。
    //   本体の不具合ではなく、本リポジトリが両形式の設定を同じクラスパスに置くために要る処理（YamlModeTestBase#wrapForYaml 参照）。
    @Before
    public void switchToYaml() {
        RepositoryInitializer.reInitializeRepository(YAML_CONFIG);
    }

    @Override
    protected TestShot.TestShotAround createTestShotAround(Class<?> testClass) {
        return YamlModeTestBase.wrapForYaml(super.createTestShotAround(testClass), YAML_CONFIG);
    }

    // 親の testUnExtends は、継承せずに MessagingReceiveTestSupport を生成するため createTestShotAround を上書きできない。
    //   生成を上書きし、wrapForYaml を差し込む。
    @Override
    protected MessagingReceiveTestSupport createUnExtendedSupport() {
        return new MessagingReceiveTestSupport(getClass()) {
            @Override
            protected TestShot.TestShotAround createTestShotAround(Class<?> testClass) {
                return YamlModeTestBase.wrapForYaml(super.createTestShotAround(testClass), YAML_CONFIG);
            }
        };
    }
}
