/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ru.npptmk.common.defectoscope;

import java.util.Arrays;

/**
 * Класс для регистрации координат во время сканирования.
 *
 * @author MalginAS
 */
public class CoordData {

    private long startTime;                     // Системное время начала сканирования
    private int coorInd;                        // Индекс текущего занчения координаты
    private final int[] coorTime;      // Массив отметок времени получения координат
    private final double[] coord;   // Массив координат

    public CoordData(int size) {
        coorTime = new int[size];
        coord = new double[size];
    }

    public void start(long startTime) {
        this.startTime = startTime;
        coorInd = 0;
        Arrays.fill(coorTime, 0);
        Arrays.fill(coord, 0.0);
    }

    public void addCoord(long time, double coor) {
        coorTime[coorInd] = (int) (time - startTime);
        coord[coorInd] = coor;
        coorInd++;
        if (coorInd >= coorTime.length) {
            coorInd = coorTime.length - 1;
        }
    }

    public long getStartTime() {
        return startTime;
    }

    public int getCoorInd() {
        return coorInd;
    }

    /**
     * Возвращвет координату, соответствующую заданному времени.
     *
     * @param time Время с момента начала сканирования
     * @return Значение координаты в указанное время.
     */
    public double getCoord(double time) {
        if (coorInd > 0) {
            // Удаляем из хвоста нулевые коорждинаты, если таковые есть.
            while (coord[coorInd - 1] == 0.0) {
                coorInd--;
                if (coorInd == 0){
                    return coord[0];
                }
            }
        }
        int ind = Arrays.binarySearch(coorTime, 0, coorInd, (int)Math.round(time));
        if (ind >= 0) {
            return coord[ind];
        }
        ind = (-ind) -1;
        if (ind == 0) {
            return coord[0];
        }
        if (ind == coorInd) {
            return coord[ind - 1];
        }
        return coord[ind - 1] + (coord[ind] - coord[ind - 1]) * (time - coorTime[ind - 1]) / (coorTime[ind] - coorTime[ind - 1]);
    }

}
