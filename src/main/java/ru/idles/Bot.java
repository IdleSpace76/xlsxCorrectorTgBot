package ru.idles;

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
    public Bot(String token) {
        super(token);
    }

    @Override
    public void onUpdateReceived(Update update) {
        Message msg = update.getMessage();
        User user = msg.getFrom();
        Long id = user.getId();

        Document document = msg.getDocument();
        File originFile = saveDoc(document);

        File newFile = XlsxCreator.correctXlsx(originFile);

        sendDoc(id, newFile);

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
