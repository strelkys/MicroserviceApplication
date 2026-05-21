/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.npptmk.common.defectoscope;

import java.io.Serializable;

/**
 * Класс результатов сканирования толщины трубы.<br>
 * Представляет собой расширение класса DefectResults для случая хранения
 * вещественных величин, находящизся в заданном диапазоне.
 *
 * @author MalginAS
 */
public class ThickResults extends DefectResults implements Serializable {
    private static final long serialVersionUID = 1566865691167615133L;   
    private double min; //Нижняя границатолщины. Значения менее игнорируются
    private double max; //Верхняя граница толщины. Значения более приравниваются этой границе.

    public ThickResults(double xSize, double ySize, int nX, int nY, int nChan, int nRanges) {
        super(xSize, ySize, nX, nY, nChan, nRanges);
    }

    public void setRange(double min, double max) {
        this.min = min;
        this.max = max;
    }

    /**
     * Вычисляет индекс диапазона для значения.
     */
    private int getRangeIndex(double val) {
        int nrg = counts[0][0][0].length;
        int ir = (int) (((val - min) / (max - min)) * nrg);
        if (ir == nrg) {
            ir--;
        }
        return ir;
    }

    /**
     * Добавление значения в результаты сканирования<br>
     * Применяется для фиксации результатов толщинометрии.
     *
     * @param val Результат сканирования отнормированный относительно порога.
     * @param x X координата в зоне сканирования.
     * @param y Y координата в зоне сканирования.
     * @param chan Индекс канала.
     */
    @Override
    public void addValue(double val, double x, double y, int chan) {
        if (val < min) {
            return;
        }
        if (x > getxSize() || x<0){
            // Отсекаем заведомо ошибочные кооринаты.
            return;
        }
        if (y > getySize() || y<0){
            // Отсекаем заведомо ошибочные кооринаты.
            return;
        }
        if (val > max) {
            val = max;
        }
        int ix = (int) (x * counts.length / getxSize());
        if (ix >= counts.length) {
            ix = counts.length - 1;
        }
        int iy = (int) (y * counts[0].length / getySize());
        if (iy >= counts[0].length) {
            iy = counts[0].length - 1;
        }
        counts[ix][iy][chan][getRangeIndex(val)]++;
    }

    /**
     * Получает среднее значенние для указанной точки и канала.
     *
     * @param iX Индекс ячейки по X
     * @param iY Индекс ячейки по Y
     * @param chan Номер канала
     * @return Среднее значение канала.
     */
    public double getAvgVal(int iX, int iY, int chan) {
        double sum = 0;
        double n = 0;
        double delta = (max - min) / counts[0][0][0].length;
        double mul = min + delta / 2;
        for (short cn : counts[iX][iY][chan]) {
            n += cn;
            sum += (mul * cn);
            mul += delta;
        }
        if (n != 0) {
            sum /= n;
        }
        return sum;
    }

    /**
     * Получает среднее значенние для указанной точки.
     *
     * @param iX Индекс ячейки по X
     * @param iY Индекс ячейки по Y
     * @return Среднее значение канала.
     */
    public double getAvgVal(int iX, int iY) {
        double sum = 0;
        double n = 0;
        double delta = (max - min) / counts[0][0][0].length;
        for (short[] cn1 : counts[iX][iY]) {
            double mul = min + delta / 2;
            for (short cn : cn1) {
                n += cn;
                sum += (mul * cn);
                mul += delta;
            }
        }
        if (n != 0) {
            sum /= n;
        }
        return sum;
    }

    @Override
    public ThickResults copy(double len){
        double step = getxSize()/(counts.length);
        int xLen = (int)Math.round(len/step)+1;
        ThickResults cop = new ThickResults(xLen*step, getySize(), xLen, 
                counts[0].length, counts[0][0].length, counts[0][0][0].length);
        for (int i = 0; i<xLen; i++){
            for (int j=0; j<counts[i].length; j++){
                for (int k=0; k<counts[i][j].length; k++){
                    System.arraycopy(counts[i][j][k], 0, cop.counts[i][j][k], 0, counts[i][j][k].length);
                }
            }
        }
        cop.setRange(min, max);
        return cop;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }
    
    
}
