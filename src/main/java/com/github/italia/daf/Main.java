package com.github.italia.daf;

import com.github.italia.daf.metabase.HTTPClient;
import com.github.italia.daf.metabase.PlotSniper;
import com.github.italia.daf.selenium.Browser;
import com.github.italia.daf.util.LoggerFactory;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class Main {


    private static final Logger LOGGER = LoggerFactory.getLogger( Main.class.getName() );

    public static void main(String[] args) throws IOException {

        HTTPClient client = new HTTPClient(new URL(System.getenv("METABASE_URL")), new HTTPClient.Credential(System.getenv("METABASE_USERNAME"), System.getenv("METABASE_PASSWORD")));
        client.authenticate();



        WebDriver webDriver = new Browser
                .Builder(new URL("http://localhost:4444/wd/hub"))
                .chrome()
                .build()
                .webDriver();
        webDriver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);

//        Fake login


//        webDriver.get("https://graph.daf.teamdigitale.it/auth/login");
//
//        final WebElement username = webDriver.findElement(By.name("username"));
//        final WebElement password = webDriver.findElement(By.name("password"));
//
//        username.sendKeys(System.getenv("METABASE_USERNAME"));
//        password.sendKeys(System.getenv("METABASE_PASSWORD"));
//        webDriver.findElement(By.cssSelector("button.Button.Grid-cell.Button--primary")).click();
//
//

        int i = 0;
        final PlotSniper sniper = new PlotSniper(webDriver);

        for (final HTTPClient.Card card : client.getPublicCards()) {
            final String url = "https://graph.daf.teamdigitale.it/public/question/" + card.public_uuid;
            File src = sniper.shoot(url);
            try {
                FileUtils.copyFile(src, new File("/tmp/" + (i++) + ".png"), true );
            } catch (IOException e) {
                e.printStackTrace();
            }

        }



        LOGGER.info("Closing selenium session");


        webDriver.close();
        webDriver.quit();


    }

//    https://graph.daf.teamdigitale.it/public/question/2bab0301-8e3d-40f2-abcc-54e041074d10
}