package ru.idles;

import org.apache.log4j.Logger;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Класс бота
 *
 * @author a.zharov
 */
public class Bot extends TelegramLongPollingBot {

    static Logger logger = Logger.getLogger(Bot.class);

    public Bot(String token) {
        super(token);
    }

    @Override
    public void onUpdateReceived(Update update) {
        Message msg = update.getMessage();
        logger.info("Msg from [" + msg.getFrom().getUserName() + "] was accepted with id ["
            + msg.getMessageId() + "]");
        User user = msg.getFrom();
        Long id = user.getId();

        Document document = msg.getDocument();
        File originFile = saveDoc(document);
        logger.info("File was saved from msg with id [" + msg.getMessageId() + "]");

        File newFile = XlsxCreator.correctXlsx(originFile);
        logger.info("File was corrected from msg with id [" + msg.getMessageId() + "]");

        sendDoc(id, newFile);
        logger.info("File was sent from msg with id [" + msg.getMessageId() + "] to ["
            + msg.getFrom().getUserName() + "]");

        try {
            Files.delete(originFile.toPath());
            Files.delete(newFile.toPath());
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getBotUsername() {
        return "xlsxCorrectorBot";
    }

    private void sendDoc(Long who, File what) {
        SendDocument sd = SendDocument.builder()
                .chatId(who.toString())
                .document(new InputFile(what, "new.xlsx"))
                .build();
        try {
            execute(sd);
        }
        catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private File saveDoc(Document document) {
        GetFile getFile = GetFile.builder()
                .fileId(document.getFileId())
                .build();
        try (FileOutputStream fos = new FileOutputStream("origin.xlsx"))  {
            String filePath = execute(getFile).getFilePath();
            File originFile = downloadFile(filePath);
            fos.write(Files.readAllBytes(originFile.toPath()));
            return new File("origin.xlsx");
        }
        catch (TelegramApiException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
