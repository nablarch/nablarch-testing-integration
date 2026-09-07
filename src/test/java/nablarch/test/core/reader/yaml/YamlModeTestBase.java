package nablarch.test.core.reader.yaml;

import nablarch.fw.ExecutionContext;
import nablarch.fw.launcher.CommandLine;
import nablarch.test.RepositoryInitializer;
import nablarch.test.core.reader.TestDataFormat;
import nablarch.test.core.standalone.MainForRequestTesting;
import nablarch.test.core.standalone.TestShot;
import nablarch.test.tool.converter.ConversionRequest;
import nablarch.test.tool.converter.DataFormat;
import nablarch.test.tool.converter.TestDataConverter;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * YAML モードテストを支援するユーティリティ。
 *
 * <p>
 * YAML 版テストクラス（{@code XxxYamlTest extends XxxTest}）から呼び出す static メソッドと定数を提供する。
 * </p>
 *
 * <p>使い方:</p>
 * <pre>
 * public class FooYamlTest extends FooTest {
 *     &#64;Rule
 *     public SystemRepositoryResource repositoryResource = new SystemRepositoryResource("unit-test-yaml.xml");
 *
 *     &#64;BeforeClass
 *     public static void prepareYaml() {
 *         YamlModeTestBase.prepareYamlData(FooYamlTest.class, FooTest.class);
 *     }
 *
 *     &#64;Before
 *     public void switchToYaml() {
 *         repositoryResource.addComponent("testDataParser", new YamlTestDataParser());
 *         repositoryResource.addComponent("nablarch.test.resource-root", YamlModeTestBase.YAML_ROOT);
 *     }
 * }
 * </pre>
 */
public final class YamlModeTestBase {

    /** 変換先ルートディレクトリ（target 配下・.gitignore で管理対象外） */
    public static final String YAML_ROOT = "target/generated-test-yaml";

    private YamlModeTestBase() {
    }

    /**
     * テスト実行前に Excel を YAML に変換し、自クラス名ディレクトリへ複製する。
     *
     * <p>
     * YAML 版テストクラスの {@code @BeforeClass} メソッドから呼ぶ。
     * </p>
     *
     * @param concreteClass YAML 版テストクラス自身（自クラス名ディレクトリ名に使用）
     * @param baseTestClass 変換元 Excel が置かれているテストクラス（Excel 版クラス）
     */
    /**
     * YAML モードへ切り替える共通処理。各 YAML テストの {@code @Before} から呼ぶ。
     *
     * <p>テストデータ形式を YAML として設定し（業務スレッドへ {@link InheritableThreadLocal} 経由で継承される）、
     * 期待形式も YAML として宣言する（{@link nablarch.test.core.reader.FormatAwareTestDataParser} が
     * 実際の解決形式との不一致を検知できるようにする）。あわせて指定の設定でリポジトリを再初期化する。</p>
     *
     * @param repositoryXml YAML モード用のリポジトリ設定ファイル名
     */
    /**
     * テストデータ形式を YAML として設定する（リポジトリ再初期化は行わない）。
     * 独自の @BeforeClass で YAML 対応設定を recreate 済みのテスト（web 系等）が @Before から呼ぶ。
     */
    public static void markYamlFormat() {
        nablarch.test.core.reader.TestDataFormat.set(nablarch.test.core.reader.TestDataFormat.YAML);
        nablarch.test.core.reader.TestDataFormat.setExpected(nablarch.test.core.reader.TestDataFormat.YAML);
    }

    public static void switchToYaml(String repositoryXml) {
        TestDataFormat.set(TestDataFormat.YAML);
        TestDataFormat.setExpected(TestDataFormat.YAML);
        RepositoryInitializer.reInitializeRepository(repositoryXml);
    }

    /**
     * YAML モードの後始末。各 YAML テストの {@code @After} から呼ぶ。
     * テストデータ形式の設定をクリアする（次テストへ持ち越さない）。
     */
    public static void clearYaml() {
        TestDataFormat.clear();
    }

    public static void prepareYamlData(Class<?> concreteClass, Class<?> baseTestClass) {
        prepareYamlData(concreteClass, baseTestClass, new String[0]);
    }

