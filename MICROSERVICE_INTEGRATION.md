# Реализация логики взаимодействия между микросервисами

## Общая схема работы

```
UI Module (8083) → ThickService (8080) → DataService (8082) → Derby БД
                      ↓
                  app.py (8001) - Python FastAPI для анализа
```

## Последовательность операций

### 1. Начало работы (после аутентификации)

**UI → ThickService:**
- `POST /thick/start`
- ThickService запрашивает у DataService последнюю запись

**ThickService → DataService:**
- `GET /pipes/last-analyzed`
- DataService проверяет базу DefectVKR (таблица RESULT)
- Если записей нет или таблица не существует → создает таблицу и возвращает `-1`
- Если есть записи → возвращает номер последней трубы

**ThickService → DefectUNPO БД:**
- Если ответ = -1: берет трубу с минимальным ID
- Если ответ != -1: находит следующую трубу после указанной
- Экспортирует данные THICKS в .raw файл

**ThickService → app.py (Python):**
- `POST /thick/upload` с .raw файлом
- Python анализирует через нейросеть
- Возвращает: classification, prediction_class, heatmap

**ThickService → UI:**
- Возвращает результат: pipeId, classification, heatmapImagePath, predictionClass

**UI:**
- Загружает тепловую карту через `GET /thick/heatmap/{pipeId}`
- Отображает на inspection.html

### 2. Решение оператора

**UI → DataService (через UI прокси):**
- `POST /api/save-decision`
- Данные: pipeId, predictedClass, operatorDecision (GOOD/DEFECT)

**DataService:**
- Сохраняет в таблицу RESULT базы DefectVKR
- Возвращает: { success: true/false, message: "..." }

**UI:**
- Если успех → `POST /thick/continue` с lastPipeId
- Если ошибка → показывает сообщение оператору

**ThickService (при continue):**
- Находит следующую трубу
- Повторяет цикл анализа

## Ключевые компоненты

### DataService (порт 8082)

**Новые файлы:**
- `PipeResult.java` - модель результата анализа
- `PipeResultRepository.java` - репозиторий для работы с RESULT
- `AnalysisResultService.java` - сервис для getLastAnalyzedPipeNumber() и saveAnalysisResult()

**Эндпоинты:**
- `GET /pipes/last-analyzed` - получить номер последней трубы
- `POST /pipes/save-result` - сохранить результат анализа

### ThickService (порт 8080)

**Изменения:**
- `ThickDataService.java` - добавлены методы:
  - `getLastAnalyzedPipeNumber()` - запрос к DataService
  - `findNextPipe(lastPipeNumber)` - поиск следующей трубы в DefectUNPO
  - `processNextPipe(lastPipeNumber)` - полный цикл обработки
- `ThickController.java` - добавлены эндпоинты:
  - `POST /thick/start` - начало работы
  - `POST /thick/continue` - продолжение работы
- `AnalysisResult.java` - добавлено поле predictionClass

### UI Module (порт 8083)

**Изменения:**
- `UiController.java` - добавлены прокси:
  - `POST /thick/start` - прокси к ThickService
  - `POST /thick/continue` - прокси к ThickService
  - `GET /thick/heatmap/{pipeId}` - прокси для тепловой карты
  - `POST /api/save-decision` - прокси к DataService
- `inspection.html` - обновлен JavaScript для работы с новой логикой

### app.py (Python FastAPI, порт 8001)

**Существующие эндпоинты:**
- `POST /thick/upload` - загрузка .raw файла и анализ
- `GET /thick/heatmap/{pipeId}` - получение тепловой карты

## Базы данных

**DefectUNPO (jdbc:derby://localhost:1527/DefectUNPO):**
- TMK.TUBERESULTS - данные толщинометрии (THICKS BLOB)
- TMK.TUBE - информация о трубах (THICKNOM)

**DefectVKR (jdbc:derby://localhost:1527/DefectVKR):**
- RESULT - таблица результатов анализа
  - ID BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY
  - PIPE_ID BIGINT NOT NULL
  - PREDICTED_CLASS INTEGER NOT NULL (0 - годно, 1 - брак)
  - OPERATOR_DECISION VARCHAR(50) NOT NULL (GOOD/DEFECT)
  - CREATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP

## Конфигурация

**application.properties (UI):**
```properties
auth.service.url=http://localhost:8081
thick.service.url=http://localhost:8080
data.service.url=http://localhost:8082
```

## Порядок запуска

1. Запустить Derby Database Server (порт 1527)
2. Запустить DataService (порт 8082)
3. Запустить ThickService (порт 8080)
4. Запустить app.py (порт 8001): `uvicorn app:app --host 0.0.0.0 --port 8001`
5. Запустить UI Module (порт 8083)
6. Открыть браузер: http://localhost:8083/login
