package com.antifraud.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

@Data
public class UserDTO {
    private Long id;
    
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度在3-50个字符之间")
    private String username;
    
    @Size(min = 6, max = 20, message = "密码长度在6-20个字符之间")
    private String password;
    
    private String realName;
    private String phone;
    private String email;
    private Integer status;
    
    private List<Long> roleIds;
}
