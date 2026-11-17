package nablarch.test.core.messaging;

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

    /** テストの中でリポジトリを作り直すときの設定を、YAML 形式用にする。 */
    @Override
    protected String getRequestTestingConfig() {
        return "nablarch/test/core/messaging/web/web-component-configuration-request-testing-yaml.xml";
    }

    /** {@link #testAssertAsString()} の設定を、YAML 形式用にする。 */
    @Override
    protected String getXmlAssertAsStringConfig() {
        return "nablarch/test/core/messaging/XmlAssertAsStringTest-yaml.xml";
    }
}
