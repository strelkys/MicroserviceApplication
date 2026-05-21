package ru.npptmk.common.defectoscope;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Двумерная матрица результатов сканирования.<br>
 * Вся область сканирования расномерно разбита на ячейки вдоль осей X и Y. Для
 * каждой ячейки хранится поканальный набор гистограмм результатов сканирования.
 * Гистограмма каждого канала содержит количество результатов сканирования в
 * каждом диапазоне. количество диапазонов - 10. Результаты каждого канала
 * должны представлять вещественное число. Значение 1.0 - соответсвует порогу
 * дефекта. Гистограмма строится ддля следующих диапазонов значений:
 * <ul>
 * <li> 0 диапазон для {@code v<0.5};
 * <li> 1 диапазон для {@code 0.5>=v<0.6};
 * <li> 2 диапазон для {@code 0.7>=v<0.8};
 * <li> 3 диапазон для {@code 0.8>=v<0.9};
 * <li> 4 диапазон для {@code 0.9>=v<1.0};
 * <li> 5 диапазон для {@code 1.0>=v<1.1};
 * <li> 6 диапазон для {@code 1.1>=v<1.2};
 * <li> 7 диапазон для {@code 1.2>=v<1.3};
 * <li> 8 диапазон для {@code 1.3>=v<1.4};
 * <li> 9 диапазон для {@code 1.4>=v};
 * </ul>
 * Гистограммы могут быть использованя для формирования графического
 * представления результатов сканирования.
 *
 * 
 */
public class DefectResults implements Serializable {

    private final double xSize;       // Размер области сканирования по оси X
    private final double ySize;       // Размер области сканирования по оси Y
    protected final short counts[][][][];   //Результаты сканирования.
    // Порядок следования индексов :
    // - индекс ячейки по X
    // - индекс ячейки по Y
    // - индекс канала
    // - индекс лиапазона
    
    
    private static final long serialVersionUID = -7170285638577006117L;
    
    
    public short[][][][] getCounts() {
        return counts;
    }

    /**
     * Конструктор результатов сканирования
     *
     * @param xSize Размер области сканирования по оси X
     * @param ySize Размер области скнирования по оси Y
     * @param nX Количество ячеек вдоль оси X.
     * @param nY Количество ячеек вдоль оси Y
     * @param nChan Количество каналов.
     */
    public DefectResults(double xSize, double ySize, int nX, int nY, int nChan) {
        this.xSize = xSize;
        this.ySize = ySize;
        counts = new short[nX][nY][nChan][10];
        reset();
    }

    /**
     * Конструктор результатов сканирования для толщинометрии.
     *
     * @param xSize Размер области сканирования по оси X
     * @param ySize Размер области скнирования по оси Y
     * @param nX Количество ячеек вдоль оси X.
     * @param nY Количество ячеек вдоль оси Y
     * @param nChan Количество каналов.
     * @param nRanges Количество диапазонов значений величины.
     */
    public DefectResults(double xSize, double ySize, int nX, int nY, int nChan, int nRanges) {
        this.xSize = xSize;
        this.ySize = ySize;
        counts = new short[nX][nY][nChan][nRanges];
        reset();
    }

    /**
     * Вычисляет индекс диапазона для значения.
     */
    private int getRangeIndex(double val) {
        if (val < 0.5) {
            return 0;
        }
        if (val >= 1.4) {
            return 9;
        }
        int ir = (int) ((val - 0.5) * 10.0) + 1;
        return ir;
    }

    /**
     * Добавление значения в результаты сканирования
     *
     * @param val Результат сканирования отнормированный относительно порога.
     * @param x X координата в зоне сканирования.
     * @param y Y координата в зоне сканирования.
     * @param chan Индекс канала.
     */
    public void addValue(double val, double x, double y, int chan) {
        if (x > getxSize() || x < 0) {
            // Отсекаем заведомо ошибочные кооринаты.
            return;
        }
        if (y > getySize() || y < 0) {
            // Отсекаем заведомо ошибочные кооринаты.
            return;
        }
        int ix = (int) (x * counts.length / xSize);
        if (ix >= counts.length) {
            ix = counts.length - 1;
        }
        int iy = (int) (y * counts[0].length / ySize);
        if (iy >= counts[0].length) {
            iy = counts[0].length - 1;
        }
        counts[ix][iy][chan][getRangeIndex(val)]++;
    }

