package nablarch.test.core.db;

import nablarch.test.RepositoryInitializer;
import nablarch.test.core.reader.yaml.YamlModeTestBase;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * {@link MasterDataSetUpperTest} を YAML 形式のマスタデータで再実行するテスト。
 *
 * <p>
 * マスタデータの Excel を、テストの実行時に変換ツールで YAML に変換する。変換ツールは、ブックをシートごとの
 * YAML（{@code <ブック名>/<シート名>.yaml}）に分けて出力する。YAML 形式のマスタデータは 1 つのファイルが
 * 1 つのデータセクションなので（解説書「マスタデータ投入ツール」）、シートごとの YAML をそれぞれマスタデータファイルとして渡す。
 * </p>
 *
 * <p>
 * 親は {@code @ClassRule}（static）でリポジトリを作るため、{@code @Before} で YAML 形式用の設定に作り直す（型 4）。
 * </p>
 */
public class MasterDataSetUpperYamlTest extends MasterDataSetUpperTest {

    /** YAML 形式用のコンポーネント設定ファイル。 */
    private static final String YAML_CONFIG = "unit-test-yaml.xml";

    /** {@link TestTable} に投入するマスタデータの変換先。 */
    private static final Path BASIC_DIR = Paths.get(YamlModeTestBase.YAML_ROOT, "masterdata", "basic");

    /** 外部キーを持つテーブルに投入するマスタデータの変換先。 */
    private static final Path FAMILY_DIR = Paths.get(YamlModeTestBase.YAML_ROOT, "masterdata", "family");

    @BeforeClass
    public static void prepareYaml() {
        for (String workbook : new String[] {"MASTER_DATA.xls", "MASTER_DATA2.xls"}) {
            YamlModeTestBase.convertWorkbook(Paths.get("src/test/java"), workbook, BASIC_DIR);
            YamlModeTestBase.convertWorkbook(
                    Paths.get("src/test/resources/nablarch/test/core/db/masterdata"), workbook, FAMILY_DIR);
        }
    }

    @Before
    public void switchToYaml() {
        RepositoryInitializer.reInitializeRepository(YAML_CONFIG);
    }

    @Override
    protected String getTestConfig() {
        return YAML_CONFIG;
    }

    @Override
    protected List<File> getMasterDataFiles() {
        return listYamlFiles(BASIC_DIR);
    }

    @Override
    protected List<File> getFamilyMasterDataFiles() {
        return listYamlFiles(FAMILY_DIR);
    }

    /**
     * 変換先のブックごとのディレクトリから、シートごとの YAML をブック名・シート名の順に返す。
     *
     * @param dir 変換先
     * @return YAML のマスタデータファイル
     */
    private static List<File> listYamlFiles(Path dir) {
        List<File> files = new ArrayList<File>();
        for (String workbook : new String[] {"MASTER_DATA", "MASTER_DATA2"}) {
            try (Stream<Path> list = Files.list(dir.resolve(workbook))) {
                list.filter(p -> p.toString().endsWith(".yaml")).sorted().forEach(p -> files.add(p.toFile()));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return files;
    }

    @Override @Test @Ignore("起動引数の検査のテストで、データの形式によらない（型 8）")
    public void testMainWithoutArgs() {}

    @Override @Test @Ignore("起動引数の検査のテストで、データの形式によらない（型 8）")
    public void testMainTooManyArgs() {}

    @Override @Test @Ignore("Excel ではないタブ区切りテキストのマスタデータを読むテストで、変換ツールを通らない（型 8）")
    public void testSetUpMasterDataFromNonExcelFile() {}

    @Override @Test @Ignore("パーサをモックにして、拡張子の判定を確かめるテストで、データの形式によらない（型 8）")
    public void testGetAllTableDataFromFileWithNoExtension() {}

    @Override @Test @Ignore("パーサをモックにして、0 件のときの警告を確かめるテストで、データの形式によらない（型 8）")
    public void testWarnWhenNoTableDataWasRead() {}
}
