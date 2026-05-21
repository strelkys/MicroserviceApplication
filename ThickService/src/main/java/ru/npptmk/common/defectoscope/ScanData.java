/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.npptmk.common.defectoscope;

import java.util.Arrays;
import ru.npptmk.common.gui.GUIUpdater;

/**
 * Класс данных с результатами сканирования. <br>
 * Содержит массивы результатов сканирования по всем
 * каналам устройства в заданном диапазоне времени. Предполагается, что 
 * результаты сканирования каждого канала распределены в заданном
 * диапазоне времен равномерно.  Каждое значение результатов
 * представлено в виде целого положительного числа типа {@code short}. Значение 
 * -1 означает отсутствие данных в этой точке времени.<br>
 * 
 * @author MalginAS
 */
public class ScanData extends GUIUpdater {
    private long begTime;       // Время первого результата 
    private double begCoord = 0.0;    // Координата первого результата
    private long endTime;       // Время последнего результата.
    private double endCoord = 0.0;    // Координата последнего результата
    private final short[][] data;   // Буфер данных
    private int curDt = 0;          // Текущий индекс элемента
                                    // в массиве результатов
    public ScanData(int sourceId,int nChan, int nData) {
        super(sourceId);
        data = new short[nChan][nData];
        for (int i=0; i<nChan; i++){
            Arrays.fill(data[i], (short)-1);
        }
    }
    /**
     * Добавляет в текущий объект новые данные.<br>
     * Алгоритм обработки:<br>
     * Вновь поступившие данные поканально переписываются в объект.Если вновь поступивших данных больше, чем свободных ячеек в 
 объекте, то переписывается только начало массива и возвращается
 смещение для первого незаписанного элемента, для последующей записи его
 в новый объект.<br>
 После записи массива определяется время для последнего жлемента
 исходя из предположения о равномерном поступлении данныз по времени.
     * 
     * @param time Значение времени для первого добавляемого элемента
     * @param offset Смещение первого добавляемого от начала исходного массива
     * данных.
     * @param nRez Количество значимых результатов для каждого канала.
     * @param dat Массив результатов. Первый индекс - номер канала, второй -
     * номер результата в канале.
     * @param crp Поставщик координат.
     * @return Смещение до первого непоместившегося в объект элемента данных.
     * Значение -1 означает, что все данные поместились в объект.
     */
    public int addData(long time, int offset, short[] nRez, short[][] dat, ICoordProvider crp){
        int maxlen = 0;
        int newOff = -1;
        int free = data[0].length - curDt;
        for (int i=0; i<nRez.length; i++){
            int len = nRez[i]-offset;
            if (len > free){
                newOff = free+offset;
                len = free;
            }
            if (len>maxlen){
                maxlen = len;
            }
            if (len>0){
                System.arraycopy(dat[i], offset, data[i], curDt, len);
            }
        }
        if (curDt == 0){
            begTime = time;
            if (crp != null){
                begCoord = crp.getCoord(time);
            }
        } else {
            endTime = time +(long)(((double)(time-begTime)*maxlen)/(double)curDt);
            if (crp != null){
                endCoord = crp.getCoord(endTime);
            }
        }
        curDt += maxlen;
        return newOff;
    }

    /**
     * Возврящает метку времни для первого элемента в масиве
     * @return Время в милисекундах для первого элемента данных.
     */
    public long getBegTime() {
        return begTime;
    }

    /**
     * Возвращает метку времени для последнего жлемента данных.
     * @return Время в милисекундах для последнего элемента данных.
     */
    public long getEndTime() {
        return endTime;
    }

    /**
     * Возвращает массив данных.
     * @return Массив результатов сканирования. Первый индекс - номер канала,
     * второй индекс - номер точки данных.
     */
    public short[][] getData() {
        return data;
    }

    /**
     * Возвращает количество элементов данных в массиве.
     * @return Количество заполненных элементов данных.
     */
    public int getCurDt() {
        return curDt;
    }
    /**
     * Возвращает признак заполненности объекта данными
     * @return {@code true} - если объект заполнен данными.
     */
    public boolean isFull(){
        return (curDt>=data[0].length);
    }

    /**
     * Координата первого результата
     * @return Координата первого результата в массиве.
     */
    public double getBegCoord() {
        return begCoord;
    }

    /**
     * Координата последнего результата.
     * @return Координата последнего результата в массиве.
     */
    public double getEndCoord() {
        return endCoord;
    }
    
}
