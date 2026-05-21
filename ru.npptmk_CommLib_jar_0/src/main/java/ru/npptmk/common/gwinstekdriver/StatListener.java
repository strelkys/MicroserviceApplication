/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package ru.npptmk.common.gwinstekdriver;

/**
 *
 * @author streltsovae
 */
public interface StatListener {
    /**        
     * Записывает статус подключения к блоку питания 
     * @param step    0 - не запущен
     *                  1 - подключается
     *                  2 - работает
     *                  3 - ошибка подключения
     *@param lastEx последняя ошибка возникшая в драйвере
    */
    void setGWConnectionStatus(int step, Exception lastEx);
}
