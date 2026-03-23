package org.example.food_a.dto.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class MedicineRequest {
    Long userId;
    String medicineName;
    Integer takeTimes;
    Long singleDosage;
    LocalDate stopTime;
}
