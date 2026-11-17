package nablarch.test.core.messaging;

import nablarch.test.core.reader.yaml.YamlModeTestBase;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.nio.file.Paths;

/**
 * {@link MockMessagingClientTest} を YAML 形式の応答電文で再実行するテスト。
 *
 * <p>
 * 取引単体テストの同期送信（{@link MockMessagingClient}）が、YAML 形式の応答電文を読めることを確かめる。
 * 応答電文のテストデータ（{@code messaging/data} 配下の、リクエストIDごとの Excel）は、テストの実行時に変換する。
 * </p>
 */
public class MockMessagingClientYamlTest extends MockMessagingClientTest {

    /** 変換ツールが読めないため、変換の対象外にする応答電文（不正な文字コードを書いた異常系のデータ）。 */
    private static final String INVALID_ENCODING_WORKBOOK = "RM11AC0297.xls";

    @BeforeClass
    public static void prepareYaml() {
        prepareResponseMessages();
    }

    /**
     * 応答電文のテストデータ（{@code messaging/data} 配下の Excel）を YAML に変換する。
     * {@link MockMessagingContextYamlTest} からも使う。
     */
    static void prepareResponseMessages() {
        YamlModeTestBase.convertDirectory(
                Paths.get("src/test/java/nablarch/test/core/messaging/data"),
                Paths.get(YamlModeTestBase.YAML_ROOT, "nablarch/test/core/messaging/data"),
                INVALID_ENCODING_WORKBOOK);
    }

    @Override @Test @Ignore("不正な文字コードを書いた応答電文（RM11AC0297）は、変換ツールが読めないため変換の対象外。"
            + "YAML 形式で不正な文字コードを書いた場合の動作は確かめていない")
    public void testInvalidEncode() {}

    @Override
    protected String getTestConfig() {
        return "nablarch/test/core/messaging/web/web-component-configuration-yaml.xml";
    }

    @Override
    protected String getSjisConfig() {
        return "nablarch/test/core/messaging/web/web-component-configuration-sjis-yaml.xml";
    }

    @Override
    protected String getNoCharsetConfig() {
        return "nablarch/test/core/messaging/web/web-component-configuration-nocharset-yaml.xml";
    }
}
