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
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import javax.swing.Icon;
import javax.swing.JLabel;
import org.apache.log4j.Logger;

/**
 * Иконка, изабражающая прямоугольную матрицу из цветных прямоугольников.
 *
 * @author Belonozhkin
 */
public class MapIcon implements Icon {

    private static final Logger LOG = Logger.getLogger(MapIcon.class);

    private Color[][] values; // набор значений для визуализации
    private Component c;            // Контейнер иконки

    public MapIcon() {
        values = new Color[1][1];
        values[0][0] = Color.black;
    }

    public void setMap(Color[][] values) {
        this.values = values;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2d = (Graphics2D) g;
        this.c = c;
        if (c == null) {
            return;
        }
        double width = ((double) c.getWidth()) / values.length;  //уточнить формат переменной
        double height = ((double) c.getHeight()) / values[0].length;
        double prevX = (double) c.getWidth() - 1;
        for (int i = 0; i < values.length; i++) { //колличство столбцов 
            double xx = (double) c.getWidth() - (i + 1) * width; 
            for (int j = 0; j < values[0].length; j++) {   
                double yy = j * height;
                Rectangle2D rect = new Rectangle2D.Double(xx, yy, (prevX - xx), height);
                g2d.setColor(values[i][j]);
                g2d.fill(rect);
            }
            prevX = xx;
        }
    }

    public int getIXbyCoord(MouseEvent evt) {
        if (c == null) {
            return 0;
        }
        double x = evt.getX();
        double width = ((double) c.getWidth()) / values.length;  //уточнить формат переменной
        return values.length - (int) (x / width) - 1;
    }

    public int getIYbyCoord(MouseEvent evt) {
        if (c == null) {
            return 0;
        }
        double y = evt.getY();
        double height = ((double) c.getHeight()) / values[0].length;
        return (int) (y / height);
    }

    @Override
    public int getIconWidth() {
        return 0;
    }

    @Override
    public int getIconHeight() {
        return 0;
    }

    public Image getImage(Rectangle re) {
        BufferedImage bp = new BufferedImage(re.width, re.height, BufferedImage.TYPE_INT_RGB);
        Graphics g = bp.getGraphics();
        JLabel cc = new JLabel();
        cc.setBounds(re);
        cc.setBackground(Color.WHITE);
        cc.setForeground(Color.GRAY);
        Component oldc = c;
        g.setClip(0, 0, re.width, re.height);
        c = cc;
        paintIcon(cc, g, 0, 0);
        c = oldc;
        return bp;
    }

}
