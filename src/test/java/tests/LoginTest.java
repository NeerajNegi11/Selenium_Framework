package tests;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import utils.ConfigReader;

import java.time.Duration;

public class LoginTest {

    WebDriver driver;

    @BeforeMethod
    public void setup ()
    {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().window().maximize();
    }
    @Test
    public void login () throws InterruptedException {
        driver.get("https://naveenautomationlabs.com/opencart/index.php");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.findElement(By.xpath("//a[@title='My Account']")).click();
        Thread.sleep(1000);
        driver.findElement(By.xpath("//a[contains(@href,'route=account/login')]")).click();
        Thread.sleep(1000);
        String title = driver.getTitle();
        Assert.assertEquals(title, "Account Login");
        WebElement emailLabel = driver.findElement(By.xpath("//label[@for='input-email']"));
        Assert.assertEquals(emailLabel.getText(), "E-Mail Address", "Email label text mismatch");
        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("input-email")));
        emailField.sendKeys(ConfigReader.getSecret("app_email"));
        WebElement passwordLabel = driver.findElement(By.xpath("//label[@for='input-password']"));
        Assert.assertEquals(passwordLabel.getText(), "Password", "Password label text mismatch");
        WebElement passwordField = driver.findElement(By.id("input-password"));
        passwordField.sendKeys(ConfigReader.getSecret("app_password"));
        driver.findElement(By.xpath("//input[@value='Login']")).click();
        wait.until(ExpectedConditions.urlContains("route=account/account"));
        String currentUrl = driver.getCurrentUrl();
        Assert.assertNotNull(currentUrl);
        Assert.assertTrue(currentUrl.contains("route=account/account"),"URL did not change after login");
    }
    @AfterMethod
    public void tearDown()
    {
       if (driver != null)
       {
           driver.quit();
       }
    }
}
