/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ru.streltsov.microserviceapplication.dataservice.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
/**
 *
 * @author Александр
 */
public class DoubleArray2DSerializer extends JsonSerializer<double[][]> {

    @Override
    public void serialize(double[][] value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
        } else {
            gen.writeStartArray();
            for (double[] row : value) {
                gen.writeStartArray();
                for (double element : row) {
                    gen.writeNumber(element);
                }
                gen.writeEndArray();
            }
            gen.writeEndArray();
        }
    }
}
