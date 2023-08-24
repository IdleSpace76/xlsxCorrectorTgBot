package ru.idles;

import org.apache.log4j.Logger;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author a.zharov
 */
public class Main {

    static Logger logger = Logger.getLogger(Main.class);

    public static void main(String[] args) throws TelegramApiException {

        logger.info("Entering application.");

        // загрузка пропертей
        Properties properties = new Properties();
        try (InputStream inputStream =
                 Main.class.getClassLoader().getResourceAsStream("app.properties")){
            properties.load(inputStream);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

        // инициализация бота
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        Bot bot = new Bot(properties.getProperty("botToken"));
        botsApi.registerBot(bot);
    }
}