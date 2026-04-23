package fpt.legendcoffee.service;

import fpt.legendcoffee.entity.User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public interface UserService {

    Optional<User> findByEmail(String name);
}
