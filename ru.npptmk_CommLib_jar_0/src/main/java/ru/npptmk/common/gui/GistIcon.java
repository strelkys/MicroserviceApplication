/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.npptmk.common.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import javax.swing.Icon;

/**
 * Иконка со столбчатой гистограммой.<br>
 * Гистограмма представлена в виде массива значений. Каждое значение
 * отображается в виде вертикального столбца с высотой, пропорциональной
 * значению. Максимальное значение для столбцов задается при передаче
 * гистограммы.<br>
 * Сделано для визуализации результатов дефектоскопии.
 *
 * @author MalginAS
 */
public class GistIcon implements Icon {

    private Component c;
    private short maxVal;
    private short[] vals;
    private Color[] colors;

    public GistIcon() {
        c = null;
    }

    public GistIcon(Component c) {
        this.c = c;
    }

    public void setVals(short maxVal, short[] vals, Color[] colors) {
        this.maxVal = maxVal;
        this.vals = vals;
        this.colors = colors;
        if (c != null) {
            c.repaint();
        }
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2d = (Graphics2D) g;
        if (this.c == null) {
            this.c = c;
        }
        if (this.c == null) {
            return;
        }
        if (vals == null || maxVal == 0) {
            return;
        }
        if (vals.length == 0) {
            return;
        }
        Rectangle2D rect;
        rect = new Rectangle2D.Double(0.0, 0.0, (double) c.getWidth(),(double)c.getHeight());
        g2d.setColor(c.getBackground());
        g2d.fill(rect);
        double width = ((double) c.getWidth()-4) / vals.length;  //уточнить формат переменной
        double height = ((double) c.getHeight()-20) / maxVal;
        for (int i = 0; i < vals.length; i++) {
            double xc = i * width + 2.0;
            double h = vals[i] * height + 1.0;
            double yc = ((double) c.getHeight()) - h - 4.0;
            rect = new Rectangle2D.Double(xc, yc, width, h);
            g2d.setColor(colors[i]);
            g2d.fill(rect);
        }
    }

    @Override
    public int getIconWidth() {
        return 0;
    }

    @Override
    public int getIconHeight() {
        return 0;
    }

}
