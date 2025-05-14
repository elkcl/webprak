package ru.elkcl.webprak

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.timeout
import org.openqa.selenium.By
import org.openqa.selenium.StaleElementReferenceException
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.interactions.Actions
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.Select
import org.openqa.selenium.support.ui.WebDriverWait
import java.time.Duration
import java.time.LocalDate
import kotlin.test.assertContains


class ControllerTests {
    private val baseUrl = "http://localhost:8080"

    fun WebElement.parseClient(): HtmlController.RenderedClient {
        val tds = this.findElements(By.tagName("td"))
        return HtmlController.RenderedClient(
            this.findElement(By.tagName("th")).text,
            tds[0].text,
            tds[1].text,
            tds[2].text,
            tds[3].text,
            tds[4].text,
        )
    }

    fun WebElement.parseService(): HtmlController.RenderedService {
        val tds = this.findElements(By.tagName("td"))
        return HtmlController.RenderedService(
            this.findElement(By.tagName("th")).text,
            tds[0].text,
            tds[1].text,
            tds[2].text,
            tds[3].text,
        )
    }

    fun WebElement.parseOperation(): HtmlController.RenderedOperation {
        val tds = this.findElements(By.tagName("td"))
        return HtmlController.RenderedOperation(
            this.findElement(By.tagName("th")).text,
            tds[0].text,
            tds[1].text,
            tds[2].text,
            tds[3].text,
            tds[4].text,
            tds[5].text,
        )
    }

    @Test
    fun `Main page loads`() {
        val driver = ChromeDriver()
        driver.manage().window().maximize()
        driver.get(baseUrl)
        driver.manage().timeouts().implicitlyWait(Duration.ofMillis(1000))
        assertEquals("Домашняя страница", driver.title)
        assertEquals("Домашняя страница", driver.findElement(By.tagName("h1")).text)
        driver.quit()
    }

    @Test
    fun `Operations page loads`() {
        val driver = ChromeDriver()
        driver.manage().window().maximize()
        driver.get(baseUrl)
        driver.manage().timeouts().implicitlyWait(Duration.ofMillis(1000))
        val navbarToggler = driver.findElement(By.className("navbar-toggler"))
        if (navbarToggler.isDisplayed) {
            navbarToggler.click()
        }
        val operationsLink = driver.findElement(By.id("operations_nav"))
        operationsLink.click()
        assertEquals("$baseUrl/operations", driver.currentUrl)
        assertEquals("Операции", driver.findElement(By.tagName("h1")).text)
        driver.quit()
    }

    @Test
    fun `Services page loads`() {
        val driver = ChromeDriver()
        driver.manage().window().maximize()
        driver.get(baseUrl)
        driver.manage().timeouts().implicitlyWait(Duration.ofMillis(1000))
        val navbarToggler = driver.findElement(By.className("navbar-toggler"))
        if (navbarToggler.isDisplayed) {
            navbarToggler.click()
        }
        val servicesLink = driver.findElement(By.id("services_nav"))
        servicesLink.click()
        assertEquals("$baseUrl/services", driver.currentUrl)
        assertEquals("Услуги", driver.findElement(By.tagName("h1")).text)
        driver.quit()
    }

    @Test
    fun `Clients page loads`() {
        val driver = ChromeDriver()
        driver.manage().window().maximize()
        driver.get(baseUrl)
        driver.manage().timeouts().implicitlyWait(Duration.ofMillis(1000))
        val navbarToggler = driver.findElement(By.className("navbar-toggler"))
        if (navbarToggler.isDisplayed) {
            navbarToggler.click()
        }
        val clientsLink = driver.findElement(By.id("clients_nav"))
        clientsLink.click()
        assertEquals("$baseUrl/clients", driver.currentUrl)
        assertEquals("Клиенты", driver.findElement(By.tagName("h1")).text)
        driver.quit()
    }

    @Test
    fun `Clients filtered by service time and balance`() {
        val driver = ChromeDriver()
        driver.manage().window().maximize()
        driver.get("$baseUrl/clients")
        driver.manage().timeouts().implicitlyWait(Duration.ofMillis(1000))
        driver.findElement(By.id("time_min_inp")).sendKeys("2025-04-01T00:00:00")
        driver.findElement(By.id("time_max_inp")).sendKeys("2025-04-02T00:00:00")
        driver.findElement(By.id("balance_min_inp")).sendKeys("50")
        driver.findElement(By.xpath("//button[text()='Фильтровать']")).click()
        WebDriverWait(driver, Duration.ofMillis(2000)).ignoring(StaleElementReferenceException::class.java)
            .until(ExpectedConditions.visibilityOfElementLocated(By.tagName("tbody")))
        val rows: List<WebElement> = driver.findElement(By.tagName("tbody")).findElements(By.tagName("tr"))
        assertEquals(1, rows.size)
        assertEquals("1", rows[0].findElement(By.tagName("th")).text)
        driver.quit()
    }

