package ru.streltsov.microserviceapplication.dataservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.streltsov.microserviceapplication.dataservice.model.PipeResult;
import ru.streltsov.microserviceapplication.dataservice.repository.PipeResultRepository;

import java.sql.*;
import java.util.Optional;

/**
 * Сервис для работы с результатами анализа труб в базе DefectVKR
 */
@Slf4j
@Service
public class AnalysisResultService {

    private static final String DB_URL = "jdbc:derby://localhost:1527/DefectVKR";
    private static final String DB_USER = "tmk";
    private static final String DB_PASSWORD = "tmk";

    @Autowired
    private PipeResultRepository pipeResultRepository;

    /**
     * Получить номер последней проанализированной трубы.
     * Если таблица result не существует - создать её и вернуть -1.
     * Если записей нет - вернуть -1.
     * 
     * @return номер последней трубы или -1 если записей нет
     */
    public Long getLastAnalyzedPipeNumber() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // Проверяем существует ли таблица result
            if (!tableExists(conn, "RESULT")) {
                log.info("Таблица RESULT не найдена. Создаем таблицу...");
                createResultTable(conn);
                return -1L;
            }

            // Ищем последнюю запись в таблице result через JPA репозиторий
            Optional<PipeResult> lastResult = pipeResultRepository.findFirstByOrderByIdDesc();
            
            if (lastResult.isPresent()) {
                Long pipeId = lastResult.get().getPipeId();
                log.info("Найдена последняя запись: pipeId={}", pipeId);
                return pipeId;
            } else {
                log.info("Записей в таблице RESULT не найдено");
                return -1L;
            }

        } catch (SQLException e) {
            log.error("Ошибка подключения к базе DefectVKR: {}", e.getMessage());
            // База может еще не существовать - создаем подключение для проверки
            try {
                // Пробуем подключиться к Derby в режиме создания базы
                Connection conn = DriverManager.getConnection(
                    "jdbc:derby://localhost:1527/DefectVKR;create=true", 
                    DB_USER, 
                    DB_PASSWORD
                );
                conn.close();
                log.info("База данных DefectVKR создана");
                return -1L;
            } catch (SQLException ex) {
                log.error("Ошибка создания базы DefectVKR: {}", ex.getMessage());
            }
            return -1L;
        }
    }

    /**
     * Сохранить результат анализа трубы
     * 
     * @param pipeId номер трубы
     * @param predictedClass предсказанный класс (0 - годно, 1 - брак)
     * @param operatorDecision решение оператора ("GOOD" или "DEFECT")
     * @return true если сохранение успешно
     */
    public boolean saveAnalysisResult(Long pipeId, Integer predictedClass, String operatorDecision) {
        try {
            PipeResult result = new PipeResult();
            result.setPipeId(pipeId);
            result.setPredictedClass(predictedClass);
            result.setOperatorDecision(operatorDecision);

            pipeResultRepository.save(result);
            log.info("Сохранен результат анализа для трубы {}: predictedClass={}, operatorDecision={}", 
                     pipeId, predictedClass, operatorDecision);
            return true;

        } catch (Exception e) {
            log.error("Ошибка сохранения результата анализа для трубы {}: {}", pipeId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Проверить существует ли таблица в базе данных
     */
    private boolean tableExists(Connection conn, String tableName) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        try (ResultSet rs = meta.getTables(null, null, tableName.toUpperCase(), new String[]{"TABLE"})) {
            return rs.next();
        }
    }

    /**
     * Создать таблицу result в базе данных
     */
    private void createResultTable(Connection conn) throws SQLException {
        String createTableSQL = 
            "CREATE TABLE RESULT (" +
            "  ID BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, " +
            "  PIPE_ID BIGINT NOT NULL, " +
            "  PREDICTED_CLASS INTEGER NOT NULL, " +
            "  OPERATOR_DECISION VARCHAR(50) NOT NULL, " +
            "  CREATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ")";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);
            log.info("Таблица RESULT успешно создана");
        }
    }
}
