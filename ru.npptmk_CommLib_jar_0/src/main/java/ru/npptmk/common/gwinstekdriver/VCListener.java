/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package ru.npptmk.common.gwinstekdriver;

/**
 *
 * @author StreltsovAE
 */
public interface VCListener {
    
    /**
     * Записывает считанное значение 
     * @param v Заначение напряжения из драйвера
     * @param cur Заначение силы тока из драйвера
     * @param offOn Состоянте выхода блока питания
     */
    void setVoltageAndCurrent (double v, double cur, boolean offOn);
//    
//    /**
//     * Запуск нового объекта - драйвера для связи с усстройством
//     */
//    void startGW();
}
