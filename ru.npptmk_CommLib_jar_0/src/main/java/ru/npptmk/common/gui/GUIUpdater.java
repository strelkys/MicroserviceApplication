/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.npptmk.common.gui;

/**
 * Базовый класс для построения обновителей графического интерфейса.<br>
 * Не содержит методов и данных. Используется как метка.
 * @author MalginAS
 */
public class GUIUpdater {
    private final int sourceId;    // Идентификатор источника данных

    public GUIUpdater(int sourceId) {
        this.sourceId = sourceId;
    }

    public int getSourceId() {
        return sourceId;
    }
    
    
}
