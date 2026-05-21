/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.npptmk.common.defectoscope;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import javax.swing.SwingWorker;
import org.apache.log4j.Logger;
import ru.npptmk.common.gui.GUIUpdater;

/**
 * Абстрактный базовый класс для драйверов сканирующих устройств, работающих по
 * протоколу UDP.<br>
 * <br>
 * Общая схема взаимодействия с устройством следующая:<br>
 * <br>
 * От прикладной программы устройству передаются команды в виде UDP пакетов
 * фиксированного размера. В ответ на команду от устройства поступают результаты
 * сканирования или измереия в реальном времени в виде UDP пакетов
 * фиксированного размера. Данные от устройства поступают сериями по несколько
 * пакетов. Количество пакетов в серии (или ее продожительность) определено
 * настройками устройства.
 * <br>
 * Каждый пакет в серии содержит результаты, относящиеся к моменту времени,
 * непосредственно предшествовавшему моменту отправки пакета. Таким образом, все
 * пакеты в серии содержат различные данные, относящиеся к разным моментам
 * времени. Привязка к моменту времени содержится внутри данных пакета
 * (порядковый номер, время и т.д.).<br>
 * Помимо результатов измерения один из ответных пакетов серии может содержаить
 * результат выполнения команды, переданной от прикладной программы устройству,
 * если таковой существует. Если в серии такого пакета нет, значит команда
 * устройством не воспринята.<br>
 * Если новая команда от прикладной программы передается на устройство до
 * завершения передачи устройством ответной серии предыдущей команды, то с
 * момента получения новой команды, устройство начинает передачу новой ответной
 * серии, при этом предыдущая серия завершается, а результаты, полученные после
 * поступления новой команды помещаются в ее ответную серию. Таким образом, для
 * поддержания непрерывного потока результатов измерения необходимо отправлять
 * устройству команды с интервалом времени меньшим, чем продолжительность
 * серии.<br>
 * Данный класс не производит никакой обработки команд он обеспечивает только их
 * передачу на устройство и прем ответа. Фрмирование команд происходит в
 * абстрактом меиоде getCommand(). Его нужно реализовать в суперкласе.
 * <br>
 *
 * Класс реализует следующий функционал:
 * <ol>
 * <li> Создание объекта драйвера с помощью конструктора, принимающего в
 * качестве параметров адреса локального и удаленного сокета, через которые
 * ведется обмен данными, продолжительность серии в милисекундах и интервал
 * отправки повторяемых команд.
 * <li> Запуск постоянно функционирующих потоков передачи команд устройству,
 * приема даных от устройства и сохранения результатов статистической обработки.
 * <li> вызов метода формирования команды и отправка сформированной команды с
 * заданной периодичностью.
 * <li> Прием от устройства серий пакетов и вызов обработчика принятых данных.
 * <li> Завершение работы и закрытие всех созданых сокетов.
 * </ol>
 *
 * В классе реализованы следующие блокировки работы:<ol>
 * <li> Поток отправки команд и пакетов для поддержания работы устройства
 * организован в виде цикла, завершаюшего работу при вызове метода {@code stop},
 * блокируется секцией {@code sndLock} на время заданное интервалом отправки
 * повторяемых команд. <br>
 * Эта секция сбрасывается при вызове метода {@code setCommand} и при остановки
 * работы драйвера.
 * <li> Поток приема данный от устройства блокируется методом
 * {@code DatagramSocket.receive}, на время длительности ответной серии.
 * <li> Поток отправки данных внешней программе, организован в виде цикла,
 * завершаюшего работу при вызове метода {@code stop}, блокируется секцией
 * {@code pointLock}. <br>
 * Эта секция сбрасывается при вызове метода {@code newPointReady}, этот метод
 * должен быть вызван внешней программой после завершения накопления буфера
 * АСкана.<br>
 *
 * </ol>
 *
 * @author MalginAS
 */
public abstract class GeneralUDPDevice extends SwingWorker<Object, GUIUpdater> {

    private static final Logger LOG = Logger.getLogger(GeneralUDPDevice.class);

