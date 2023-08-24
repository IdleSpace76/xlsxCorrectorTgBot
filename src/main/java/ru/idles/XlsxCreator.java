package ru.idles;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * Корректор xlsx
 *
 * @author a.zharov
 */
public class XlsxCreator {

    public static File correctXlsx(File file) {

        try (FileInputStream originFile = new FileInputStream(file)) {

            // инициализация исходной таблицы
            Workbook originWorkbook = new XSSFWorkbook(originFile);
            Sheet originSheet = originWorkbook.getSheetAt(0);

            // заполнение временных данных
            Map<Integer, List<String>> data = new HashMap<>();
            int i = 0;
            for (Row row : originSheet) {
                data.put(i, new ArrayList<>());
                for (Cell cell : row) {
                    switch (cell.getCellType()) {
                        case STRING -> data.get(i).add(cell.getRichStringCellValue().getString());
                        case NUMERIC -> {
                            if (DateUtil.isCellDateFormatted(cell)) {
                                data.get(i).add(cell.getDateCellValue() + "");
                            } else {
                                data.get(i).add(cell.getNumericCellValue() + "");
                            }
                        }
                        case BOOLEAN -> data.get(i).add(cell.getBooleanCellValue() + "");
                        case FORMULA -> data.get(i).add(cell.getCellFormula());
                        default -> {}
                    }
                }
                i++;
            }

            // инициализация новой таблицы
            Workbook newWorkbook = new XSSFWorkbook();
            Sheet newSheet = newWorkbook.createSheet();
            newSheet.setColumnWidth(0, 20000);

            // создание стиля заголовка
            CellStyle headerStyle = newWorkbook.createCellStyle();
            Font headerFont = newWorkbook.createFont();
            headerFont.setFontName("Arial");
            headerFont.setFontHeightInPoints((short) 11);
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            // создание заголовка
            Row header = newSheet.createRow(0);
            Cell headerCell = header.createCell(0);
            headerCell.setCellValue(
                    Arrays.stream(data.get(0).get(0).split("\n")).findFirst().orElseThrow());
            headerCell.setCellStyle(headerStyle);

            // строка АДРЕС
            Row addressRow = newSheet.createRow(1);
            Cell addressCell = addressRow.createCell(0);
            addressCell.setCellValue("Адрес");
            addressCell.setCellStyle(headerStyle);

            // создание стиля основной части
            CellStyle commonStyle = newWorkbook.createCellStyle();
            Font commonFont = newWorkbook.createFont();
            commonFont.setFontName("Arial");
            commonFont.setFontHeightInPoints((short) 11);
            commonStyle.setFont(commonFont);

            // заполнение основной части
            int rowNum = 2;
            for (i = 3; i < data.size(); i++) {
                int indexForCut = data.get(i).get(2).indexOf("п.");
                while (data.get(i).get(2).charAt(indexForCut - 1) == ',') {
                    indexForCut--;
                }
                String newString = data.get(i).get(2).substring(0, indexForCut);
                if (newString.equalsIgnoreCase(
                        newSheet.getRow(rowNum - 1).getCell(0).getStringCellValue())) {
                    continue;
                }
                Row newRow = newSheet.createRow(rowNum);
                Cell newCell = newRow.createCell(0);
                newCell.setCellStyle(commonStyle);
                newCell.setCellValue(newString);
                rowNum++;
            }

            // создание файла
            File newFile = new File("new.xlsx");
            FileOutputStream outputStream = new FileOutputStream(newFile);
            newWorkbook.write(outputStream);

            newWorkbook.close();
            outputStream.close();

            return newFile;
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
