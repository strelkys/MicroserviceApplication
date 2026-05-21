/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.npptmk.common.gui;

import java.awt.Color;

/**
 * Цветовая шкала.<br>
 * Предназначен для формирования цветя в зависимости от значения велисины.
 * Цветовая шкала задается в виде пар цвет-значение. Для определения цвета для
 * заданной величины выбирается диапазон ближайших хначений из шкалы, между
 * которыми находится заданная величина. Цвет для величины формируется путем
 * сложения RGB компонент цветов по краям диапазона с весовыми коэффициентами
 * обратно пропорцональными разнице между величиной и значением шкалы.
 *
 * @author MalginAS
 */
public class ColorScale {

    private double[] scale;
    private Color[] colors;

    public ColorScale() {
    }

    public ColorScale(double[] scale, Color[] colors) {
        setScale(scale, colors);
    }

    /**
     * Задает цветовую шкалу.
     * @param scale Числовые значения шкалы в порядке возрастания.
     * @param colors Цвета, соответствующие значениям шкалы.
     */
    public final void setScale(double[] scale, Color[] colors) {
        this.scale = scale;
        this.colors = colors;
    }

    public Color getColor(double val) {
        if (val <= scale[0]) {
            return colors[0];
        }
        for (int i = 1; i < scale.length; i++) {
            if (val <= scale[i]) {
                double kl = 1 - (val - scale[i - 1]) / (scale[i] - scale[i - 1]);
                double k2 = 1 - (scale[i] - val) / (scale[i] - scale[i - 1]);
                return new Color(
                        (int)(colors[i-1].getRed()*kl) + (int)(colors[i].getRed()*k2),
                        (int)(colors[i-1].getGreen()*kl) + (int)(colors[i].getGreen()*k2),
                        (int)(colors[i-1].getBlue()*kl) + (int)(colors[i].getBlue()*k2));
            }
        }
        return colors[colors.length-1];
    }
}
