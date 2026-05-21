/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ru.streltsov.microserviceapplication.dataservice.model;

/**
 *
 * @author Александр
 */
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.persistence.*;
import lombok.*;

import java.util.Arrays;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

@Entity
@Table(name = "pipes")
@Data
//@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DrillingPipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pipe_type", nullable = false)
    private String type; 
    
    @Column(name = "defect_data", columnDefinition = "JSON")
    @JdbcTypeCode(SqlTypes.JSON)
    @JsonDeserialize(using = DoubleArray2DDeserializer.class)
    @JsonSerialize(using = DoubleArray2DSerializer.class)
    private double[][] defectData;
    

   
    public DrillingPipe(String type, int sensorCount, int dataPerSensor) {
        this.type = type;
        this.defectData = new double[sensorCount][dataPerSensor];
        // Заполняем случайными значениями для теста
        for (int i = 0; i < sensorCount; i++) {
            for (int j = 0; j < dataPerSensor; j++) {
                this.defectData[i][j] = Math.random() * 100; 
            }
        }
    }
    
    public DrillingPipe() {
    }

    // Метод для получения количества датчиков
    public int getSensorCount() {
        return defectData != null ? defectData.length : 0;
    }

    // Метод для получения количества данных на один датчик
    public int getDataPerSensor() {
        return defectData != null && defectData.length > 0 ? defectData[0].length : 0;
    }

    @Override
    public String toString() {
        return "Pipe{"
                + "id=" + id
                + ", type='" + type + '\''
                + ", sensorCount=" + getSensorCount()
                + ", dataPerSensor=" + getDataPerSensor()
                + '}';
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double[][] getDefectData() {
        return defectData;
    }

    public void setDefectData(double[][] defectData) {
        this.defectData = defectData;
    }

    public Long getId() {
        return id;
    }
    
    
}
