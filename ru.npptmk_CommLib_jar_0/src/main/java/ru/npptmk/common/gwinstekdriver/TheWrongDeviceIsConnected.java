/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ru.npptmk.common.gwinstekdriver;

import java.net.ConnectException;

/**
 * 
 * @author StreltsovAE
 */
public class TheWrongDeviceIsConnected extends ConnectException {

   public TheWrongDeviceIsConnected() {
        super("Подключенное устройство отлично от ожидаемого");
    }

    public TheWrongDeviceIsConnected(String expectedDeviceName) {
        super("Подключенное устройство  отлично от " + expectedDeviceName);
    }
    public TheWrongDeviceIsConnected(String expectedDeviceName, String factDeviceName) {
        super(String.format("Подключенное устройство %s отлично от %s " , expectedDeviceName , factDeviceName));
    }
}
