package com.library.service;

import com.library.dto.auth.LoginResponseDto;
import com.library.dto.user.UserResponseDto;
import com.library.requestform.account.LoginRequestForm;
import com.library.requestform.account.RegisterRequestForm;

public interface AuthService {

    UserResponseDto register(RegisterRequestForm form);

    LoginResponseDto login(LoginRequestForm form);
}
