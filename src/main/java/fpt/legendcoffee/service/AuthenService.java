package fpt.legendcoffee.service;


import fpt.legendcoffee.dto.LoginRequestDTO;
import fpt.legendcoffee.dto.RegisterRequestDTO;

public interface AuthenService {

    void login(LoginRequestDTO request);

    boolean register(RegisterRequestDTO request);

    void forgotPassword(String email);

    void resetPassword(String email, String oldPassword, String newPassword);

}
