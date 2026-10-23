package nablarch.test.core.messaging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

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

    // 送信数/行数の不一致検証。エラーメッセージに test class 名（testClass.getName()）が動的に入り、
    //   YAML 版ではクラス名が異なるため、期待値のクラス名のみ YAML 版に調整して実テストする
    //   （検証ロジック・データは Excel 版と同一。YAML 版独自の担保が無いため @Ignore にはしない）。
    @org.junit.Test
    @Override
    public void testAbnormalEnd3() {
        try {
            execute();
            fail();
        } catch (AssertionError e) {
            assertEquals(
                    "number of send message was invalid. expected number=[3], but actual number=[2]. case no=[1], message id=[case6], request id=[RM21AA0101] test class=[nablarch.test.core.messaging.RequestTestingSendSyncBatchYamlTest].",
                    e.getMessage());
        }
    }

    // 送信数/行数の不一致検証。エラーメッセージに test class 名（testClass.getName()）が動的に入り、
    //   YAML 版ではクラス名が異なるため、期待値のクラス名のみ YAML 版に調整して実テストする
    //   （検証ロジック・データは Excel 版と同一。YAML 版独自の担保が無いため @Ignore にはしない）。
    @org.junit.Test
    @Override
    public void testAbnormalEnd4() {
        try {
            execute();
            fail();
        } catch (IllegalStateException e) {
            assertEquals(
                    "number of lines of header and body does not match. number of lines of header=[2], but number of lines of body=[3]. case no=[1], message id=[case6], request id=[RM21AA0101] test class=[nablarch.test.core.messaging.RequestTestingSendSyncBatchYamlTest].",
                    e.getMessage());
        }
    }

    // 送信数/行数の不一致検証。エラーメッセージに test class 名（testClass.getName()）が動的に入り、
    //   YAML 版ではクラス名が異なるため、期待値のクラス名のみ YAML 版に調整して実テストする
    //   （検証ロジック・データは Excel 版と同一。YAML 版独自の担保が無いため @Ignore にはしない）。
    @org.junit.Test
    @Override
    public void testAbnormalEnd5() {
        try {
            execute();
            fail();
        } catch (AssertionError e) {
            assertEquals(
                    "number of send message was invalid. expected number=[1], but actual number=[2]. case no=[1], message id=[case6], request id=[RM21AA0101] test class=[nablarch.test.core.messaging.RequestTestingSendSyncBatchYamlTest].",
                    e.getMessage());
        }
    }

    // 送信数/行数の不一致検証。エラーメッセージに test class 名（testClass.getName()）が動的に入り、
    //   YAML 版ではクラス名が異なるため、期待値のクラス名のみ YAML 版に調整して実テストする
    //   （検証ロジック・データは Excel 版と同一。YAML 版独自の担保が無いため @Ignore にはしない）。
    @org.junit.Test
    @Override
    public void testAbnormalEnd6() {
        try {
            execute();
            fail();
        } catch (IllegalStateException e) {
            assertEquals(
                    "number of lines of header and body does not match. number of lines of header=[2], but number of lines of body=[1]. case no=[1], message id=[case6], request id=[RM21AA0101] test class=[nablarch.test.core.messaging.RequestTestingSendSyncBatchYamlTest].",
                    e.getMessage());
        }
    }

    // メッセージ未発見時のエラーを業務経路(execute)で実テストする。
    //   期待値のうち YAML 版で異なる path（target/generated-test-yaml）と resource name（...YamlTest）のみ
    //   YAML 版に調整する（検証ロジック・データは Excel 版と同一）。
    @org.junit.Test
    @Override
    public void testExpectedMessageNotExist1() {
        try {
            execute();
            org.junit.Assert.fail();
        } catch (IllegalStateException e) {
            org.junit.Assert.assertEquals(
                    "message was not found. message must be set. "
                    + "case number=[1], message id=[notExist], data type=[EXPECTED_REQUEST_HEADER_MESSAGES], "
                    + "path=[target/generated-test-yaml/nablarch/test/core/messaging], "
                    + "resource name=[RequestTestingSendSyncBatchYamlTest/testExpectedMessageNotExist1].",
                    e.getMessage());
        }
    }

    // メッセージ未発見時のエラーを業務経路(execute)で実テストする。
    //   期待値のうち YAML 版で異なる path（target/generated-test-yaml）と resource name（...YamlTest）のみ
    //   YAML 版に調整する（検証ロジック・データは Excel 版と同一）。
    @org.junit.Test
    @Override
    public void testExpectedMessageNotExist2() {
        try {
            execute();
            org.junit.Assert.fail();
        } catch (IllegalStateException e) {
            org.junit.Assert.assertEquals(
                    "message was not found. message must be set. "
                    + "case number=[1], message id=[notExist], data type=[EXPECTED_REQUEST_BODY_MESSAGES], "
                    + "path=[target/generated-test-yaml/nablarch/test/core/messaging], "
                    + "resource name=[RequestTestingSendSyncBatchYamlTest/testExpectedMessageNotExist2].",
                    e.getMessage());
        }
    }

    // メッセージ未発見時のエラーを業務経路(execute)で実テストする。
    //   期待値のうち YAML 版で異なる path（target/generated-test-yaml）と resource name（...YamlTest）のみ
    //   YAML 版に調整する（検証ロジック・データは Excel 版と同一）。
    @org.junit.Test
    @Override
    public void testAbnormalEnd7() {
        try {
            execute();
            org.junit.Assert.fail();
        } catch (IllegalStateException e) {
            org.junit.Assert.assertEquals(
                    "message was not found. message must be set. "
                    + "case number=[1], message id=[case6], data type=[EXPECTED_REQUEST_HEADER_MESSAGES], "
                    + "path=[target/generated-test-yaml/nablarch/test/core/messaging], "
                    + "resource name=[RequestTestingSendSyncBatchYamlTest/testAbnormalEnd7].",
                    e.getMessage());
        }
    }

    // メッセージ未発見時のエラーを業務経路(execute)で実テストする。
    //   期待値のうち YAML 版で異なる path（target/generated-test-yaml）と resource name（...YamlTest）のみ
    //   YAML 版に調整する（検証ロジック・データは Excel 版と同一）。
    @org.junit.Test
    @Override
    public void testAbnormalEnd8() {
        try {
            execute();
            org.junit.Assert.fail();
        } catch (IllegalStateException e) {
            org.junit.Assert.assertEquals(
                    "message was not found. message must be set. "
                    + "case number=[1], message id=[case6], data type=[EXPECTED_REQUEST_BODY_MESSAGES], "
                    + "path=[target/generated-test-yaml/nablarch/test/core/messaging], "
                    + "resource name=[RequestTestingSendSyncBatchYamlTest/testAbnormalEnd8].",
                    e.getMessage());
        }
    }
}
