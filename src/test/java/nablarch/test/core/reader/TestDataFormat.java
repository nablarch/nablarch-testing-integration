package nablarch.test.core.reader;

/**
 * 実行中のテストデータ形式（YAML / EXCEL）をスレッド跨ぎで共有する保持クラス。
 *
 * <p>送信同期メッセージの応答電文読込はフレームワークの業務スレッド（別スレッド）で行われる。
 * 通常の {@link ThreadLocal} や {@code ThreadContext} は子スレッドに伝播しないため、
 * {@link InheritableThreadLocal} を用いてスレッド生成時に親（テスト実行スレッド）の値を継承させる。</p>
 *
 * <p>形式が未指定の場合は EXCEL とみなす（既存 Excel テストは無変更で動作する）。
 * YAML テストは {@link #setExpected(String)} で「期待する形式」を宣言し、
 * 実際に解決された形式と不一致の場合は {@link FormatAwareTestDataParser} が例外を送出する
 * （YAML のつもりで Excel を読む等の取り違えをサイレントにしない）。</p>
 */
public final class TestDataFormat {

    /** YAML 形式を表す値。 */
    public static final String YAML = "YAML";
    /** EXCEL 形式を表す値。 */
    public static final String EXCEL = "EXCEL";

    /** 実際に使用する形式（未設定時は EXCEL とみなす）。 */
    private static final InheritableThreadLocal<String> CURRENT = new InheritableThreadLocal<String>();
    /** テストが期待する形式（不一致検知用）。 */
    private static final InheritableThreadLocal<String> EXPECTED = new InheritableThreadLocal<String>();

    private TestDataFormat() {}

    /** 使用する形式を設定する（テスト開始時）。 */
    public static void set(String format) {
        CURRENT.set(format);
    }

    /** 使用する形式を取得する。未設定なら EXCEL。 */
    public static String get() {
        String v = CURRENT.get();
        return v != null ? v : EXCEL;
    }

    /** 形式が明示設定されているか（未設定=暗黙EXCELと区別する）。 */
    public static boolean isExplicitlySet() {
        return CURRENT.get() != null;
    }

    /** テストが期待する形式を宣言する（不一致検知用）。 */
    public static void setExpected(String format) {
        EXPECTED.set(format);
    }

    /** 期待する形式を取得する。未宣言なら {@code null}。 */
    public static String getExpected() {
        return EXPECTED.get();
    }

    /** 形式をクリアする（テスト終了時）。 */
    public static void clear() {
        CURRENT.remove();
        EXPECTED.remove();
    }
}
