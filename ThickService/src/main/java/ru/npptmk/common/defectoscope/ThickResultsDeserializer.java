/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ru.npptmk.common.defectoscope;

/**
 *
 * @author Александр
 */
import ru.npptmk.common.defectoscope.ThickResults;

import java.io.*;

public class ThickResultsDeserializer {

    public static void main(String[] args) {
        String filePath = "defectOnePipe.txt";
        ThickResults results = deserializeThickResults(filePath);

        if (results != null) {
            System.out.println("Десериализация успешна!");
            System.out.println("Размер области по X: " + results.getxSize());
            System.out.println("Размер области по Y: " + results.getySize());
            System.out.println("Количество ячеек по X: " + results.getXQt());
            System.out.println("Количество ячеек по Y: " + results.getYQt());
            System.out.println("Количество каналов: " + results.getChanQt());
            System.out.println("Мин. граница толщины: " + results.getMin());
            System.out.println("Макс. граница толщины: " + results.getMax());
            
            // Пример получения среднего значения для первой ячейки
            if (results.getXQt() > 0 && results.getYQt() > 0) {
                double avgVal = results.getAvgVal(0, 0);
                System.out.println("Среднее значение [0,0]: " + avgVal);
            }
        }
    }

    /**
     * Десериализует объект ThickResults из указанного файла
     * @param filePath Путь к файлу с сериализованным объектом
     * @return Объект ThickResults или null в случае ошибки
     */
    public static ThickResults deserializeThickResults(String filePath) {
        ThickResults results = null;
        try (FileInputStream fis = new FileInputStream(filePath);
             ObjectInputStream ois = new ObjectInputStream(fis)) {

            Object obj = ois.readObject();
            if (obj instanceof ThickResults) {
                results = (ThickResults) obj;
            } else if (obj instanceof DefectResults) {
                System.err.println("Ошибка: объект в файле является DefectResults, а не ThickResults");
            } else {
                System.err.println("Ошибка: объект в файле не является экземпляром ThickResults");
            }

        } catch (IOException e) {
            System.err.println("Ошибка ввода/вывода при десериализации: " + e.getMessage());
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.err.println("Класс ThickResults не найден: " + e.getMessage());
            e.printStackTrace();
        } catch (ClassCastException e) {
            System.err.println("Ошибка приведения типа: " + e.getMessage());
            e.printStackTrace();
        }

        return results;
    }
}