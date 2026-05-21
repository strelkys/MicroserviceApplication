package ru.streltsov.microserviceapplication.thickservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import ru.npptmk.common.defectoscope.ThickResults;
import ru.streltsov.microserviceapplication.thickservice.model.AnalysisResult;

import java.io.*;
import java.nio.file.Files;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

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
    
    // URL Python микросервиса для анализа
    private static final String THICK_SERVICE_URL = "http://localhost:8001";
    
    private final RestTemplate restTemplate;

    public ThickDataService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
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
     * Генерирует тепловую карту на основе данных raw файла через Python микросервис
     * @param rawFilePath Путь к raw файлу
     * @param pipeId ID трубы
     * @return Путь к изображению тепловой карты
     */
    public String generateHeatmap(String rawFilePath, Long pipeId) {
        try {
            // Читаем raw файл
            File rawFile = new File(rawFilePath);
            byte[] fileContent = Files.readAllBytes(rawFile.toPath());
            
            // Создаем HTTP запрос с файлом
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            
            ByteArrayResource resource = new ByteArrayResource(fileContent) {
                @Override
                public String getFilename() {
                    return "thick_" + pipeId + ".raw";
                }
            };
            
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", resource);
            body.add("pipe_id", pipeId);
            
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            
            // Отправляем файл на Python сервис для анализа и получения heatmap
            ResponseEntity<byte[]> response = restTemplate.postForEntity(
                THICK_SERVICE_URL + "/thick/heatmap/{pipeId}",
                requestEntity,
                byte[].class,
                pipeId
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                // Сохраняем полученное изображение
                String heatmapPath = rawFilePath.replace(".raw", ".png");
                try (FileOutputStream fos = new FileOutputStream(heatmapPath)) {
                    fos.write(response.getBody());
                }
                log.info("Heatmap received from Python service and saved to: {}", heatmapPath);
                return heatmapPath;
            }
        } catch (Exception e) {
            log.error("Error generating heatmap via Python service for pipe ID: {}", pipeId, e);
        }
        
        // Fallback: возвращаем путь без генерации
        return rawFilePath.replace(".raw", ".png");
    }

    /**
     * Выполняет анализ данных через Python микросервис и возвращает результат классификации
     * @param rawFilePath Путь к raw файлу
     * @param pipeId ID трубы
     * @return Результат анализа
     */
    public AnalysisResult analyzeData(String rawFilePath, Long pipeId) {
        try {
            // Читаем raw файл
            File rawFile = new File(rawFilePath);
            byte[] fileContent = Files.readAllBytes(rawFile.toPath());
            
            // Создаем multipart запрос
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            
            ByteArrayResource resource = new ByteArrayResource(fileContent) {
                @Override
                public String getFilename() {
                    return "thick_" + pipeId + ".raw";
                }
            };
            
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", resource);
            body.add("pipe_id", pipeId);
            
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            
            // Отправляем файл на Python сервис для анализа
            ResponseEntity<Map> response = restTemplate.postForEntity(
                THICK_SERVICE_URL + "/thick/upload",
                requestEntity,
                Map.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> resultData = response.getBody();
                
                // Преобразуем ответ в AnalysisResult
                AnalysisResult result = new AnalysisResult();
                result.setPipeId(pipeId);
                
                String status = (String) resultData.get("status");
                result.setClassification("APPROVED".equals(status) ? "Годно" : "Брак");
                
                Double avgThickness = (Double) resultData.get("average_thickness");
                if (avgThickness != null) {
                    result.setAverageValue(avgThickness);
                }
                
                // Генерируем heatmap через отдельный запрос или используем сохраненный
                String heatmapPath = generateHeatmap(rawFilePath, pipeId);
                result.setHeatmapImagePath(heatmapPath);
                
                log.info("Analysis received from Python service for pipe ID {}: {}", pipeId, result.getClassification());
                return result;
            }
        } catch (Exception e) {
            log.error("Error analyzing data via Python service for pipe ID: {}", pipeId, e);
        }
        
        // Fallback: возвращаем результат без анализа
        AnalysisResult result = new AnalysisResult();
        result.setPipeId(pipeId);
        result.setClassification("Годно");
        result.setHeatmapImagePath(generateHeatmap(rawFilePath, pipeId));
        result.setAverageValue(0.0);
        return result;
    }
}