    /**
     * Зануление результатов.
     */
    public final void reset() {
        for (short cnt3[][][] : counts) {
            for (short cnt2[][] : cnt3) {
                for (short cnt1[] : cnt2) {
                    Arrays.fill(cnt1, (short) 0);
                }
            }
        }
    }

    /**
     * Возвращает количество ячеек вдоль оси X
     *
     * @return Количество ячеек вдоль оси X
     */
    public int getXQt() {
        return counts.length;
    }

    /**
     * Возвращает количество ячеек вдоль координаты Y
     *
     * @return Количество ячеек вдоль координаты Y
     */
    public int getYQt() {
        if (counts.length > 0) {
            return counts[0].length;
        }
        return 0;
    }

    /**
     * Возвращает общее количество каналов в одной ячейке.
     *
     * @return Количество каналов в ячейке.
     */
    public int getChanQt() {
        if (counts.length > 0) {
            if (counts[0].length > 0) {
                return counts[0][0].length;
            }
        }
        return 0;
    }

    /**
     * Возвращяет размер области сканирования вдоль оси X
     *
     * @return Размер области сканирования вдоль оси X
     */
    public double getxSize() {
        return xSize;
    }

    /**
     * Возвращает размер области сканирования вдоль оси Y
     *
     * @return размер области сканирования вдоль оси Y.
     */
    public double getySize() {
        return ySize;
    }

    /**
     * Возвращает общее количество значений в ячейке для указанного канала
     *
     * @param iX индекс ячейки по оси X
     * @param iY индекс ячейки по оси Y
     * @param chan номер канала.
     * @return Общее количество значений в ячейке для заданного канала.
     */
    public int getTotalVals(int iX, int iY, int chan) {
        int cnt = 0;
        if (chan>=getChanQt()){
            return 0;
        }
        for (int i = 0; i < counts[0][0][0].length; i++) {
            cnt += counts[iX][iY][chan][i];
        }
        return cnt;
    }

    /**
     * Возвращает общее количество значений в ячейке по всем каналам
     *
     * @param iX индекс ячейки по оси X
     * @param iY индекс ячейки по оси Y
     * @return Общее количество значений в ячейке.
     */
    public int getTotalVals(int iX, int iY) {
        int cnt = 0;
        for (int i = 0; i < getChanQt(); i++) {
            for (int j = 0; j < counts[0][0][0].length; j++) {
                cnt += counts[iX][iY][i][j];
            }
        }
        return cnt;
    }

    /**
     * Возвращает количество значений выше порога для данной точки и данного
     * канала
     *
     * @param iX индекс ячейки по оси X
     * @param iY индекс ячейки по оси Y
     * @param chan номер канала.
     * @return Количество значений в ячейке равное, либо превышающее порог
     */
    public int getBigVals(int iX, int iY, int chan) {
        int cnt = 0;
        if (chan >= getChanQt()) {
            return 0;
        }
        for (int i = 5; i < 10; i++) {
            cnt += counts[iX][iY][chan][i];
        }
        return cnt;
    }

    /**
     * Возвращает количество значений выше порога для данной точки по всем
     * каналам
     *
     * @param iX индекс ячейки по оси X
     * @param iY индекс ячейки по оси Y
     * @return Количество значений в ячейке равное, либо превышающее порог
     */
    public int getBigVals(int iX, int iY) {
        int cnt = 0;
        for (int i = 0; i < getChanQt(); i++) {
            for (int j = 5; j < 10; j++) {
                cnt += counts[iX][iY][i][j];
            }
        }
        return cnt;
    }

    /**
     * Возвращает количество значений порога для данной точки и данного канала
     *
     * @param iX индекс ячейки по оси X
     * @param iY индекс ячейки по оси Y
     * @param chan номер канала.
     * @return Количество значений в ячейке не превышающее порог
     */
    public int getSmallVals(int iX, int iY, int chan) {
        int cnt = 0;
        if (chan >= getChanQt()) {
            return 0;
        }
        for (int i = 0; i < 5; i++) {
            cnt += counts[iX][iY][chan][i];
        }
        return cnt;
    }