    /**
     * 異常系データ等、中間モデル化できないシートを変換対象から除外して準備する。
     * @param excludeSheets 変換対象外のシート名
     */
    public static void prepareYamlData(Class<?> concreteClass, Class<?> baseTestClass, String... excludeSheets) {
        String packagePath = baseTestClass.getPackage().getName().replace('.', '/');
        String baseName = baseTestClass.getSimpleName();
        String yamlClassName = concreteClass.getSimpleName();

        Path inputDir = Paths.get("src/test/java", packagePath);
        Path outputDir = Paths.get(YAML_ROOT, packagePath);

        Path yamlClassDir = outputDir.resolve(yamlClassName);
        deleteDirectory(yamlClassDir);

        ConversionRequest.Builder builder = new ConversionRequest.Builder()
                .sourceFormat(DataFormat.XLS)
                .targetFormat(DataFormat.YAML)
                .inputPath(inputDir)
                .outputPath(outputDir)
                .overwrite(true)
                .include(baseName + ".xls")
                .include(baseName + ".xlsx");
        for (String sheet : excludeSheets) {
            builder.excludeSheet(sheet);
        }
        ConversionRequest request2 = builder.build();
        int converted = TestDataConverter.convert(request2);
        if (converted == 0) {
            throw new IllegalStateException(
                    "Excel not found for " + baseName + " under " + inputDir
                            + " — expected " + baseName + ".xls[x]");
        }

        Path convertedDir = outputDir.resolve(baseName);
        if (Files.exists(convertedDir) && !baseName.equals(yamlClassName)) {
            copyDirectory(convertedDir, yamlClassDir);
        }

        // テストデータが ${binaryFile:...} 等で参照する付随バイナリファイル（png 等）は変換対象外のため、
        // 入力ディレクトリ直下の非 Excel/非 Java ファイルを YAML 出力ディレクトリにコピーする
        // （resource-root が YAML ディレクトリ優先のため、付随ファイルもそこに存在する必要がある）。
        copyAuxiliaryFiles(inputDir, outputDir);
    }

