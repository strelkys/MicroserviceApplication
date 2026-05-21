/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package ru.npptmk.common.defectoscope;

//import ru.npptmk.common.defectoscope.DefectResults;

import java.io.*;
/**
 *
 * @author Александр
 */
public class DefectResultsDeserializer {

    public static void main(String[] args) {
        String filePath = "defectOnePipe.txt";
        DefectResults results = deserializeDefectResults(filePath);

        if (results != null) {
            System.out.println("Десериализация успешна!");
            System.out.println("Размер области по X: " + results.getxSize());
            System.out.println("Размер области по Y: " + results.getySize());
            System.out.println("Количество ячеек по X: " + results.getXQt());
            System.out.println("Количество ячеек по Y: " + results.getYQt());
            System.out.println("Количество каналов: " + results.getChanQt());
        }
    }
    
    public static DefectResults deserializeDefectResults(String filePath) {
        DefectResults results = null;
        try (FileInputStream fis = new FileInputStream(filePath);
             ObjectInputStream ois = new ObjectInputStream(fis)) {

            Object obj = ois.readObject();
            if (obj instanceof DefectResults) {
                results = (DefectResults) obj;
            } else {
                System.err.println("Ошибка: объект в файле не является экземпляром DefectResults");
            }

        } catch (IOException e) {
            System.err.println("Ошибка ввода/вывода при десериализации: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("Класс DefectResults не найден: " + e.getMessage());
        } catch (ClassCastException e) {
            System.err.println("Ошибка приведения типа: " + e.getMessage());
        }

        return results;
    }
}
