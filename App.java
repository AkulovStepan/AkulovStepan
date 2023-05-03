
/**
 * Задача: Дан лог-файл прокси-сервера "Squid" (перед работой надо извлечь из архива), 
 * в котором хранится информация о доступе пользователей в интернет. Файл имеет следующую структуру:
 * время_длина запроса_ip-адрес_код/статус_байты данных_метод_URL_пользователь_источник_тип где '_'- пробел.
 * 
 * Написать программу, выводящую в выходной файл out.txt информацию о десяти пользователях с наибольшим трафиком.
 * Обработку и хранение информации необходимо произвести с использованием коллекции типа Map
 * 
 * @author Степан Акулов
 * @version 1.0.0
 */
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Logger;

import javax.swing.JFileChooser;

public class App {
    private static final Logger LOGGER = Logger.getLogger(App.class.getName());

    public static void main(String[] args) throws Exception {
        var path = openFile();
        if (path == null) {
            return;
        }
        var records = readRecords(new File(path));
        var hashMap = new HashMap<String, Integer>();
        for (LogRecord logRecord : records) {
            if (hashMap.containsKey(logRecord.ip())) {
                hashMap.replace(logRecord.ip(), hashMap.get(logRecord.ip()) + logRecord.bytes());
                continue;
            }
            hashMap.put(logRecord.ip(), logRecord.bytes());
        }
        var result = new ArrayList<String>();
        for (int i = 0; i < 10; i++) {
            int max = 0;
            String maxKey = "";
            for (var set : hashMap.entrySet()) {
                if (set.getValue() > max) {
                    max = set.getValue();
                    maxKey = set.getKey();
                }
            }
            result.add(String.format("%s : %s bytes", maxKey, max));
            hashMap.remove(maxKey);
        }
        writeToFile(result);
    }

    /**
     * Открывает проводник, возвращает путь к файлу, выбранному пользователем
     * 
     * @return Путь к выбранному файлу
     */
    private static String openFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Выбор Log файла");
        int response = fileChooser.showOpenDialog(null);
        if (response == JFileChooser.APPROVE_OPTION) {
            LOGGER.info("Selected file: " + fileChooser.getSelectedFile().getAbsolutePath());
            return fileChooser.getSelectedFile().getAbsolutePath();
        } else {
            LOGGER.info("No file selected");
            return null;
        }
    }

    /**
     * Построчно читает файл и возвращает массив строк
     * 
     * @param file - Читаемый файл
     * @return Массив записей {@link LogRecord}
     */
    private static ArrayList<LogRecord> readRecords(File file) {
        var result = new ArrayList<LogRecord>();
        try (var scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                var line = scanner.nextLine();
                var splittedLine = line.split("\s+");
                result.add(new LogRecord(splittedLine[2], Integer.parseInt(splittedLine[4])));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            LOGGER.warning(e.getMessage());
        }

        return result;
    }

    /**
     * Записывает коллекцию строк в файл
     * 
     * @param collection - записываемая коллекция
     */
    private static void writeToFile(Collection<String> collection) {
        try {
            FileWriter fileWriter = new FileWriter("out.txt");
            PrintWriter printWriter = new PrintWriter(fileWriter);
            for (var str : collection) {
                printWriter.println(str);
            }
            printWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}