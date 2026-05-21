/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.npptmk.common.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import javax.swing.Icon;
import javax.swing.JLabel;

/**
 * Иконка с линейной шкалой.
 *
 * @author MalginAS
 */
public class ScaleIconRevX implements Icon {

    private double fullLenth;   // Полная длина линейки.
    private double scaleLength; // Длина шкалы. Конец шкалы распологаетя
                                // на левом краю иконки.
    private final double mainStep;    // Шаг основных дклкний шкалы.
    private final double smallStep;   // Шаг маленьких делений шкалы.
    private Component c;

    public ScaleIconRevX(double fullLenth, double scaleLength, double mainStep, double smallStep) {
        this.fullLenth = fullLenth;
        this.scaleLength = scaleLength;
        this.mainStep = mainStep;
        this.smallStep = smallStep;
    }

    public void setLen(double fullLenth, double scaleLength) {
        this.fullLenth = fullLenth;
        this.scaleLength = scaleLength;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        this.c = c;
        if (c == null) {
            return;
        }
        double mx =  fullLenth/((double) c.getWidth()) ;
        // 0. Закрашиваем иконку цветом фона.
        g.setColor(c.getBackground());
        g.fillRect(0, 0, c.getWidth(), c.getHeight());       
        // 1. Рисуем рамку вокруг иконки.
        g.setColor(Color.black);
        int[] xc = new int[5];
        int[] yc = new int[5];
        xc[0] = 0;
        yc[0] = 0;
        xc[1] = c.getWidth() - 1;
        yc[1] = 0;
        xc[2] = c.getWidth() - 1;
        yc[2] = c.getHeight() - 1;
        xc[3] = 0;
        yc[3] = c.getHeight() - 1;
        xc[4] = 0;
        yc[4] = 0;
        g.drawPolyline(xc, yc, 5);
        // 2. Зполняем черным промежуток от левого края до начала шкалы.
        double curX = (fullLenth);
        g.fillRect(0, 0, (int) ((fullLenth-scaleLength) / mx), c.getHeight());
        double prtCoor = 0;
        int fh = g.getFontMetrics().getAscent() + g.getFontMetrics().getLeading();
        for (; curX > 0; curX -= mainStep) {
            // 3. Цикл по рисованию основных делений и мелких делений.
            xc[0] = (int) (curX / mx);
            yc[0] = 0;
            xc[1] = (int) (curX / mx);
            yc[1] = c.getHeight() - 1;
            g.drawPolyline(xc, yc, 2);
            g.drawString(String.format("%2.0f", prtCoor ), (int) (curX / mx)+2, fh );
            prtCoor += mainStep;
        }
        for (curX = (fullLenth); curX > 0; curX -= smallStep) {
            // 3. Цикл по рисованию мелких делений.
            xc[0] = (int) (curX / mx);
            yc[0] = 0;
            xc[1] = (int) (curX / mx);
            yc[1] = c.getHeight()/3;
            g.drawPolyline(xc, yc, 2);
            xc[0] = (int) (curX / mx);
            yc[0] = c.getHeight()-c.getHeight()/3;
            xc[1] = (int) (curX / mx);
            yc[1] = c.getHeight()-1;
            g.drawPolyline(xc, yc, 2);
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
