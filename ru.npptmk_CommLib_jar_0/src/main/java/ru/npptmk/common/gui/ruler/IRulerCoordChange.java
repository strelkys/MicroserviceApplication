/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package ru.npptmk.common.gui.ruler;

/**
 *
 * @author KandybaOA
 * Метод newValues данного интерфейса вызывается объектом Ruler, при изменении
 * положения ползунков слайдера, а также при инициализации объекта Ruler.
 * В переменные Val1 и Val2 передаются минимальное и максимальное значение
 * диапазона cоответственно
 */
public interface IRulerCoordChange {
    public void newValues(double Val1, double Val2);
}
