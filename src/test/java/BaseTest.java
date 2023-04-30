import com.github.javafaker.Faker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

public class BaseTest {

    static ChromeDriver driver;

    String baseUrl = "https://demo.beseller.by";

    WebDriverWait wait;

    Faker faker = new Faker();

    @BeforeAll
    public static void setProperty() {

        System.setProperty("webdriver.http.factory", "jdk-http-client");
    }

    @AfterEach
    public void closeAfterEach() {

        driver.close();
    }
}
