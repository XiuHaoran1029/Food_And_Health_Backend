package org.example.food_a.service;


import org.example.food_a.common.ImageSaver;
import org.example.food_a.entity.DietRestriction;
import org.example.food_a.entity.Disease;
import org.example.food_a.entity.User;
import org.example.food_a.repository.DietRestrictionRepository;
import org.example.food_a.repository.DiseaseRepository;
import org.example.food_a.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.example.food_a.common.ImageConfig;
import static org.example.food_a.common.StringSplitter.splitString;
import static org.example.food_a.common.ImageSaver.saveAvatar;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {
    UserRepository userRepository;
    DietRestrictionRepository dietRestrictionRepository;
    DiseaseRepository diseaseRepository;
    ImageConfig imageConfig;

    @Value("${image.path1}")
    private String avatarPath;
    @Value("${test.path}")
    private String webPath;



    public UserService(UserRepository userRepository, DietRestrictionRepository dietRestrictionRepository,ImageConfig imageConfig,DiseaseRepository diseaseRepository) {
        this.userRepository = userRepository;
        this.imageConfig = imageConfig;
        this.dietRestrictionRepository = dietRestrictionRepository;
        this.diseaseRepository=diseaseRepository;
    }


    public User login(String email, String password) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("邮箱或密码错误"));

        // 验证密码
        if (!Objects.equals(password, user.getPassword())) {
            throw new RuntimeException("邮箱或密码错误");
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

    public Map<String, Object> setting(String username, Long userid, List<String> sick, List<String> taboo, String img) {
        Map<String, Object> map = new HashMap<>();
        User settingUser = userRepository.findById(userid)
                .orElseThrow(() -> new RuntimeException("未找到用户"));

        settingUser.setUsername(username);

        // ===================== 处理饮食禁忌 taboo（已改为 List）=====================
        List<DietRestriction> newRestrictions = new ArrayList<>();
        if (taboo != null && !taboo.isEmpty()) {
            for (String name : taboo) {
                if (name == null || name.trim().isEmpty()) continue;
                String cleanName = name.trim();

                Optional<DietRestriction> existingOpt = dietRestrictionRepository.findByName(cleanName);
                DietRestriction restriction;
                if (existingOpt.isPresent()) {
                    restriction = existingOpt.get();
                } else {
                    restriction = new DietRestriction();
                    restriction.setName(cleanName);
                    dietRestrictionRepository.save(restriction);
                }
                newRestrictions.add(restriction);
            }
        }

        // 更新用户关联
        settingUser.getDietRestrictions().clear();
        settingUser.getDietRestrictions().addAll(newRestrictions);

        // ===================== 处理疾病 sick（已改为 List）=====================
        List<Disease> newDisease = new ArrayList<>();
        if (sick != null && !sick.isEmpty()) {
            for (String name : sick) {
                if (name == null || name.trim().isEmpty()) continue;
                String cleanName = name.trim();

                Optional<Disease> existingOpt = diseaseRepository.findByName(cleanName);
                Disease disease;
                if (existingOpt.isPresent()) {
                    disease = existingOpt.get();
                } else {
                    disease = new Disease();
                    disease.setName(cleanName);
                    diseaseRepository.save(disease);
                }
                newDisease.add(disease);
            }
        }

        // 更新用户关联
        settingUser.getDiseases().clear();
        settingUser.getDiseases().addAll(newDisease);

        // ===================== 头像处理 =====================
        String avatar;
        if (img == null || img.isEmpty()) {
            avatar = settingUser.getAvatarUrl();
        } else {
            try {
                avatar = saveAvatar(img, userid, avatarPath);
            } catch (IOException e) {
                throw new RuntimeException("头像保存失败", e);
            }
            settingUser.setAvatarUrl(avatar);
            userRepository.save(settingUser);
        }


        // 返回结果
        map.put("username", username);
        map.put("img",webPath+"/avatar/"+avatar);  // 修复：返回真实保存的头像路径
        map.put("sick", sick);
        map.put("taboo", taboo);
        System.out.println(map);
        return map;
    }

    public Map<String, Object>  getInfo(Long userId,String userName) {
        Map<String, Object> map = new HashMap<>();
        User user=userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("未找到用户"));
        // 提取疾病名称
        List<String> diseaseNames = user.getDiseases().stream()
                .map(Disease::getName)
                .collect(Collectors.toList());

        // 提取忌口名称
        List<String> restrictionNames = user.getDietRestrictions().stream()
                .map(DietRestriction::getName)
                .collect(Collectors.toList());

        map.put("sick", diseaseNames);
        map.put("taboo", restrictionNames);
        String avatar=user.getAvatarUrl();
        if(avatar==null) avatar = "empty.jpg";
        String avatarUrl = webPath+"/avatar/"+avatar;
        map.put("img", avatarUrl);
        map.put("username", userName);
        map.put("userId", userId);

        return map;

    }

    public boolean getUserSetting(Long userId,String oldPassword, String newPassword) {
        User user=userRepository.findById(userId)
                .orElseThrow(()->new RuntimeException("未发现用户"));
        if(oldPassword.equals(newPassword)) return false;
        user.setPassword(newPassword);
        return true;
    }

}
