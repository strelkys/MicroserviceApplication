/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ru.npptmk.common.gwinstekdriver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import javax.swing.SwingWorker;
import java.util.List;

/**
 * Класс обеспечивает взаимодействие с блоким питания GWInstek модель ??????? по
 * протаколу TCP. По умолчанию используется порт 192.168.0.244:2268 Для предачи
 * значений текущего напряжения и токи на выходе блока следует получить ссылку
 * на объект с интерфейсом VCListener, используя метод setVCListener.<br>
 *
 * Подключение и базовый опрос реализованы при помощи SwingWorker.<br>
 * Первый класс реализует подключение и при корректном подключении вызывает
 * объект второго класса, который периодически запрашивает у блока питания
 * показания напряжения и силы тока и проаеряет подключение соккета. <br>
 * В случае отсутствия ответа от устройства более 5 раз при запросе значений
 * тока и напряжения возвращается -999.0
 *
 * @author StreltsovAE
 */
public class GWInstek {

    private Socket sc;                          // Соккет  для бмена данными с блоком питания
    private String addresIP;                    // Сетевой адрес  БП
    private final int port = 2268;              // Поток для подключения к соккету
    private int socketTimeOut;                  // Время ожидания ответа от соккета
    private final String deviceName = "GW-INSTEK";    // Начало имени устройства

    private VCListener vcl = null;              //Объект Listener с интерфейсом , для передачи показаний напряжения и тока
    private StatListener statLis = null;        //Объект Listener с интерфейсом , для контроля состояния подключения

    private BufferedReader in;                  //потоки для записи 
    private OutputStream out = null;            //и чтения  данных из соккета
    
    private int numberOfReconnections;          // Количество попыток при подключении
    private int measurementInterval;            //Шаг времени измерения
    private int reconnectTime;                  //Шаг времени проверки закрыт соккет или нет
    private String msg = null;                  // сообщение для отправки блоку питания,

    private Exception lastEx = null;            //Последняя ошибка
    private int step = 0;                       // Номер этапа работы  драйвера с устройством

    private final Object sndLock = new Object();// Для блокировки потока в ожидании

    /**
     * Конструктор для настройки соединения с устройством
     *
     * @param addresIP Адресс устройства для подключения
     */
    public GWInstek(String addresIP) {
        this(addresIP, 0);
    }

    /**
     * Конструктор для настройки соединения с устройством
     *
     * @param addresIP Адресс устройства для подключения
     * @param numberOfReconnections Количество попыток при подключении
     */
    public GWInstek(String addresIP, int numberOfReconnections) {

        this(addresIP, numberOfReconnections, 500, 1000, 500);
    }

    /**
     * Конструктор для настройки соединения с устройством
     *
     * @param addresIP Адресс устройства для подключения
     * @param numberOfReconnections Количество попыток при подключении
     * @param measurementInterval Шаг времени измерения
     * @param reconnectTime Шаг времени проверки закрыт соккет или нет
     * @param socketTimeOut Время ожидания ответа от соккета
     */
    public GWInstek(String addresIP, int numberOfReconnections,
            int measurementInterval, int reconnectTime, int socketTimeOut) {
        this.addresIP = addresIP;
        this.numberOfReconnections = numberOfReconnections;
        this.measurementInterval = measurementInterval;
        this.reconnectTime = reconnectTime;
        this.socketTimeOut = socketTimeOut;
    }

    /**
     * Метод запускает новый поток для подключения к сокету
     *
     */
    public final void startSC() {
        if (step == 0) {
            changeStep(1);
        }
    }

    /**
     * Устанавливает ссылку на объект с интерфейсом VCListener
     *
     * @param vcl объект с реализованным интерфейсом
     */
    public void setVCListener(VCListener vcl) {
        this.vcl = vcl;
    }

    /**
     * Устанавливает ссылку на объект с интерфейсом VCListener
     *
     * @param statLis объект с реализованным интерфейсом
     */
    public void setStatListener(StatListener statLis) {
        this.statLis = statLis;
    }

    /**
     * Метод возврщает последнюю ошибку Если ошибок небыло или подключение
     * востановленно то null
     *
     * @return последняя ошибка при работе драйвера
     */
    public Exception getLastEx() {
        return lastEx;
    }

    /**
     * Метод устанавливаетстатус драйвера в 0 - не запущен или выключен,
     *
     */
    public void stop() {
        changeStep(0);
    }

    /**
     * Метод возвращает значение напряжения
     *
     * @return значение напряжения установленного в устройстве
     * @throws java.io.IOException
     */
    private Double measurementVoltage() throws IOException {
        return Double.valueOf(socketQuery(":MEASure:VOLTage?"));
    }

    /**
     * Метод возвращает значение тока
     *
     * @return значение тока установленного в устройстве
     * @throws java.io.IOException
     */
    private Double measurementCurrent() throws IOException {
        return Double.valueOf(socketQuery(":MEASure:CURRent?"));
    }

    // Методы управления блоком питания прерывают ожидание потока 
    /**
     * Устанавливает выходной ток блока питания. Ожидаемое значение тока в
     * диапазоне от 0 до 10 ампер, если значение вне диапазона , то применяется
     * значение ближайшей граници.
     *
     * @param current требуемый уровень тока
     */
    public void setCurrentLevel(double current) {
        if (current < 0) {
            current = 0;
        } else if (current > 10) {
            current = 10;
        }
        msg = ":CURR " + current;
        synchronized (sndLock) {
            sndLock.notifyAll();
        }
    }