    /**
     * Нет ошибок.
     */
    public static final int RC_OK = 0;
    /**
     * Возник SocketException при создании сокета.
     */
    public static final int RC_SOCKETEXCEPTION = 1;
    /**
     * Возник IOException при создании сокета.
     */
    public static final int RC_IOEXCEPTION = 2;
    /**
     * Неправильные параметры драйвера.
     */
    public static final int RC_ERRPARAMS = 3;
    /**
     * Статус разовой команды. Команда отправлена на устройство, ответа пока
     * нет.
     */
    public static final int CS_INPROGRESS = -1;
    /**
     * Статус разовой команды. Получен ответ от устройства на разовую команду.
     */
    public static final int CS_OK = 0;
    /**
     * Статус разовой команды. Истекло время ожидания ответа на команду.
     */
    public static final int CS_NORESPONCE = 1000;

    private final long serTime;       // Прдолжительность серии в милисекунтах
    private final long sendTime;      // Интервал отправки повторяемых команд.
    private final InetSocketAddress local;                // Локальный сокет
    private final InetSocketAddress remote;               // Удаленный сокет.
    private DatagramSocket sock = null;
    private final byte[] rcvBuf;                      // Буфер для приема данных
    private DatagramPacket rcvPack;             // Принятый пакет.

    private boolean isStarted = false;          // флаг цикла потоков драйвера.
    /**
     * Имя драйвера для добавления к именам потоков чтеня, отправки и обработки
     * данных.
     */
    public String driverName = "DefaultUDP"; // Имя драйвера

    private int error;
    private String errMessage;

    private long sended = 0;                 // Количество отправленных повторяемых команд
    private long received = 0;                  // Количество принятых пакетов.

    private Thread dgSnd = null;                        // Поток отправки пакетов
    private Thread dgRcv = null;                        // Поток приема пакетов
    private final Object sndLock = new Object();        // Блокировка цикла отправки
    public final int sendSize;

    /**
     * Конструктор драйвера устройства.
     *
     * @param serTime длительность серии ответных пакетов в милисекундах.
     * @param sendTime интервал отправки повторяемых команд в милисекундах.
     * @param local локальный сокет.
     * @param remote удаленный сокет.
     * @param size размер пакета принимаемых, отправляемых данных
     */
    public GeneralUDPDevice(long serTime, long sendTime, InetSocketAddress local, InetSocketAddress remote, int size) {
        this.serTime = serTime;
        this.sendTime = sendTime;
        this.local = local;
        this.remote = remote;
        sendSize = size;
        rcvBuf = new byte[size];
    }

    /**
     * Сброс ошибочного состояния. Вызывается после успешной обработки ошибочной
     * ситуации.
     */
    public void resetError() {
        error = RC_OK;
        errMessage = null;
    }

    /**
     * Возвращает код последней ошибки. Действующие коды описаны в виде констант
     * с префиксом RC_ данного класса.
     *
     * @return код последней ошибки.
     */
    public int getError() {
        return error;
    }

    /**
     * Возвращает строку с описанием ошибки,
     *
     * @return Опсание последней ошибки или {@code  null} если ошибки нет.
     */
    public String getErrMessage() {
        return errMessage;
    }

    /**
     * Запуск драйвера.<br>
     * Создается сокет и запускаются потоки отправки и према пакетов.
     */
    public void start() {
        if (sock != null) {
            return;
        }
        resetError();
        if (local == null) {
            error = RC_ERRPARAMS;
            LOG.error("Не задан адрес локального сокета");
            sock = null;
            return;
        }
        if (remote == null) {
            error = RC_ERRPARAMS;
            LOG.error("Не задан адрес удаленного сокета");
            sock = null;
            return;
        }
        if (serTime == 0) {
            error = RC_ERRPARAMS;
            LOG.error("Не задано время серии");
            sock = null;
            return;
        }
        if (sendTime == 0) {
            error = RC_ERRPARAMS;
            LOG.error("Не задан интервал отправки повторяемых команд");
            sock = null;
            return;
        }
        if (rcvBuf.length == 0) {
            error = RC_ERRPARAMS;
            LOG.error("Не задан размер принимаемых пакетов");
            sock = null;
            return;
        }
        try {
            sock = new DatagramSocket(local);
        } catch (SocketException ex) {
            error = RC_SOCKETEXCEPTION;
            LOG.error("SocketException при создании: ", ex);
            sock = null;
            return;
        }
        try {
            sock.setSoTimeout((int) serTime);
        } catch (SocketException ex) {
            error = RC_SOCKETEXCEPTION;
            LOG.error("SocketException при установке Timeout: ", ex);
            sock = null;
            return;
        }
        rcvPack = new DatagramPacket(rcvBuf, rcvBuf.length);
        // Поток отправки команд.
        isStarted = true;
        sended = 0;
        received = 0;
        dgSnd = new Thread(() -> {
            sender();
        }, "UDPSender " + driverName);
        dgSnd.start();
        // Поток приема команд.
        dgRcv = new Thread(() -> {
            receiver();
        }, "UDPReceiver " + driverName);
        ThreadGroup tg = dgRcv.getThreadGroup();
        dgRcv.setPriority(tg.getMaxPriority());
        dgRcv.start();
    }

