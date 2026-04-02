package org.example.food_a.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.food_a.common.Result;
import org.example.food_a.dto.request.MedicineRequest;
import org.example.food_a.service.MedicineRecordService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("api/medicine")
@RequiredArgsConstructor
public class MedicineRecordController {
    private final MedicineRecordService medicineRecordService;

    @PostMapping("/send")
    public Result<?> creatMedicineRecord(@RequestBody MedicineRequest  medicineRequest){

        Long singleDosage = medicineRequest.getSingleDosage();
        Long userId = medicineRequest.getUserId();
        Integer takeTimes = medicineRequest.getTakeTimes();
        LocalDate stopTime = medicineRequest.getStopTime();
        String medicineName = medicineRequest.getMedicineName();
        log.info("收到用户id为{}的用药提醒请求",userId);
        medicineRecordService.recordMedicine(userId,medicineName,takeTimes,singleDosage,stopTime);
        log.info("用药提醒请求已处理");
        return Result.success();
    }
}