    /**
     * Методы управления блоком питания прерывают ожидание потока Метод включает
     * выход блока питания
     *
     */
    public void powerON() {
        msg = ":OUTP 1";
        synchronized (sndLock) {
            sndLock.notifyAll();
        }
    }

    /**
     * Методы управления блоком питания прерывают ожидание потока Метод
     * выключает выход блока питания
     *
     */
    public void powerOFF() {
        msg = ":OUTP 0";
        synchronized (sndLock) {
            sndLock.notifyAll();
        }
    }

    /**
     * Метод для отправки команды устройству, если соединение отсутствует или не
     * корректно статус драйвера устанавливается в 1 - подключение, что приведет
     * к повторному подключению к устройству и завершению текущего потока
     *
     * @param query Текстовый запрос
     * @return Текстовый ответ, при корректной обработке запроса.
     * @throws java.io.IOException
     */
    public String socketQuery(String query) throws IOException {
        out.write((query + "\n").getBytes("ASCII"));
        return in.readLine();
    }

    private void changeStep(int step) {

        this.step = step;

        switch (step) {
            case 0 -> {
                if (sc != null) {
                    try {
                        sc.close();
                    } catch (IOException ex) {
                    }
                }
                sc = null;
            }
            case 1 -> {

                if (sc != null) {
                    try {
                        sc.close();
                    } catch (IOException ex) {
                    }
                }
                sc = null;
                 (new GWISocketConnect()).execute();

            }
            case 2 -> {
                 (new GWISocketTranslator()).execute();
            }
        }
    }
    // Потоки для работы с устройством

    /**
     * Клас для запусска потока подключения к устройству
     */
    public class GWISocketConnect extends SwingWorker<Void, String> {

        private void publishMsg() throws Exception {
            if (statLis != null) {
                publish("");
            }
        }

        @Override
        protected synchronized Void doInBackground() throws Exception {
            int connectionNumber = 0;           // Текущее количество попыток подключения к устройству
            publishMsg();
            String devName;
            

            // Если статус 1 - подключение иначе 
            //публикуем текущий статус и завершаем 
            while (step == 1) {
                try {
                    //подключение с параметрами порта и адреса
                    sc = new Socket(InetAddress.getByName(addresIP), port);
                    sc.setSoTimeout(socketTimeOut);

                    // блокируем потоки
                    out = sc.getOutputStream();
                    in = new BufferedReader(
                            new InputStreamReader(sc.getInputStream()));
                    out.write(("*IDN?\n").getBytes("ASCII"));
                    //считываем имя устройства
                    devName = in.readLine();

                    // если имя не совпадает то закрываем соккет для переподключения
                    if (!devName.startsWith(deviceName)) {
                        try {
                            sc.close();
                        } catch (IOException ex) {
                        }
                        sc = null;
                        lastEx = new TheWrongDeviceIsConnected(deviceName, devName);
                        //иначе подключение верное, запускаем поток для работы с запросами
                    } else {
                        connectionNumber = 0;
                        lastEx = null;
                        changeStep(2);
                        // сообщаем слушателю об удачном подключении
                        publishMsg();
                        return null;
                    }
                } catch (IOException ex) { // ошибка подключения
                    lastEx = ex;
                }

                connectionNumber++;

                if (numberOfReconnections > 0) {
                    if (connectionNumber > numberOfReconnections) {
                        changeStep(0);
                    }
                }
                try {
                    Thread.sleep(reconnectTime);
                } catch (InterruptedException ex) {
                }
            }
            publishMsg();
            if (statLis == null && lastEx != null) {
                throw lastEx;
            }
            return null;
        }

        @Override
        protected void process(List<String> chunks) {
            statLis.setGWConnectionStatus(step, lastEx);
        }

    }

    /**
     * Предает показания объекту с реализованным интерфейсом Listener
     *
     */
    public class GWISocketTranslator extends SwingWorker<Void, String> {

        private double v = -999.0;                  //напряжение на выходе блока питания
        private double cur = -999.0;                // сила тока
        private boolean offOn = false;              // статус блока питания

        @Override
        protected Void doInBackground() {
            // пока не изменился этап 
            // периодически опрашиваем устройство
            while (step == 2) {
                try {
                    //Обработка команды , если есть
                    if (msg != null) {
                        out.write((msg + "\n").getBytes("ASCII"));
                        msg = null;
                    }
                    //Запросы параметров блока питания
                    v = measurementVoltage();
                    cur = measurementCurrent();
                    offOn = "1".equals(socketQuery(":OUTP?"));
                } catch (IOException ex) {
                    v = cur = -999.0;
                    changeStep(1);
                }
                publish("");
                // таймаут между опросами, может быит прерван ,
                //если есть команда
                synchronized (sndLock) {
                    try {
                        sndLock.wait(measurementInterval);
                    } catch (InterruptedException ex) {
                    }
                }
            }
            return null;
        }

        /**
         * Передаем текущие показания прибора слушателю если он есть
         *
         * @param chunks
         */
        @Override
        protected void process(List<String> chunks) {
            if (vcl != null) {
                vcl.setVoltageAndCurrent(v, cur, offOn);
            }
        }
    }
}