    /**
     * Остановка драйвера и закрытие сокета.
     */
    public void stop() {
        resetError();
        isStarted = false;
        synchronized (sndLock) {
            sndLock.notifyAll();
        }
        if (dgRcv != null) {
            try {
                dgRcv.join();
            } catch (InterruptedException ex) {
            }
        }
        if (dgSnd != null) {
            try {
                dgSnd.join();
            } catch (InterruptedException ex) {
            }
        }
        if (sock != null) {
            sock.close();
        }
        sock = null;
    }

    /**
     * Возвращает количество отправленных повторяемых команд.
     *
     * @return количество отправленных повторяемых команд с момента старта
     * драйвера.
     */
    public long getSended() {
        return sended;
    }

    /**
     * Возвращает количество принятых пакетов данных
     *
     * @return количество пакетов даннх принятых с момента запуска драйвера.
     */
    public long getReceived() {
        return received;
    }

    /**
     * Возвращает адрес локального сокета.
     *
     * @return адрес локального сокета.
     */
    public InetSocketAddress getLocal() {
        return local;
    }

    /**
     * Возвращает адрес удаленного сокета.
     *
     * @return адрес удаленного сокета.
     */
    public InetSocketAddress getRemote() {
        return remote;
    }

    /**
     * Обработчик поступившего пакета.<br>
     * Этот метод вызывается при поступлении нового пакета от устройства.Он
     * должен реализовать разбор поступившего пакета, накопление результатов для
     * их последующей статистической обработки.
     *
     * @param ris Поток принятых данных.
     */
    public abstract void newDataAvailable(RevDataInputStream ris);

    /**
     * Метод формирования команды на отправку.<br>
     * Вызывается из потока отправки данных для формирования очередной команды.
     *
     * @return Сформированный буфер с командой для отправки. Если возвращен
     * {@code null}, то отправка команды не производится.
     */
    public abstract DatagramPacket getCommand();

    /**
     * Поток отправки пакетов
     */
    private void sender() {
        while (isStarted) {
            DatagramPacket pts = getCommand();
            if (pts != null) {
                try {
                    sock.send(pts);
                    sended++;
//                    System.out.println("Отправлено " + sended
//                            + " " + pts.getData()[10]
//                            + " " + pts.getData()[11]
//                            + " " + pts.getData()[12]
//                            + " " + pts.getData()[13]);
                } catch (IOException ex) {
                    error = RC_IOEXCEPTION;
                    LOG.error("IOException при отправке пакета: ", ex);
                }
            }
            synchronized (sndLock) {
                try {
                    sndLock.wait(sendTime);
                } catch (InterruptedException ex) {
                }
            }
        }
    }

    // Иницирует отправку команды.
    public void sendCmd() {
        synchronized (sndLock) {
            sndLock.notifyAll();
        }
    }

    /**
     * Поток приема пакетов
     */
    private void receiver() {
        RevDataInputStream ris = new RevDataInputStream(rcvBuf);
        while (isStarted) {
            try {
                ris.resetPos();
                sock.receive(rcvPack);
//                System.out.println("Принято " + rcvBuf.length + " байт " + received);
                if (rcvPack.getLength() == rcvBuf.length) {
                    // Хороший пакет. обрабатываем.
                    received++;
                    newDataAvailable(ris);
                }
            } catch (IOException ex) {
            }
        }
    }

}
