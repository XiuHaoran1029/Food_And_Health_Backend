package org.example.food_a.service;

import org.example.food_a.common.ImageSaver;
import org.example.food_a.entity.DietRestriction;
import org.example.food_a.entity.Disease;
import org.example.food_a.entity.User;
import org.example.food_a.repository.DietRestrictionRepository;
import org.example.food_a.repository.DiseaseRepository;
import org.example.food_a.repository.UserRepository;
import org.springframework.stereotype.Service;
import static org.example.food_a.common.StringSplitter.splitString;
import static org.example.food_a.common.ImageSaver.saveBase64AsJpg;
import static org.example.food_a.common.ImageSaver.removeBase64Header;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class UserService {
    UserRepository userRepository;
    DietRestrictionRepository dietRestrictionRepository;
    DiseaseRepository diseaseRepository;


    public UserService(UserRepository userRepository, DietRestrictionRepository dietRestrictionRepository,DiseaseRepository diseaseRepository) {
        this.userRepository = userRepository;
        this.dietRestrictionRepository = dietRestrictionRepository;
        this.diseaseRepository=diseaseRepository;
    }


    public User login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户名或密码错误"));

        // 验证密码
        if (!Objects.equals(password, user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        return user;
    }

    public User register(String username, String password, String email) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);

        // 验证邮箱是否已注册
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("邮箱已存在");
        }

        // 设置创建时间
        user.setCreateTime(LocalDateTime.now());
        User savedUser = userRepository.save(user);
        return user;
    }

    public Map<String, Object>  setting(String username,Long userid,String sick,String taboo,String img) {

        Map<String, Object> map = new HashMap<>();
        User settingUser = userRepository.findById(userid)
                .orElseThrow(() -> new RuntimeException("未找到用户"));

        settingUser.setUsername(username);


        List<DietRestriction> newRestrictions = new ArrayList<>();

        if (taboo != null && !taboo.trim().isEmpty()) {
            String[] taboo_m = splitString(taboo);

            for (String name : taboo_m) {
                if (name == null || name.trim().isEmpty()) continue;

                String cleanName = name.trim();

                // 查找是否存在
                Optional<DietRestriction> existingOpt = dietRestrictionRepository.findByName(cleanName);

                DietRestriction restriction;
                if (existingOpt.isPresent()) {
                    restriction = existingOpt.get();
                } else {
                    // 不存在则创建
                    restriction = new DietRestriction();
                    restriction.setName(cleanName);
                    dietRestrictionRepository.save(restriction);
                }
                newRestrictions.add(restriction);
            }
        }

        // 3. 核心步骤：更新关联关系
        settingUser.getDietRestrictions().clear();
        settingUser.getDietRestrictions().addAll(newRestrictions);

        List<Disease> newDisease = new ArrayList<>();

        if (sick != null && !sick.trim().isEmpty()) {
            String[] sick_m = splitString(sick);

            for (String name : sick_m) {
                if (name == null || name.trim().isEmpty()) continue;

                String cleanName = name.trim();

                // 查找是否存在
                Optional<Disease> existingOpt = diseaseRepository.findByName(cleanName);

                Disease disease;
                if (existingOpt.isPresent()) {
                    disease = existingOpt.get();
                } else {
                    // 不存在则创建
                    disease = new Disease();
                    disease.setName(cleanName);
                    diseaseRepository.save(disease);
                }
                newDisease.add(disease);
            }
        }

        // 3. 核心步骤：更新关联关系
        settingUser.getDiseases().clear();
        settingUser.getDiseases().addAll(newDisease);

        String avatar;
        try {
            avatar = ImageSaver.saveBase64AsJpg(removeBase64Header(img), userid, "src/main/resources/avatar");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        settingUser.setAvatarUrl(avatar);
        userRepository.save(settingUser);

        map.put("username", username);
        map.put("img", img);
        map.put("sick",sick);
        map.put("taboo",taboo);


        return map;
        
    }
    
    public boolean getUsersetting(Long userId,String oldPassword, String newPassword) {
        User user=userRepository.findById(userId)
                .orElseThrow(()->new RuntimeException("未发现用户"));
        if(oldPassword.equals(newPassword)) return false;
        return oldPassword.equals(user.getPassword());
    }

}
