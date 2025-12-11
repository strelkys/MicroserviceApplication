/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ru.streltsov.microserviceapplication.dataservice.model;


import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.IOException;
import java.util.List;

/**
 *
 * @author Александр
 */
public class DoubleArray2DDeserializer extends JsonDeserializer<double[][]> {

    @Override
    public double[][] deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        ObjectMapper mapper = (ObjectMapper) p.getCodec();
        JsonNode node = mapper.readTree(p);

        if (node.isNull()) {
            return null;
        }

        if (!node.isArray()) {
            throw new IOException("Expected array for double[][]");
        }

        ArrayNode arrayNode = (ArrayNode) node;
        int rows = arrayNode.size();
        double[][] result = new double[rows][];

        for (int i = 0; i < rows; i++) {
            JsonNode rowNode = arrayNode.get(i);
            if (!rowNode.isArray()) {
                throw new IOException("Expected array for row " + i);
            }
            ArrayNode rowArray = (ArrayNode) rowNode;
            int cols = rowArray.size();
            result[i] = new double[cols];
            for (int j = 0; j < cols; j++) {
                result[i][j] = rowArray.get(j).asDouble();
            }
        }

        return result;
    }
}
