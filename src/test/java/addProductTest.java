import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class addProductTest extends BaseTest{

    @BeforeEach
    public void setDriverAndUrl(){
        driver = new ChromeDriver();
        baseUrl = "https://demo.beseller.by";
        wait = new WebDriverWait(driver, Duration.of(7, ChronoUnit.SECONDS));
        //выполнение условия внутри драйвера, длительность ожидания 7 секунд
    }

    @Test
    //FIXME поля формы контактов иногда не доступны
    public void addProductToCart() {
        driver.get(baseUrl + "/telefoniya/mobilnye-telefony/mobilnyij-telefon-lg-nexus-5-32gb-16gb-661/");
        String productName = driver.findElement(By.cssSelector(".page-title.product-name")).getText();
//        driver.manage().window().maximize(); максимизирует окно тестируемого браузера

        String kod = "Код: 18118-1-1";
        String price = "104000";

        driver.findElement(By.cssSelector("[data-gtm-id='add-to-cart-product']")).click();
        wait.until(ExpectedConditions.visibilityOf(driver.findElement(By.className("cart-alert"))));  //в течении 7 секунд будет происходить ожидание появления элемента "cart-alert" (пока элемент не станет видимым)
        wait.until(ExpectedConditions.textToBe(By.cssSelector(".button-basket .product-counter"), "1"));  //в течении 7 секунд будет происходить ожидание что у элемента ".button-basket .product-counter" будет текст "1"

        WebElement orderButton = driver.findElement(By.cssSelector(".product-add-block .oformit-v-korzine"));
        assertTrue(orderButton.isDisplayed());

        Actions actions = new Actions(driver);
        actions.moveToElement(orderButton);

        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("window.scrollBy(0,350)", "");

        orderButton.click();

        assertThat(driver.getCurrentUrl()).isEqualTo(baseUrl + "/shcart/"); //сравнивает URL с тем что у нас есть
        assertTrue(driver.findElement(By.className("ok-order__title")).isDisplayed()); //проверяем что отображается элемент ОФОРМЛЕНИЕ ЗАКАЗА

        assertThat(driver.findElements(By.cssSelector("[data-product-item]")).size()).isGreaterThan(0);
        //чтобы не пропустить баг когда у нас вместо одного элемента 2 элемента или нет элементов
        //проверяет что коллекция findElements(By.cssSelector("[data-product-item]")) не пустая
        //если 0 элементов то тест упадет потому что у нас товаров нету

        WebElement product = driver.findElements(By.cssSelector("[data-product-item]")).get(0);
        assertThat(product.findElement(By.cssSelector(".ok-order__image img"))   //нужно обратиться к картинке через img, поэтому нужно добавить название тэга img
                .getAttribute("alt"))  //alt обозначает текст который будет отображаться если наша картинка сломана
                .contains(productName);
        assertThat(product.findElement(By.className("ok-order__text")).getText().contains(productName));
        assertThat(product.findElement(By.className("ok-order__text")).getText().contains(kod));
//        assertThat(product.findElement(By.cssSelector("[data-product-item-input-quantity]"))
//                .getAttribute("value"))
//                .isEqualTo(1); ниже то же самое, но по проще
        assertEquals(product.findElement(By.cssSelector("[data-product-item-input-quantity]"))
                .getAttribute("value"), "1");
        assertEquals(product.findElement(By.cssSelector(".ok-table-el.f-tac.-size-half.hidden-xs"))
                .getText(), "шт.");
        assertEquals(product.findElement(By.cssSelector("[data-product-item-sum] [data-price-value]"))
                .getText(), price);// копейки на сайте то появляются то исчезают из-за этого тесты проходят и не проходят

        //оформление заказа
        String fio = faker.name().fullName();
        String registration = faker.address().fullAddress();
        String phone = faker.phoneNumber().phoneNumber();
        String comment = faker.lorem().sentence();

        driver.findElement(By.name("fio")).sendKeys(fio);
        driver.findElement(By.name("registration")).sendKeys(registration);
        driver.findElement(By.name("phone")).sendKeys(phone);
        driver.findElement(By.name("comment")).sendKeys(comment);

        driver.findElement(By.id("terms_btn_cart_fast")).click();

        assertEquals(driver.getCurrentUrl(), "https://demo.beseller.by/shcart/finish");
        assertThat(driver.findElement(By.className("ok-order__title")).getText()).contains("ЗАКАЗ №", "ОФОРМЛЕН");

        WebElement orderedProduct = driver.findElements(By.cssSelector(".ok-table__tbody .ok-table-row")).get(0);
        assertThat(orderedProduct.findElement(By.cssSelector(".ok-order__image img")).getAttribute("alt"))
                .contains(productName);
        assertThat(orderedProduct.findElement(By.className("ok-order__text")).getText().contains(productName));
        assertThat(orderedProduct.findElement(By.className("ok-order__text")).getText().contains(kod));
        assertEquals(orderedProduct.findElement(By.cssSelector(".ok-order__count")).getText(), "1");
//        assertEquals(orderedProduct.findElement(By.cssSelector(".ok-table-el.f-tac.-size-half.hidden-xs")).getText(), "шт.");
        assertEquals(orderedProduct.findElement(By.cssSelector("[data-finish-order-value]")).getText(), price);

        //ДЗ. Проверка в заказе доставки, оплаты и Контактная информация

        //Способ доставки
        WebElement orderedVerification = driver.findElements(By.cssSelector(".ok-table__tbody .ok-table-row")).get(1);
        assertThat(orderedVerification.findElement(By.className("ok-table-el"))
                .getText()
                .equals("Курьер"));
        assertEquals(orderedVerification.findElement(By.className("ok-order__value")).getText(), "0,00");

        //Способ оплаты
        WebElement orderedVerification1 = driver.findElements(By.cssSelector(".ok-table__tbody .ok-table-row")).get(2);
        assertThat(orderedVerification1.findElement(By.className("ok-table-el")).getText().equals("Наличные"));
        assertEquals(orderedVerification1.findElement(By.className("ok-order__value")).getText(), "0,00");

        //Общая сумма
        assertEquals(driver.findElement(By.cssSelector(".ok-order__sum-val"))
                .getText(), "104 000,00 руб.");

        //Контактная информация
        assertEquals(driver.findElements(By.cssSelector(".ok-order-contact-text.col-md-7.col-sm-7")).get(0).getText(), fio);
        assertThat(driver.findElements(By.cssSelector(".ok-order-contact-text.col-md-7.col-sm-7"))
                .get(1)
                .getText()
                .equals(phone));
//        assertEquals(driver.findElements(By.cssSelector(".ok-order-contact-text.col-md-7.col-sm-7")).get(1).getText(), phone);
        //эту проверку не использовал, т.к. в Actual у номер телефона добавляется буква х
        assertEquals(driver.findElements(By.cssSelector(".ok-order-contact-text.col-md-7.col-sm-7")).get(2).getText(), registration);
        assertEquals(driver.findElements(By.cssSelector(".ok-order-contact-text.col-md-7.col-sm-7")).get(3).getText(), comment);

    }


    //Неявные ожидания. Их всего 3
    @Test
    public void implicityWaitTest() {
//        driver.manage().timeouts().implicitlyWait(5000, TimeUnit.MILLISECONDS);
        driver.manage().timeouts().implicitlyWait(Duration.ofMillis(5000));
        //на уровне драйвера устанавливается таймаут для ожидания элементов
        //ждем 5000 миллисекунд и только потом выполняется элемент, т.е. каждый элемент будет ждать тест по 5 секунд

        driver.manage().timeouts().pageLoadTimeout(Duration.ofMillis(5000));
        //на уровне драйвера устанавливается таймаут для ожидания загрузки страниц

//        driver.manage().timeouts().setScriptTimeout(5000);
        driver.manage().timeouts().scriptTimeout(Duration.ofMillis(5000));
        //на уровне драйвера устанавливается таймаут для ожидания выполнения скрипта
    }

}