    @Test
    fun `Added client shows up and then gets deleted`() {
        val driver = ChromeDriver()
        driver.manage().window().maximize()
        driver.get("$baseUrl/clients")
        driver.manage().timeouts().implicitlyWait(Duration.ofMillis(1000))
        driver.findElement(By.xpath("//button[contains(text(), 'Зарегистрировать клиента')]")).click()
        driver.findElement(By.id("name_query_add")).sendKeys("Федя")
        Select(driver.findElement(By.id("type_add"))).selectByValue("INDIVIDUAL")
        driver.findElement(By.id("passport_num_add")).sendKeys("691337")
        driver.findElement(By.id("passport_iss_add")).sendKeys("2007-01-01")
        driver.findElement(By.xpath("//button[contains(text(), 'Сохранить')]")).click()
        driver.findElement(By.xpath("//button[contains(text(), 'Вернуться назад')]")).click()
        val rows = driver.findElement(By.tagName("tbody")).findElements(By.tagName("tr")).map { it.parseClient() }
        val filtered = rows.filter { it.name == "Федя"
                && it.clientInfo == IndividualClientInfo("691337", LocalDate.of(2007, 1, 1)).toString()
                && it.creditLimit == "0"
                && it.creditDue == "—"}
        assertEquals(1, filtered.size)
        val elem = driver.findElement(By.xpath("//th/a[text()='${filtered[0].id}']"))
        val actions = Actions(driver)
        actions.moveToElement(elem)
        actions.perform()
        elem.click()
        driver.findElement(By.xpath("//button[contains(text(), 'Удалить клиента')]")).click()
        driver.findElement(By.xpath("//button[text()='Удалить']")).click()
        driver.findElement(By.xpath("//button[contains(text(), 'Вернуться назад')]")).click()
        val rows2 = driver.findElement(By.tagName("tbody")).findElements(By.tagName("tr")).map { it.parseClient() }
        val filtered2 = rows2.filter { it.name == "Федя"
                && it.clientInfo == IndividualClientInfo("691337", LocalDate.of(2007, 1, 1)).toString()
                && it.creditLimit == "0"
                && it.creditDue == "—"}
        assertEquals(0, filtered2.size)
        driver.quit()
    }

    @Test
    fun `Client gets edited`() {
        val driver = ChromeDriver()
        driver.manage().window().maximize()
        driver.get("$baseUrl/clients")
        driver.manage().timeouts().implicitlyWait(Duration.ofMillis(1000))
        val elem = driver.findElement(By.xpath("//th/a[text()='3']"))
        val actions = Actions(driver)
        actions.scrollToElement(elem)
        actions.moveToElement(elem)
        actions.perform()
        elem.click()
        val field = driver.findElement(By.id("address_add"))
        field.clear()
        field.sendKeys("Юпитер")
        Actions(driver).moveToElement(driver.findElement(By.xpath("//button[contains(text(), 'Сохранить')]"))).click().perform()
        assertEquals(1, driver.findElements(By.xpath("//button[contains(text(), 'Вернуться назад')]")).size)
        driver.get("$baseUrl/clients")
        val client = driver.findElement(By.xpath("//th/a[text()='3']//ancestor::tr")).parseClient()
        assertContains(client.clientInfo, "Юпитер")
        driver.quit()
    }

    @Test
    fun `Services filtered by type`() {
        val driver = ChromeDriver()
        driver.manage().window().maximize()
        driver.get("$baseUrl/services")
        driver.manage().timeouts().implicitlyWait(Duration.ofMillis(1000))
        Select(driver.findElement(By.id("type_inp"))).selectByValue("INTERNET")
        driver.findElement(By.xpath("//button[text()='Фильтровать']")).click()
        WebDriverWait(driver, Duration.ofMillis(2000)).ignoring(StaleElementReferenceException::class.java)
            .until(ExpectedConditions.visibilityOfElementLocated(By.tagName("tbody")))
        val rows: List<WebElement> = driver.findElement(By.tagName("tbody")).findElements(By.tagName("tr"))
        assertEquals(1, rows.size)
        assertEquals("1", rows[0].findElement(By.tagName("th")).text)
        driver.quit()
    }

