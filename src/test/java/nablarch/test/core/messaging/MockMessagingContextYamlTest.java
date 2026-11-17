package nablarch.test.core.messaging;

import nablarch.core.repository.SystemRepository;
import nablarch.core.repository.di.DiContainer;
import nablarch.core.repository.di.config.xml.XmlComponentDefinitionLoader;
import org.junit.BeforeClass;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

/**
 * {@link MockMessagingContextTest} を YAML 形式の応答電文で再実行するテスト。
 *
 * <p>
 * 取引単体テストの同期送信が、YAML 形式の応答電文を読めること、応答電文を書き換えたときに読み直すことを確かめる。
 * 応答電文のテストデータ（{@code messaging/data} 配下の、リクエストIDごとの Excel）は、テストの実行時に変換する。
 * </p>
 */
public class MockMessagingContextYamlTest extends MockMessagingContextTest {

    /** YAML 形式用のコンポーネント設定ファイル。 */
    private static final String YAML_CONFIG = "nablarch/test/core/messaging/web/web-component-configuration-yaml.xml";

    /**
     * 応答電文を変換し、YAML 形式用の設定でリポジトリを作り直す。
     * 親の {@code @BeforeClass}（Excel 形式用の設定を読む）のあとに実行される。
     */
    @BeforeClass
    public static void prepareYamlAndRepository() {
        MockMessagingClientYamlTest.prepareResponseMessages();
        SystemRepository.clear();
        SystemRepository.load(new DiContainer(new XmlComponentDefinitionLoader(YAML_CONFIG)));
    }

    /**
     * YAML 形式ではテストデータの実体がリクエストIDと同名のディレクトリのため、ディレクトリの中身を差し替える。
     */
    @Override
    protected void replaceTestData(File source, File target) throws Exception {
        Path sourceDir = source.toPath();
        Path targetDir = target.toPath();
        try (Stream<Path> walk = Files.walk(sourceDir)) {
            walk.filter(Files::isRegularFile).forEach(file -> {
                Path copied = targetDir.resolve(sourceDir.relativize(file));
                try {
                    Files.createDirectories(copied.getParent());
                    Files.copy(file, copied, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        }
    }
}
