package ru.streltsov.microserviceapplication.thickservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.npptmk.common.defectoscope.ThickResults;
import ru.streltsov.microserviceapplication.thickservice.model.AnalysisResult;

import java.io.*;
import java.sql.*;

/**
 * Сервис для работы с данными толщинометрии из базы данных Derby
 */
@Slf4j
@Service
public class ThickDataService {

    private static final String DB_URL = "jdbc:derby://localhost:1527/DefectUNPO";
    private static final String DB_USER = "tmk";
    private static final String DB_PASSWORD = "tmk";
    
    // Директория для сохранения raw файлов и тепловых карт
    private static final String OUTPUT_DIR = System.getProperty("java.io.tmpdir") + "/thick_data";

    public ThickDataService() {
        // Создаем директорию для выходных файлов
        new File(OUTPUT_DIR).mkdirs();
    }

    /**
     * Получает данные THICKS для указанной трубы и сохраняет в raw файл
     * @param pipeId ID трубы
     * @return Путь к сохраненному raw файлу, или null если данные не найдены
     */
    public String exportThickToRaw(Long pipeId) {
        String query = "SELECT tr.ID, tr.THICKS, t.THICKNOM " +
                       "FROM TMK.TUBERESULTS tr " +
                       "INNER JOIN TMK.TUBE t ON tr.TUBEID = t.ID " +
                       "WHERE tr.ID = ? AND tr.THICKS IS NOT NULL";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setLong(1, pipeId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    long id = rs.getLong("ID");
                    Blob blob = rs.getBlob("THICKS");
                    double thicknom = rs.getDouble("THICKNOM");
                    
                    if (blob != null) {
                        try (InputStream inputStream = blob.getBinaryStream();
                             ObjectInputStream ois = new ObjectInputStream(inputStream)) {
                            
                            Object obj = ois.readObject();
                            if (obj instanceof ThickResults) {
                                ThickResults results = (ThickResults) obj;
                                
                                // Проверяем на наличие данных
                                if (hasValidData(results)) {
                                    String filePath = OUTPUT_DIR + "/thick_" + id + ".raw";
                                    saveToRawFile(results, thicknom, filePath);
                                    log.info("Saved thick data to: {}", filePath);
                                    return filePath;
                                } else {
                                    log.warn("No valid data found for pipe ID: {}", pipeId);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error exporting thick data for pipe ID: {}", pipeId, e);
        }
        
        return null;
    }

    /**
     * Проверяет наличие валидных данных в результатах
     */
    private boolean hasValidData(ThickResults results) {
        short[][][][] counts = results.getCounts();
        int nullCounts = 0;
        
        searchLoop:
        for (int x = 25; x < Math.min(counts.length - 25, counts.length); x++) {
            for (int y = 0; y < counts[x].length; y++) {
                if (results.getAvgVal(x, y) == 0) {
                    nullCounts++;
                    if (nullCounts > 10) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Сохраняет данные в raw файл в формате, совместимом с ThicksExporter
     */
    private void saveToRawFile(ThickResults results, double thicknom, String filePath) throws IOException {
        short[][][][] counts = results.getCounts();
        
        try (FileOutputStream fos = new FileOutputStream(filePath);
             DataOutputStream dos = new DataOutputStream(fos)) {
            
            // Заголовок: размеры массива
            dos.writeInt(counts.length);                    // X
            dos.writeInt(counts[0].length);                 // Y
            dos.writeDouble(thicknom);                      // номинальная толщина
            
            // Данные - средние значения для каждой точки
            for (int x = 0; x < counts.length; x++) {
                for (int y = 0; y < counts[0].length; y++) {
                    dos.writeDouble(results.getAvgVal(x, y));
                }
            }
        }
    }

    /**
     * Генерирует тепловую карту на основе данных raw файла
     * @param rawFilePath Путь к raw файлу
     * @return Путь к изображению тепловой карты
     */
    public String generateHeatmap(String rawFilePath) {
        // Временная реализация - позже будет вызов Python сервиса
        // Для сейчас просто возвращаем путь к PNG файлу с тем же именем
        String heatmapPath = rawFilePath.replace(".raw", ".png");
        
        // TODO: Здесь будет логика генерации тепловой карты
        // Либо вызов Python микросервиса для анализа и генерации изображения
        
        log.info("Generated heatmap path: {}", heatmapPath);
        return heatmapPath;
    }

    /**
     * Выполняет анализ данных и возвращает результат классификации
     * @param rawFilePath Путь к raw файлу
     * @return Результат анализа
     */
    public AnalysisResult analyzeData(String rawFilePath, Long pipeId) {
        // Временная реализация - позже будет вызов Python сервиса
        AnalysisResult result = new AnalysisResult();
        result.setPipeId(pipeId);
        result.setClassification("Годно"); // По умолчанию
        result.setHeatmapImagePath(generateHeatmap(rawFilePath));
        result.setAverageValue(0.0); // Будет вычислено Python сервисом
        
        // TODO: Здесь будет вызов Python микросервиса для реального анализа
        
        return result;
    }
}
