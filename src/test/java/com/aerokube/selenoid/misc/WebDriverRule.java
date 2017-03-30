package com.aerokube.selenoid.misc;

import org.junit.rules.ExternalResource;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import ru.qatools.properties.PropertyLoader;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.function.Function;

public class WebDriverRule extends ExternalResource {

    private static final TestProperties PROPERTIES = PropertyLoader.newInstance().populate(TestProperties.class);
    
    private WebDriver driver;
    
    private final Function<DesiredCapabilities, DesiredCapabilities> capabilitiesProcessor;

    WebDriverRule(Function<DesiredCapabilities, DesiredCapabilities> capabilitiesProcessor) {
        this.capabilitiesProcessor = capabilitiesProcessor;
    }

    @Override
    protected void before() throws Throwable {
        driver = new RemoteWebDriver(getConnectionUrl(), capabilitiesProcessor.apply(getDesiredCapabilities()));
    }
    
    private URL getConnectionUrl() throws MalformedURLException {
        return areLoginAndPasswordPresent() ?
                new URL(String.format(
                    "http://%s:%s@%s:%s/wd/hub",
                    PROPERTIES.getLogin(),
                    PROPERTIES.getPassword(),
                    PROPERTIES.getHostName(),
                    PROPERTIES.getHostPort()
                )) : 
                new URL(String.format(
                        "http://%s:%s/wd/hub",
                        PROPERTIES.getHostName(),
                        PROPERTIES.getHostPort()
                ));
    }
    
    private boolean areLoginAndPasswordPresent() {
        String login = PROPERTIES.getLogin();
        String password = PROPERTIES.getPassword();
        return login != null && !login.isEmpty() && password != null && !password.isEmpty();
    }

    private DesiredCapabilities getDesiredCapabilities() {
        DesiredCapabilities caps = new DesiredCapabilities(PROPERTIES.getBrowserName(), PROPERTIES.getBrowserVersion(), Platform.LINUX);
        caps.setCapability("screenResolution", "1280x1024");
        return caps;
    }
    
    @Override
    protected void after() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }

    public WebDriver getDriver() {
        return driver;
    }

}