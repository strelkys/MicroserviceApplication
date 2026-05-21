/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ru.npptmk.common.defectoscope;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author Александр
 */
public class ThicksExporter {

    private static final String DB_URL = "jdbc:derby://localhost:1527/DefectUNPO";
    private static final String DB_USER = "tmk";
    private static final String DB_PASSWORD = "tmk";
    private static final String OUTPUT_FILE = "D:\\Универ\\ВКР\\Код\\ПарсингДаных\\DataTransform\\scientificProject\\thicks_export_2";

    public static void main(String[] args) {
        //exportThicksToCsv();
        exportToNpy(OUTPUT_FILE);
    }

    public static void exportThicksToCsv() {
        String query = "SELECT ID, THICKS, TUBEID FROM TMK.TUBERESULTS WHERE THICKS IS NOT NULL";
        int count = 0;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD); PreparedStatement stmt = conn.prepareStatement(query); ResultSet rs = stmt.executeQuery(); BufferedWriter writer = new BufferedWriter(new FileWriter(OUTPUT_FILE))) {

            int processed = 0;
            while (rs.next()) {
                long id = rs.getLong("ID");
                long tubeId = rs.getLong("TUBEID");
                Blob blob = rs.getBlob("THICKS");

                if (blob != null) {
                    try (InputStream inputStream = blob.getBinaryStream(); ObjectInputStream ois = new ObjectInputStream(inputStream)) {

                        Object obj = ois.readObject();

                        if (obj instanceof ThickResults) {
                            ThickResults thickResults = (ThickResults) obj;
                            boolean notNull = false;
                            // Записываем массив counts
                            short[][][][] counts = thickResults.getCounts();
                            //проверить на нулевые значения
                            serchLoop:
                            for (int x = 0; x < counts.length; x++) {
                                for (int y = 0; y < counts[x].length; y++) {
                                    for (int ch = 0; ch < counts[x][y].length; ch++) {

                                        for (int r = 0; r < counts[x][y][ch].length; r++) {
                                            if (r > 0) {
                                                notNull = true; // если нашли значение прервать цикл
                                                break serchLoop; //
                                            }

                                        }

                                    }
                                }
                            }
                            if (notNull) { //если есть данные , записываю в файл
                                // Записываем метаданные
                                writer.write(String.format("ID,%d\n", id));
                                writer.write(String.format("TUBEID,%d\n", tubeId));
                                writer.write(String.format("xSize,%.6f\n", thickResults.getxSize()));
                                writer.write(String.format("ySize,%.6f\n", thickResults.getySize()));
                                writer.write(String.format("xQt,%d\n", thickResults.getXQt()));
                                writer.write(String.format("yQt,%d\n", thickResults.getYQt()));
                                writer.write(String.format("chanQt,%d\n", thickResults.getChanQt()));
                                writer.write(String.format("min,%.6f\n", thickResults.getMin()));
                                writer.write(String.format("max,%.6f\n", thickResults.getMax()));

                                writer.write("COUNTS_START\n");

                                for (int x = 0; x < counts.length; x++) {
                                    for (int y = 0; y < counts[x].length; y++) {
                                        for (int ch = 0; ch < counts[x][y].length; ch++) {
                                            StringBuilder line = new StringBuilder();
                                            line.append(String.format("%d,%d,%d,", x, y, ch));

                                            for (int r = 0; r < counts[x][y][ch].length; r++) {
                                                if (r > 0) {
                                                    line.append(";");
                                                }
                                                line.append(counts[x][y][ch][r]);
                                            }
                                            line.append("\n");
                                            writer.write(line.toString());
                                        }
                                    }
                                }

                                writer.write("COUNTS_END\n");
                                writer.write("RECORD_END\n\n");

                                count++;
                                if (count % 100 == 0) {
                                    writer.flush();
                                    System.out.println("Обработано записей: " + count);
                                }
                            }
                        }
                    } catch (ClassNotFoundException e) {
                        System.err.println("Класс ThickResults не найден для ID: " + id);
                    }
                }
                processed++;
                if (processed % 1000 == 0) {
                    System.out.println("Прогресс: " + processed + " записей обработано");
                }
            }

            System.out.println("Всего экспортировано записей: " + count);
            System.out.println("Данные сохранены в файл: " + OUTPUT_FILE);

        } catch (SQLException | IOException e) {
            System.err.println("Ошибка: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // В ThicksExporter добавьте метод:
    public static void exportToNpy(String outputDir) {
        new File(outputDir).mkdirs();

//        String query = "SELECT ID, THICKS FROM TMK.TUBERESULTS WHERE THICKS IS NOT NULL";
        String query = "SELECT tr.ID, tr.THICKS, t.THICKNOM " +
               "FROM TMK.TUBERESULTS tr " +
               "INNER JOIN TMK.TUBE t ON tr.TUBEID = t.IDTUBE " +
               "WHERE tr.THICKS IS NOT NULL";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD); PreparedStatement stmt = conn.prepareStatement(query); ResultSet rs = stmt.executeQuery()) {
            int processed = 0;
            int processedNotNull = 0;

            while (rs.next()) {
            //while (processed < 100) {
                //rs.next();
                processed++;
                long id = rs.getLong("ID");
                Blob blob = rs.getBlob("THICKS");
                double thicknom = rs.getDouble("THICKNOM");
                if (blob != null) {
                    try (InputStream inputStream = blob.getBinaryStream(); ObjectInputStream ois = new ObjectInputStream(inputStream)) {

                        Object obj = ois.readObject();
                        if (obj instanceof ThickResults) {
                            ThickResults results = (ThickResults) obj;
                            boolean notNull = true;
                            // Записываем массив counts
                            short[][][][] counts = results.getCounts();
                            //проверить на нулевые значения
                            int nullcounts = 0;
                            serchLoop:
                            for (int x = 25; x < counts.length-25; x++) {
                                for (int y = 0; y < counts[x].length; y++) {
                                    //for (int ch = 0; ch < counts[x][y].length; ch++) {

                                        //for (int r = 0; r < counts[x][y][ch].length; r++) {
                                            if (results.getAvgVal(x, y) == 0) {
                                                nullcounts++;
                                                if (nullcounts > 10) {
                                                notNull = false; // если нашли значение прервать цикл
                                                // System.out.println("значение " + x + "," + y +  "," + ch + "," + r + ": " +  counts[x][y][ch][r]);
                                               // System.out.println("Пропуск в данных. Прогресс: " + processed + " записей обработано: " + processedNotNull);
                                                break serchLoop; //
                                                }

                                            }

                                        //}

                                   // }
                                }
                            }

                            if (notNull) { //если есть данные , записываю в файл
                                // Сохраняем как raw binary для NumPy
                                processedNotNull++;
                                try (FileOutputStream fos = new FileOutputStream(
                                        outputDir + "/thick_" + id + ".raw"); DataOutputStream dos = new DataOutputStream(fos)) {

                                    // Заголовок: размеры массива
                                    dos.writeInt(counts.length);                    // X
                                    dos.writeInt(counts[0].length);                 // Y
                                    dos.writeDouble(thicknom);                      // номинальная толщина
                                    //System.out.println(counts.length);
                                    //System.out.println(counts[0].length);
                                    //System.out.println(thicknom);
                                    //dos.writeInt(counts[0][0].length);              // Channels
                                    //dos.writeInt(counts[0][0][0].length);           // Ranges
                                    //dos.writeDouble(results.getxSize());
                                    //dos.writeDouble(results.getySize());
                                    //dos.writeDouble(results.getMin());
                                    //dos.writeDouble(results.getMax());

                                    // Данные
                                    for (int x = 0; x < counts.length; x++) {
                                        for (int y = 0; y < counts[0].length; y++) {
                                            dos.writeDouble(results.getAvgVal(x, y));
                                            //System.out.print(results.getAvgVal(x, y)+" ");
                                        }
                                        //System.out.println();
                                    }
                                    /**
                                     * for (short[][][] x : counts) { for
                                     * (short[][] y : x) { for (short[] ch : y)
                                     * { //results.getAvgVal(processed,
                                     * processed); for (short v : ch) {
                                     * dos.writeShort(v); } } } }
                                    * *
                                     */
                                   // System.out.println("Прогресс: " + processed + " записей обработано " + notNull);
                                }
                            }
                        }
                    }
                }
                if (processed % 1000 == 0) {
                    System.out.println("Прогресс: " + processed + " записей обработано: " + processedNotNull);
                }
            }
            System.out.println("Прогресс: " + processed + " записей обработано: " + processedNotNull);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
