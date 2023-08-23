package ru.idles;

import org.apache.commons.io.FileUtils;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.*;
import java.io.File;

/**
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
            FileUtils.delete(originFile);
            FileUtils.delete(newFile);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getBotUsername() {
        return "xlsCorrectorBot";
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
        try (FileOutputStream fos = new FileOutputStream(".\\origin.xlsx");)  {
            String filePath = execute(getFile).getFilePath();
            File originFile = downloadFile(filePath);
            fos.write(FileUtils.readFileToByteArray(originFile));
            return originFile;
        }
        catch (TelegramApiException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
