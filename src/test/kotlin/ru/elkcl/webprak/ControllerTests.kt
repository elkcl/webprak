package ru.elkcl.webprak

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.support.ui.Select
import java.time.Duration
import java.util.concurrent.TimeUnit

class ControllerTests {
    private val baseUrl = "http://localhost:8080"

    @Test
    fun `Main page loads`() {
        val driver = ChromeDriver()
        driver.manage().window().maximize()
        driver.get(baseUrl)
        driver.manage().timeouts().implicitlyWait(Duration.ofMillis(500))
        assertEquals("Домашняя страница", driver.title)
        assertEquals("Домашняя страница", driver.findElement(By.tagName("h1")).text)
        driver.quit()
    }

    @Test
    fun `Operations page loads`() {
        val driver = ChromeDriver()
        driver.manage().window().maximize()
        driver.get(baseUrl)
        driver.manage().timeouts().implicitlyWait(Duration.ofMillis(500))
        val navbarToggler = driver.findElement(By.className("navbar-toggler"))
        if (navbarToggler.isDisplayed) {
            navbarToggler.click()
        }
        val operationsLink = driver.findElement(By.id("operations_nav"))
        operationsLink.click()
        driver.manage().timeouts().implicitlyWait(Duration.ofMillis(500))
        assertEquals("$baseUrl/operations", driver.currentUrl)
        assertEquals("Операции", driver.findElement(By.tagName("h1")).text)
        driver.quit()
    }

    @Test
    fun `Services page loads`() {
        val driver = ChromeDriver()
        driver.manage().window().maximize()
        driver.get(baseUrl)
        driver.manage().timeouts().implicitlyWait(Duration.ofMillis(500))
        val navbarToggler = driver.findElement(By.className("navbar-toggler"))
        if (navbarToggler.isDisplayed) {
            navbarToggler.click()
        }
        val servicesLink = driver.findElement(By.id("services_nav"))
        servicesLink.click()
        driver.manage().timeouts().implicitlyWait(Duration.ofMillis(500))
        assertEquals("$baseUrl/services", driver.currentUrl)
        assertEquals("Услуги", driver.findElement(By.tagName("h1")).text)
        driver.quit()
    }

    @Test
    fun `Clients page loads`() {
        val driver = ChromeDriver()
        driver.manage().window().maximize()
        driver.get(baseUrl)
        driver.manage().timeouts().implicitlyWait(Duration.ofMillis(500))
        val navbarToggler = driver.findElement(By.className("navbar-toggler"))
        if (navbarToggler.isDisplayed) {
            navbarToggler.click()
        }
        val clientsLink = driver.findElement(By.id("clients_nav"))
        clientsLink.click()
        driver.manage().timeouts().implicitlyWait(Duration.ofMillis(500))
        assertEquals("$baseUrl/clients", driver.currentUrl)
        assertEquals("Клиенты", driver.findElement(By.tagName("h1")).text)
        driver.quit()
    }

    @Test
    fun `Clients filtered by service time and balance`() {
        val driver = ChromeDriver()
        driver.manage().window().maximize()
        driver.get("$baseUrl/clients")
        driver.manage().timeouts().implicitlyWait(Duration.ofMillis(500))
        driver.findElement(By.id("time_min_inp")).sendKeys("2025-04-01T00:00:00")
        driver.findElement(By.id("time_max_inp")).sendKeys("2025-04-02T00:00:00")
        driver.findElement(By.id("balance_min_inp")).sendKeys("50")
        driver.findElement(By.xpath("//button[text()='Фильтровать']")).click()
        driver.manage().timeouts().implicitlyWait(Duration.ofMillis(500))
        val rows: List<WebElement> = driver.findElement(By.tagName("table")).findElements(By.tagName("tr"))
        assertEquals(2, rows.size)
        assertEquals("1", rows[1].findElement(By.tagName("th")).text)
        driver.quit()
    }

    @Test
    fun `Added client shows up`() {
        val driver = ChromeDriver()
        driver.manage().window().maximize()
        driver.get("$baseUrl/clients")
        driver.manage().timeouts().implicitlyWait(Duration.ofMillis(500))
        driver.findElement(By.xpath("//button[text()='Зарегистрировать клиента']")).click()
        driver.manage().timeouts().implicitlyWait(Duration.ofMillis(500))
        driver.findElement(By.id("name_query_add")).sendKeys("Федя")
        Select(driver.findElement(By.id("type_add"))).selectByValue("INDIVIDUAL")
        driver.findElement(By.id("passport_num_add")).sendKeys("691337")
        driver.findElement(By.id("passport_iss_add")).sendKeys("2007-01-01")
        driver.findElement(By.xpath("//button[text()='Сохранить']")).click()
        driver.manage().timeouts().implicitlyWait(Duration.ofMillis(500))
        driver.findElement(By.xpath("//button[text()='Вернуться назад']")).click()
        driver.manage().timeouts().implicitlyWait(Duration.ofMillis(500))
        val rows: List<WebElement> = driver.findElement(By.tagName("table")).findElements(By.tagName("tr"))
        assertEquals(6, rows.size)
        assertEquals("Федя", rows[5].findElements(By.tagName("td"))[0].text)
        driver.quit()
    }

//    @Test
//    fun `Edited client shows up`() {
//        val driver = ChromeDriver()
//        driver.manage().window().maximize()
//        driver.get("$baseUrl/clients")
//        driver.manage().timeouts().implicitlyWait(Duration.ofMillis(500))
//        val rows: List<WebElement> = driver.findElement(By.tagName("table")).findElements(By.tagName("tr"))
//        rows[3].findElements(By.tagName("td"))[0].click()
//        driver.manage().timeouts().implicitlyWait(Duration.ofMillis(500))
//        driver.findElement()
//        driver.quit()
//    }
}