package org.example.food_a.repository;

import org.example.food_a.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // JpaRepository 已经提供了以下常用方法：
    // - save(User user): 保存或更新用户
    // - findById(Long id): 根据ID查找用户，返回Optional
    // - findAll(): 查询所有用户
    // - deleteById(Long id): 根据ID删除用户
    // - count(): 统计用户总数

    // 你可以根据业务需求，添加自定义的查询方法
    // Spring Data JPA会根据方法名自动生成SQL

    /**
     * 根据用户名查找用户
     * @param username 用户名
     * @return 一个Optional包装的User对象，如果找不到则为空
     */
    Optional<User> findByUsername(String username);


    Optional<User> findById(Long id);

    /**
     * 根据邮箱查找用户
     * @param email 邮箱地址
     * @return 一个Optional包装的User对象
     */
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
    /**
     * 检查用户名是否已存在
     * @param username 用户名
     * @return 如果存在则返回true，否则返回false
     */
    boolean existsByUsername(String username);


    Optional<Long> getIdByUsername(String username);
    Page<User> findAll(Pageable pageable);

}