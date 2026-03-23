package org.example.food_a.common;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 通用API响应类
 * 用于统一所有接口的返回格式
 */
@Data
public class Result<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 响应状态码
     * 200: 成功
     * 400: 参数错误
     * 401: 未授权
     * 403: 无权限
     * 404: 资源不存在
     * 500: 服务器内部错误
     */
    private int code;

    /**
     * 响应信息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;


    /**
     * 私有构造函数，防止外部直接实例化
     */
    private Result() {
    }

    /**
     * 构建成功响应
     *
     * @param data 响应数据
     * @param <T>  数据类型
     * @return Result<T>
     */
    public static <T> Result<T> success(T data) {
        Result<T> response = new Result<>();
        response.setCode(200);
        response.setMessage("success");
        response.setData(data);
        return response;
    }

    /**
     * 构建成功响应（无数据）
     *
     * @param <T> 数据类型
     * @return Result<T>
     */
    public static <T> Result<T> success() {
        Result<T> response = new Result<>();
        response.setCode(200);
        return response;
    }

    /**
     * 构建成功响应（自定义消息）
     *
     * @param message 自定义消息
     * @param data    响应数据
     * @param <T>     数据类型
     * @return Result<T>
     */
    public static <T> Result<T> success(String message, T data) {
        Result<T> response = new Result<>();
        response.setCode(200);
        response.setMessage(message);
        response.setData(data);
        return response;
    }
    /**
     * 构建失败响应
     *
     * @param code    错误码
     * @param message 错误信息
     * @param <T>     数据类型
     * @return Result<T>
     */
    public static <T> Result<T> error(int code, String message) {
        Result<T> response = new Result<>();
        response.setCode(code);
        response.setMessage(message);
        response.setData(null);
        return response;
    }

    /**
     * 构建失败响应（使用默认错误码400）
     *
     * @param message 错误信息
     * @param <T>     数据类型
     * @return Result<T>
     */
    public static <T> Result<T> error(String message) {
        return error(400, message);
    }
}