    /**
     * Возвращает количество значений ниже порога для данной точки по всем
     * каналам
     *
     * @param iX индекс ячейки по оси X
     * @param iY индекс ячейки по оси Y
     * @return Количество значений в ячейке не превышающее порог
     */
    public int getSmallVals(int iX, int iY) {
        int cnt = 0;
        for (int i = 0; i < getChanQt(); i++) {
            for (int j = 0; j < 5; j++) {
                cnt += counts[iX][iY][i][j];
            }
        }
        return cnt;
    }

    /**
     * Возвращает сумму значений превышающих порог для данной точки и данного
     * канала
     *
     * @param iX индекс ячейки по оси X
     * @param iY индекс ячейки по оси Y
     * @param chan номер канала.
     * @return Сумма значений в ячейке не превышающее порог
     */
    public double getBigSumm(int iX, int iY, int chan) {
        double sum = 0.0;
        double mult = 1.05;
        if (chan >= getChanQt()) {
            return 0.0;
        }
        for (int i = 5; i < 10; i++) {
            sum += counts[iX][iY][chan][i] * mult;
            mult += 0.1;
        }
        return sum;
    }

    /**
     * Возвращает сумму значений превышающих порог для данной точки
     *
     * @param iX индекс ячейки по оси X
     * @param iY индекс ячейки по оси Y
     * @return Сумма значений в ячейке превышающее порог
     */
    public double getBigSumm(int iX, int iY) {
        double sum = 0.0;
        for (int i = 0; i < getChanQt(); i++) {
            double mult = 1.05;
            for (int j = 5; j < 10; j++) {
                sum += counts[iX][iY][i][j] * mult;
                mult += 0.1;
            }
        }
        return sum;
    }

    /**
     * Возвращает сумму значений превышающих порог для данной точки и данного
     * канала
     *
     * @param iX индекс ячейки по оси X
     * @param iY индекс ячейки по оси Y
     * @param chan номер канала.
     * @return Сумма значений в ячейке не превышающее порог
     */
    public double getSmallSumm(int iX, int iY, int chan) {
        double sum = 0.0;
        double mult = 0.55;
        if (chan >= getChanQt()) {
            return 0.0;
        }
        for (int i = 0; i < 5; i++) {
            sum += counts[iX][iY][chan][i] * mult;
            mult += 0.1;
        }
        return sum;
    }

    /**
     * Возвращает сумму значений превышающих порог для данной точки
     *
     * @param iX индекс ячейки по оси X
     * @param iY индекс ячейки по оси Y
     * @return Сумма значений в ячейке превышающее порог
     */
    public double getSmallSumm(int iX, int iY) {
        double sum = 0.0;
        for (int i = 0; i < getChanQt(); i++) {
            double mult = 0.55;
            for (int j = 0; j < 5; j++) {
                sum += counts[iX][iY][i][j] * mult;
                mult += 0.1;
            }
        }
        return sum;
    }

    /**
     * Возвращает максимальное количество значений в столбце гистограммы.
     *
     * @return
     */
    public short getMaxCnt(int ix, int iy) {
        short maxCnt = 5;
        for (short[] cnt3 : counts[ix][iy]) {
            for (short cn : cnt3) {
                if (cn > maxCnt) {
                    maxCnt = cn;
                }
            }
        }
        return maxCnt;
    }

    public DefectResults copy(double len) {
        double step = xSize / (counts.length);
        int xLen = (int) Math.round(len / step) + 1;
        DefectResults cop = new DefectResults(xLen * step, ySize, xLen,
                counts[0].length, counts[0][0].length, counts[0][0][0].length);
        for (int i = 0; i < xLen; i++) {
            for (int j = 0; j < counts[i].length; j++) {
                for (int k = 0; k < counts[i][j].length; k++) {
                    System.arraycopy(counts[i][j][k], 0, cop.counts[i][j][k], 0, counts[i][j][k].length);
                }
            }
        }
        return cop;
    }

    public short[] getGist(int iX, int iY, int chan) {
        if (chan >= getChanQt()) {
            return new short[0];
        }
        return counts[iX][iY][chan];
    }
    
    
}
