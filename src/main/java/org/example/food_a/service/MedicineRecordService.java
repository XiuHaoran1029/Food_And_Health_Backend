package org.example.food_a.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.food_a.entity.User;
import org.example.food_a.entity.UserMedicine;
import org.example.food_a.repository.UserMedicineRepository;
import org.example.food_a.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class MedicineRecordService {
    private final UserRepository userRepository;
    private final UserMedicineRepository userMedicineRepository;

    public void recordMedicine(Long userId, String medicineName, Integer takeTimes, Long singleDosage, LocalDate stopTime){

        log.info("正在创建用药提醒，药品名为：{}",medicineName);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        try{
            UserMedicine userMedicine = new UserMedicine();
            userMedicine.setUser(user);
            userMedicine.setMedicineName(medicineName);
            userMedicine.setTakeTimes(takeTimes);
            userMedicine.setSingleDosage(singleDosage);
            userMedicine.setStopTime(stopTime.atStartOfDay());
            userMedicine.setCreateTime(LocalDateTime.now());

            userMedicineRepository.save(userMedicine);
        }catch (Exception e){
            log.info("创建用药提醒失败：{}",e.getMessage());
            throw  new RuntimeException("创建用药提醒失败");
        }

    }

}
