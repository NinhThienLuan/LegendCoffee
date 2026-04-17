package fpt.legendcoffee.service;


import fpt.legendcoffee.dto.request.LoginRequestDTO;
import fpt.legendcoffee.dto.request.RegisterRequestDTO;
import fpt.legendcoffee.dto.response.ProfileDTO;

public interface AuthenService {

    void login(LoginRequestDTO request);

    boolean register(RegisterRequestDTO request);

    void forgotPassword(String email);

    void resetPassword(String email, String oldPassword, String newPassword);

    ProfileDTO getProfile(long id);

    void updateProfile(long id, ProfileDTO profile);
}
