package org.example.food_a.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.food_a.common.Result;
import org.example.food_a.dto.request.MedicineRequest;
import org.example.food_a.service.MedicineRecordService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("api/medicine")
@RequiredArgsConstructor
public class MedicineRecordController {
    private final MedicineRecordService medicineRecordService;

    @PostMapping("/send")
    public Result creatMedicineRecord(@RequestBody MedicineRequest  medicineRequest){
        System.out.println(medicineRequest.getMedicineName());
        Long singleDosage = medicineRequest.getSingleDosage();
        Long userId = medicineRequest.getUserId();
        Integer takeTimes = medicineRequest.getTakeTimes();
        LocalDate stopTime = medicineRequest.getStopTime();
        String medicineName = medicineRequest.getMedicineName();

        medicineRecordService.recordMedicine(userId,medicineName,takeTimes,singleDosage,stopTime);
        return Result.success();
    }
}
