/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.npptmk.common.defectoscope;

/**
 * Поставщик координат для результатов сканирования.<br>
 * Интерфейс используется для определения координат при формировании
 * бллока результатов сканирования.
 * @author MalginAS
 */
public interface ICoordProvider {
    /**
     * Возвращает координату для заданного момента времени.
     * @param time Системное время для которого требуется определить
     * координату.
     * @return Координата в м., соответстующая заданному времени.
     */
    public double getCoord(long time);
}
