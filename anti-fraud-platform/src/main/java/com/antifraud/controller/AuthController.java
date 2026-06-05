package com.antifraud.controller;

import com.antifraud.common.Result;
import com.antifraud.dto.LoginDTO;
import com.antifraud.dto.RegisterDTO;
import com.antifraud.dto.UserDTO;
import com.antifraud.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

@Api(tags = "认证管理")
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @ApiOperation("用户登录")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody @Valid LoginDTO loginDTO) {
        Map<String, Object> data = userService.login(loginDTO.getUsername(), loginDTO.getPassword());
        return Result.success("登录成功", data);
    }

    @ApiOperation("用户注册")
    @PostMapping("/register")
    public Result<Void> register(@RequestBody @Valid RegisterDTO registerDTO) {
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername(registerDTO.getUsername());
        userDTO.setPassword(registerDTO.getPassword());
        userDTO.setRealName(registerDTO.getRealName());
        userDTO.setPhone(registerDTO.getPhone());
        userDTO.setEmail(registerDTO.getEmail());
        userDTO.setRoleIds(null);
        userService.register(userDTO);
        return Result.success("注册成功");
    }
}
