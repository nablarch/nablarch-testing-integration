package nablarch.test.core.reader;

import java.util.List;
import java.util.Map;

import nablarch.core.log.Logger;
import nablarch.core.log.LoggerManager;

import nablarch.test.core.file.DataFile;
import nablarch.test.core.messaging.MessagePool;
import nablarch.test.core.messaging.RequestTestingMessagePool;
import nablarch.test.core.db.TableData;

/**
 * 実行中のテストデータ形式（{@link TestDataFormat}）に応じて、YAML 用／EXCEL 用の
 * {@link BasicTestDataParser} へ委譲するプロキシ。
 *
 * <p>{@code RequestTestingSendSyncSupport} が {@code TestDataParser} を {@link BasicTestDataParser} へ
 * キャストして具象メソッド（{@code getSendSyncMessage} 等）を呼ぶため、本プロキシは
 * {@link BasicTestDataParser} を継承する。各メソッドは呼び出しのたびに {@link TestDataFormat#get()} を見て
 * 委譲先を決めるため、{@code SystemRepository} がコンポーネントをキャッシュしても実行時の形式で切り替わる。
 * 形式が未設定の場合は整合不良として例外を送出する。</p>
 */
public class FormatAwareTestDataParser extends BasicTestDataParser {

    private BasicTestDataParser yamlParser;
    private BasicTestDataParser excelParser;

    /** YAML 用パーサを設定する。 */
    public void setYamlParser(BasicTestDataParser yamlParser) {
        this.yamlParser = yamlParser;
    }

    /** EXCEL 用パーサを設定する。 */
    public void setExcelParser(BasicTestDataParser excelParser) {
        this.excelParser = excelParser;
    }

    /** 現在の形式に対応する委譲先パーサを返す。 */
    private static final Logger LOGGER = LoggerManager.get(FormatAwareTestDataParser.class);

    private BasicTestDataParser delegate() {
        String format = TestDataFormat.get();           // 未設定なら EXCEL（既存 Excel テストは無変更で動く）
        String expected = TestDataFormat.getExpected();  // テストが宣言した期待形式（無ければ null）

        // 取り違え検知: 期待形式が宣言されていて、実際の形式と異なる場合は例外（サイレント失敗を防ぐ）。
        if (expected != null && !expected.equals(format)) {
            throw new IllegalStateException(
                    "test data format mismatch. expected=[" + expected + "] but resolved=[" + format
                    + "]. ensure TestDataFormat.set(" + expected + ") is in effect on this thread ["
                    + Thread.currentThread().getName() + "].");
        }

        // どの形式として動作したかを必ずログ出力（事後に追跡可能にする）。
        if (LOGGER.isDebugEnabled()) {
            LOGGER.logDebug("test data format resolved=[" + format + "] explicit=["
                    + TestDataFormat.isExplicitlySet() + "] thread=[" + Thread.currentThread().getName() + "]");
        }

        if (TestDataFormat.YAML.equals(format)) {
            return yamlParser;
        }
        return excelParser;
    }

    @Override
    public List<TableData> getSetupTableData(String path, String resourceName, String... groupId) {
        return delegate().getSetupTableData(path, resourceName, groupId);
    }

    @Override
    public List<TableData> getExpectedTableData(String path, String resourceName, String... groupId) {
        return delegate().getExpectedTableData(path, resourceName, groupId);
    }

    @Override
    public List<Map<String, String>> getListMap(String path, String resourceName, String id) {
        return delegate().getListMap(path, resourceName, id);
    }

    @Override
    public List<DataFile> getSetupFile(String path, String resourceName, String... groupId) {
        return delegate().getSetupFile(path, resourceName, groupId);
    }

    @Override
    public List<DataFile> getExpectedFile(String path, String resourceName, String... groupId) {
        return delegate().getExpectedFile(path, resourceName, groupId);
    }

    @Override
    public MessagePool getMessage(String path, String resourceName, String id) {
        return delegate().getMessage(path, resourceName, id);
    }

    @Override
    public MessagePool getMessageWithoutCache(String path, String resourceName, DataType dataType, String id) {
        return delegate().getMessageWithoutCache(path, resourceName, dataType, id);
    }

    @Override
    public List<RequestTestingMessagePool> getSendSyncMessage(String path, String resourceName, String id, DataType dataType) {
        return delegate().getSendSyncMessage(path, resourceName, id, dataType);
    }

    @Override
    public boolean isResourceExisting(String basePath, String resourceName) {
        return delegate().isResourceExisting(basePath, resourceName);
    }
}
