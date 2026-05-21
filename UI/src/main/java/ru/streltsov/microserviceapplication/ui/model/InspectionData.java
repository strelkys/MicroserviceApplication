/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ru.streltsov.microserviceapplication.ui.model;

/**
 *
 * @author Александр
 */
public class InspectionData {
    
    private Long pipeId;
    private String imageUrl; // URL изображения (например, http://localhost:8000/images/scan_206.png)
    private String status; // "APPROVED" или "REJECTED"

    // Геттеры и сеттеры
    public Long getPipeId() { return pipeId; }
    public void setPipeId(Long pipeId) { this.pipeId = pipeId; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