    @Test
    fun `Added service shows up and then gets deleted`() {
        val driver = ChromeDriver()
        driver.manage().window().maximize()
        driver.get("$baseUrl/services")
        driver.manage().timeouts().implicitlyWait(Duration.ofMillis(1000))
        driver.findElement(By.xpath("//button[contains(text(), 'Зарегистрировать услугу')]")).click()
        driver.findElement(By.id("name_query_add")).sendKeys("Мега бомбер")
        driver.findElement(By.id("desc_query_add")).sendKeys("Пакет в 2000 SMS")
        Select(driver.findElement(By.id("type_add"))).selectByValue("SMS")
        Select(driver.findElement(By.id("billing_type_add"))).selectByValue("ONEOFF")
        driver.findElement(By.id("oneoff_add")).sendKeys("500")
        driver.findElement(By.xpath("//button[contains(text(), 'Сохранить')]")).click()
        driver.findElement(By.xpath("//button[contains(text(), 'Вернуться назад')]")).click()
        val rows = driver.findElement(By.tagName("tbody")).findElements(By.tagName("tr")).map { it.parseService() }
        val filtered = rows.filter { it.name == "Мега бомбер"
                && it.description == "Пакет в 2000 SMS"
                && it.serviceType == "СМС"
                && it.billingInfo == OneoffBillingInfo(500).toString()
        }
        assertEquals(1, filtered.size)
        val elem = driver.findElement(By.xpath("//th/a[text()='${filtered[0].id}']"))
        val actions = Actions(driver)
        actions.moveToElement(elem)
        actions.perform()
        elem.click()
        driver.findElement(By.xpath("//button[contains(text(), 'Удалить услугу')]")).click()
        driver.findElement(By.xpath("//button[text()='Удалить']")).click()
        driver.findElement(By.xpath("//button[contains(text(), 'Вернуться назад')]")).click()
        val rows2 = driver.findElement(By.tagName("tbody")).findElements(By.tagName("tr")).map { it.parseService() }
        val filtered2 = rows2.filter { it.name == "Мега бомбер"
                && it.description == "Пакет в 2000 SMS"
                && it.serviceType == "СМС"
                && it.billingInfo == OneoffBillingInfo(500).toString()
        }
        assertEquals(0, filtered2.size)
        driver.quit()
    }

    @Test
    fun `Service gets edited`() {
        val driver = ChromeDriver()
        driver.manage().window().maximize()
        driver.get("$baseUrl/services")
        driver.manage().timeouts().implicitlyWait(Duration.ofMillis(1000))
        val elem = driver.findElement(By.xpath("//th/a[text()='2']"))
        val actions = Actions(driver)
        actions.moveToElement(elem)
        actions.perform()
        elem.click()
        val field = driver.findElement(By.id("name_query_add"))
        field.clear()
        field.sendKeys("На трубке")
        Actions(driver).moveToElement(driver.findElement(By.xpath("//button[contains(text(), 'Сохранить')]"))).click().perform()
        assertEquals(1, driver.findElements(By.xpath("//button[contains(text(), 'Вернуться назад')]")).size)
        driver.get("$baseUrl/services")
        val service = driver.findElement(By.xpath("//th/a[text()='2']//ancestor::tr")).parseService()
        assertEquals("На трубке", service.name)
        driver.quit()
    }
    @Test
    fun `Operations filtered by client`() {
        val driver = ChromeDriver()
        driver.manage().window().maximize()
        driver.get("$baseUrl/operations")
        driver.manage().timeouts().implicitlyWait(Duration.ofMillis(1000))
        driver.findElement(By.id("client_query_id")).sendKeys("Вася")
        driver.findElement(By.xpath("//button[text()='Фильтровать']")).click()
        WebDriverWait(driver, Duration.ofMillis(2000)).ignoring(StaleElementReferenceException::class.java)
            .until(ExpectedConditions.visibilityOfElementLocated(By.tagName("tbody")))
        val rows: List<WebElement> = driver.findElement(By.tagName("tbody")).findElements(By.tagName("tr"))
        assertEquals(2, rows.size)
        assertEquals("1", rows[0].findElement(By.tagName("th")).text)
        assertEquals("3", rows[1].findElement(By.tagName("th")).text)
        driver.quit()
    }

    @Test
    fun `Added operation shows up and adds to balance`() {
        val driver = ChromeDriver()
        driver.manage().window().maximize()
        driver.get("$baseUrl/operations")
        driver.manage().timeouts().implicitlyWait(Duration.ofMillis(1000))
        driver.findElement(By.xpath("//button[contains(text(), 'Зарегистрировать операцию')]")).click()
        driver.findElement(By.id("client_id_add")).sendKeys("4")
        Select(driver.findElement(By.id("type_add"))).selectByValue("DEPOSIT")
        driver.findElement(By.id("amount_min_add")).sendKeys("100")
        driver.findElement(By.xpath("//button[contains(text(), 'Сохранить')]")).click()
        driver.findElement(By.xpath("//button[contains(text(), 'Вернуться назад')]")).click()
        val rows = driver.findElement(By.tagName("tbody")).findElements(By.tagName("tr")).map { it.parseOperation() }
        val filtered = rows.filter { it.client == "[4] Альберт Эйнштейн"
                && it.opType == "Пополнение"
                && it.service == "—"
                && it.description == "—"
                && it.amount == "100"
        }
        assertEquals(1, filtered.size)
        driver.get("$baseUrl/clients")
        val clientRows = driver.findElement(By.tagName("tbody")).findElements(By.tagName("tr")).map { it.parseClient() }
        assertEquals("100", clientRows.filter { it.id == "4" }[0].balance)
        driver.quit()
    }
}