package nablarch.test.core.messaging;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nablarch.fw.messaging.logging.MessagingLogUtil;
import nablarch.fw.messaging.MessageSenderSettings;
import nablarch.fw.messaging.SyncMessage;
import nablarch.test.core.log.LogVerifier;

import nablarch.test.RepositoryInitializer;
import nablarch.test.core.reader.yaml.YamlModeTestBase;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

public class RequestTestingMessagingClientYamlTest extends RequestTestingMessagingClientTest {

    @BeforeClass
    public static void prepareYamlAndRepository() {
        YamlModeTestBase.prepareYamlData(
                RequestTestingMessagingClientYamlTest.class,
                RequestTestingMessagingClientTest.class);
        RepositoryInitializer.recreateRepository(
                "nablarch/test/core/messaging/web/web-component-configuration-request-testing-yaml.xml");
    }

    @Before
    public void switchToYamlFormat() {
        YamlModeTestBase.markYamlFormat();
    }

    @After
    public void clearYamlFormat() {
        YamlModeTestBase.clearYaml();
    }

    // 本体内で Excel 版 web 設定を recreate するため、YAML 版では設定が戻ってしまう。
    //   recreate 先を YAML 版 web 設定に差し替えて実テストする（検証ロジックは Excel 版と同一）。
    @org.junit.Test
    @Override
    public void testAssertAsDataRecord() throws Exception {
        // 今回のテスト用リポジトリ
        RepositoryInitializer.recreateRepository("nablarch/test/core/messaging/web/web-component-configuration-request-testing-yaml.xml");

        Map<String, Object> reqrec = createTestRecord();

        SyncMessage request = new SyncMessage("RM11AD0201");
        request.addDataRecord(reqrec);
        MessageSenderSettings settings = new MessageSenderSettings("RM11AD0201");

        Map<String, String> logInfo = new HashMap<String, String>();
        logInfo.put("logLevel", "INFO");
        logInfo.put("message1", MessagingLogUtil.getHttpSentMessageLog(getSendingMessage(request), utf8Charset));
        List<Map<String,String>> expectedLog = Arrays.asList(logInfo);

        RequestTestingMessagingClient.clearSendingMessageCache();
        RequestTestingMessagingClient.initializeForRequestUnitTesting(getClass(), "testAssertAsDataRecord", "1", "case1", "RM11AD0201");

        LogVerifier.setExpectedLogMessages(expectedLog);
        RequestTestingMessagingClient client = new RequestTestingMessagingClient();
        client.setCharset(utf8CharsetName);
        SyncMessage reply = client.sendSync(settings, request);
        assertEquals("200", reply.getHeaderRecord().get("STATUS_CODE"));
        LogVerifier.verify("Failed!");

        Map<String, Object> resrec = reply.getDataRecord();
        assertEquals("00000000000000000112", resrec.get("userInfoId"));
        assertEquals("0", resrec.get("dataKbn"));
        assertEquals("200", resrec.get("_nbctlhdr.statusCode"));

        RequestTestingMessagingClient.assertSendingMessage(getClass(), "testAssertAsDataRecord", "1", "case1");

        // リポジトリを元に戻す
        RepositoryInitializer.recreateRepository("nablarch/test/core/messaging/web/web-component-configuration-request-testing-yaml.xml");
    }

    // 本体内で独自設定(XmlAssertAsStringTest.xml)と Excel 版 web 設定を recreate するため、
    //   YAML 版では設定が戻る。recreate 先を YAML 版に差し替えて実テストする。
    @org.junit.Test
    @Override
    public void testAssertAsString() throws Exception {
        // 今回のテスト用リポジトリ
        RepositoryInitializer.recreateRepository("nablarch/test/core/messaging/XmlAssertAsStringTest-yaml.xml");

        Map<String, Object> reqrec = createTestRecord();

        SyncMessage request = new SyncMessage("RM11AD0201");
        request.addDataRecord(reqrec);
        MessageSenderSettings settings = new MessageSenderSettings("RM11AD0201");

        Map<String, String> logInfo = new HashMap<String, String>();
        logInfo.put("logLevel", "INFO");
        logInfo.put("message1", MessagingLogUtil.getHttpSentMessageLog(getSendingMessage(request), utf8Charset));
        List<Map<String,String>> expectedLog = Arrays.asList(logInfo);

        RequestTestingMessagingClient.clearSendingMessageCache();
        RequestTestingMessagingClient.initializeForRequestUnitTesting(getClass(), "testAssertAsString", "1", "case1", "RM11AD0201");

        LogVerifier.setExpectedLogMessages(expectedLog);
        RequestTestingMessagingClient client = new RequestTestingMessagingClient();
        client.setCharset(utf8CharsetName);
        SyncMessage reply = client.sendSync(settings, request);
        assertEquals("200", reply.getHeaderRecord().get("STATUS_CODE"));
        LogVerifier.verify("Failed!");

        Map<String, Object> resrec = reply.getDataRecord();
        assertEquals("00000000000000000112", resrec.get("userInfoId"));
        assertEquals("0", resrec.get("dataKbn"));
        assertEquals("200", resrec.get("_nbctlhdr.statusCode"));

         RequestTestingMessagingClient.assertSendingMessage(getClass(), "testAssertAsString", "1", "case1");

        // リポジトリを元に戻す
        RepositoryInitializer.recreateRepository("nablarch/test/core/messaging/web/web-component-configuration-request-testing-yaml.xml");
    }
}