    /**
     * 入力ディレクトリ直下の付随ファイル（{@code .xls}/{@code .xlsx}/{@code .java} 以外。バイナリ等）を
     * 出力ディレクトリ直下へコピーする。テストデータが参照する画像等を YAML ディレクトリでも解決できるようにする。
     *
     * @param inputDir  入力ディレクトリ（src/test/java/&lt;package&gt;）
     * @param outputDir 出力ディレクトリ（target/generated-test-yaml/&lt;package&gt;）
     */
    private static void copyAuxiliaryFiles(Path inputDir, Path outputDir) {
        if (!Files.isDirectory(inputDir)) {
            return;
        }
        try (Stream<Path> list = Files.list(inputDir)) {
            list.filter(Files::isRegularFile).forEach(src -> {
                String name = src.getFileName().toString();
                if (name.endsWith(".xls") || name.endsWith(".xlsx") || name.endsWith(".java")) {
                    return;
                }
                try {
                    Files.createDirectories(outputDir);
                    Files.copy(src, outputDir.resolve(name), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * {@code @ClassRule} を使うテストクラス向けに、{@link TestShot.TestShotAround} をラップする。
     *
     * <p>
     * {@link MainForRequestTesting#handle} の finally で {@code revertDefaultRepository()} が呼ばれ
     * {@code SystemRepository} が unit-test.xml ベースに戻る。このメソッドが返す
     * {@code TestShotAround} は、{@code createMain()} で返す {@code Main} の {@code handle()} が
     * 完了した直後に {@code reInitializeRepository(repositoryXml)} を呼び直すことで、
     * 後続の {@code assertAll()} で YAML 版リポジトリが使われるようにする。
     * </p>
     *
     * @param original      元の {@link TestShot.TestShotAround} 実装
     * @param repositoryXml 差し替え先リポジトリ XML（例: {@code "unit-test-yaml.xml"}）
     * @return ラップされた {@link TestShot.TestShotAround}
     */
    public static TestShot.TestShotAround wrapForYaml(
            TestShot.TestShotAround original, String repositoryXml) {
        return new TestShot.TestShotAround() {
            @Override
            public void setUpInputData(TestShot testShot) {
                original.setUpInputData(testShot);
            }
            @Override
            public void assertOutputData(String msgOnFail, TestShot testShot) {
                original.assertOutputData(msgOnFail, testShot);
            }
            @Override
            public boolean isColumnForTestFramework(String columnName) {
                return original.isColumnForTestFramework(columnName);
            }
            @Override
            public String compareStatus(int actual, TestShot testShot) {
                return original.compareStatus(actual, testShot);
            }
            @Override
            public nablarch.fw.launcher.Main createMain() {
                // 元の TestShotAround が返す Main（受信/送信テスト固有の setUpSystemRepository を
                // override したもの等）の挙動を保持しつつ、handle 完了後に YAML 版リポジトリへ
                // 再初期化する。素の MainForRequestTesting に置き換えると元 Main 固有の
                // リポジトリ再構築ロジックが失われ、YAML パーサが使われずメッセージ等が読めなくなる。
                final nablarch.fw.launcher.Main delegate = original.createMain();
                return new nablarch.fw.launcher.Main() {
                    @Override
                    public Integer handle(CommandLine commandLine, ExecutionContext context) {
                        try {
                            return delegate.handle(commandLine, context);
                        } finally {
                            RepositoryInitializer.reInitializeRepository(repositoryXml);
                        }
                    }
                };
            }
        };
    }

    private static void deleteDirectory(Path dir) {
        if (!Files.exists(dir)) {
            return;
        }
        try (Stream<Path> walk = Files.walk(dir)) {
            walk.sorted(Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.delete(p);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void copyDirectory(Path src, Path dest) {
        try (Stream<Path> walk = Files.walk(src)) {
            walk.forEach(source -> {
                Path target = dest.resolve(src.relativize(source));
                try {
                    if (Files.isDirectory(source)) {
                        Files.createDirectories(target);
                    } else {
                        Files.createDirectories(target.getParent());
                        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * 変換後 YAML 内の期待ログ等に埋め込まれた「テストデータのパス／テストクラス名」を YAML 版へ正規化する。
     *
     * <p>一部のテストは期待ログ（{@code message was not found ... path=[...] resource name=[Xxx/yyy]}）を
     * テストデータ自身に持ち、その値が「テストデータのパス（{@code src/test/java/...}）」と
     * 「テストクラス名（{@code XxxTest}）」に依存する。YAML 版では実際の解決パスが
     * {@code target/generated-test-yaml/...}、クラス名が {@code XxxYamlTest} になるため、
     * 変換後 YAML の該当文字列を機械的に置換して実行時の実値と一致させる。
     * これによりメッセージ未発見等の異常系も業務経路（{@code execute()}）で実テストできる。</p>
     *
     * @param concreteClass YAML 版テストクラス（自クラス名ディレクトリ）
     * @param baseTestClass 変換元 Excel が置かれているテストクラス（Excel 版クラス）
     */
    public static void normalizeEmbeddedTestPath(Class<?> concreteClass, Class<?> baseTestClass) {
        String packagePath = baseTestClass.getPackage().getName().replace('.', '/');
        Path yamlClassDir = Paths.get(YAML_ROOT, packagePath, concreteClass.getSimpleName());
        if (!Files.isDirectory(yamlClassDir)) {
            return;
        }
        String fromPath = "path=[src/test/java/" + packagePath + "]";
        String toPath = "path=[" + YAML_ROOT + "/" + packagePath + "]";
        String fromName = "resource name=[" + baseTestClass.getSimpleName() + "/";
        String toName = "resource name=[" + concreteClass.getSimpleName() + "/";
        try (Stream<Path> walk = Files.walk(yamlClassDir)) {
            walk.filter(p -> p.toString().endsWith(".yaml")).forEach(p -> {
                try {
                    String content = new String(Files.readAllBytes(p), java.nio.charset.StandardCharsets.UTF_8);
                    String replaced = content.replace(fromPath, toPath).replace(fromName, toName);
                    if (!replaced.equals(content)) {
                        Files.write(p, replaced.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
