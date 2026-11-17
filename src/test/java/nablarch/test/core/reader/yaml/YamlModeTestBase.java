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
 * YAML 版のテストクラスを支援するユーティリティ。
 *
 * <p>
 * YAML 版のテストクラス（{@code XxxYamlTest extends XxxTest}）から呼び出す static メソッドと定数を提供する。
 * YAML 版の作り方（型）は README の「YAML 版の作り方」を参照。
 * </p>
 *
 * <p>使い方（基本形）:</p>
 * <pre>
 * public class FooYamlTest extends FooTest {
 *
 *     &#64;Override
 *     protected String getTestConfig() {
 *         return "unit-test-yaml.xml";
 *     }
 *
 *     &#64;BeforeClass
 *     public static void prepareYaml() {
 *         YamlModeTestBase.prepareYamlData(FooYamlTest.class, FooTest.class);
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
     * テストデータの形式を YAML として宣言する（リポジトリの再初期化は行わない）。
     *
     * <p>
     * 独自の {@code @BeforeClass} で YAML 用の設定にリポジトリを作り直したテストが、{@code @Before} から呼ぶ。
     * </p>
     */
    public static void markYamlFormat() {
        TestDataFormat.set(TestDataFormat.YAML);
        TestDataFormat.setExpected(TestDataFormat.YAML);
    }

    /**
     * テストデータの形式を YAML として宣言し、指定の設定でリポジトリを再初期化する。
     * 各 YAML 版の {@code @Before} から呼ぶ。
     *
     * <p>テストデータの形式は、業務のスレッドへ {@link InheritableThreadLocal} 経由で引き継がれる。
     * 期待する形式も YAML として宣言するので、{@link nablarch.test.core.reader.FormatAwareTestDataParser} が
     * 実際に読んだ形式との食い違いを検知できる。</p>
     *
     * @param repositoryXml YAML 用のコンポーネント設定ファイル名
     */
    public static void switchToYaml(String repositoryXml) {
        markYamlFormat();
        RepositoryInitializer.reInitializeRepository(repositoryXml);
    }

    /**
     * テストデータの形式の宣言を消す（次のテストへ持ち越さない）。各 YAML 版の {@code @After} から呼ぶ。
     */
    public static void clearYaml() {
        TestDataFormat.clear();
    }

    /**
     * テストの実行前に、Excel 版のテストデータを YAML に変換し、YAML 版のクラス名のディレクトリへ複製する。
     *
     * <p>
     * YAML 版の {@code @BeforeClass} から呼ぶ。
     * </p>
     *
     * @param concreteClass YAML 版のテストクラス（複製先のディレクトリ名に使う）
     * @param baseTestClass 変換元の Excel と同名のテストクラス（Excel 版）
     */
    public static void prepareYamlData(Class<?> concreteClass, Class<?> baseTestClass) {
        prepareYamlData(concreteClass, baseTestClass, new String[0]);
    }

    /**
     * 変換ツールの中間モデルにできないシート（異常系のデータなど）を変換の対象から外して、
     * {@link #prepareYamlData(Class, Class)} と同じ準備をする。
     *
     * @param concreteClass YAML 版のテストクラス（複製先のディレクトリ名に使う）
     * @param baseTestClass 変換元の Excel と同名のテストクラス（Excel 版）
     * @param excludeSheets 変換の対象外にするシート名
     */
    public static void prepareYamlData(Class<?> concreteClass, Class<?> baseTestClass, String... excludeSheets) {
        String packagePath = baseTestClass.getPackage().getName().replace('.', '/');
        String baseName = baseTestClass.getSimpleName();
        String yamlClassName = concreteClass.getSimpleName();

        Path inputDir = Paths.get("src/test/java", packagePath);
        Path outputDir = Paths.get(YAML_ROOT, packagePath);

        Path yamlClassDir = outputDir.resolve(yamlClassName);
        deleteDirectory(yamlClassDir);

        int converted = convert(inputDir, outputDir, excludeSheets, baseName + ".xls", baseName + ".xlsx");
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
     * Excel のブックを 1 つ、ファイル名を指定して YAML に変換する。
     *
     * <p>
     * テストクラスと対にならない Excel（例: 変換前後の一致を確かめるテストが使う
     * {@code BasicTestDataParserTest.xls}）を、テストの実行時に変換するために使う。
     * 変換結果は {@code outputDir/<ブック名>/<シート名>.yaml} に出力する。
     * </p>
     *
     * @param inputDir         変換元の Excel を置いたディレクトリ
     * @param workbookFileName 変換元の Excel のファイル名（例: {@code BasicTestDataParserTest.xls}）
     * @param outputDir        変換先のディレクトリ
     * @throws IllegalStateException 変換元の Excel が見つからない場合
     */
    public static void convertWorkbook(Path inputDir, String workbookFileName, Path outputDir) {
        int converted = convert(inputDir, outputDir, new String[0], workbookFileName);
        if (converted == 0) {
            throw new IllegalStateException("Excel not found: " + inputDir.resolve(workbookFileName));
        }
    }

    /**
     * 変換ツールで、指定したファイル名の Excel を YAML に変換する。
     *
     * @param inputDir      変換元の Excel を置いたディレクトリ
     * @param outputDir     変換先のディレクトリ
     * @param excludeSheets 変換の対象外にするシート名
     * @param fileNames     変換元の Excel のファイル名
     * @return 変換したブックの数
     */
    private static int convert(Path inputDir, Path outputDir, String[] excludeSheets, String... fileNames) {
        ConversionRequest.Builder builder = new ConversionRequest.Builder()
                .sourceFormat(DataFormat.XLS)
                .targetFormat(DataFormat.YAML)
                .inputPath(inputDir)
                .outputPath(outputDir)
                .overwrite(true);
        for (String fileName : fileNames) {
            builder.include(fileName);
        }
        for (String sheet : excludeSheets) {
            builder.excludeSheet(sheet);
        }
        return TestDataConverter.convert(builder.build());
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
     * リクエスト単体テストの実行後に、YAML 用の設定へリポジトリを戻すよう、{@link TestShot.TestShotAround} を包む。
     *
     * <p>
     * {@link MainForRequestTesting#handle} は、終了時に {@code RepositoryInitializer.revertDefaultRepository()} で
     * デフォルトの設定（{@code unit-test.xml}）に戻す。このメソッドが返す {@code TestShotAround} は、
     * {@code createMain()} が返す {@code Main} の {@code handle()} が終わった直後に
     * {@code reInitializeRepository(repositoryXml)} を呼び直し、後続の検証（{@code assertAll()}）で
     * YAML 用の設定が使われるようにする。
     * </p>
     *
     * <p>
     * これは本体の不具合への対処ではない。利用者のプロジェクトでは {@code unit-test.xml} 自体に YAML 形式用の
     * {@code testDataParser} を設定するため（解説書「テストデータの形式をYAMLに変更する」）、デフォルトの設定に
     * 戻っても YAML のままである。本リポジトリは Excel 用と YAML 用の設定を同じクラスパスに置き、
     * {@code unit-test.xml} を Excel 用にしているため、YAML 版でだけこの処理が要る。
     * 本体の {@link MainForRequestTesting} がデフォルトの設定へ戻す動作を変えた場合は、このメソッドを見直す。
     * </p>
     *
     * @param original      元の {@link TestShot.TestShotAround}
     * @param repositoryXml 戻す先の YAML 用のコンポーネント設定ファイル名（例: {@code "unit-test-yaml.xml"}）
     * @return 包んだ {@link TestShot.TestShotAround}
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
