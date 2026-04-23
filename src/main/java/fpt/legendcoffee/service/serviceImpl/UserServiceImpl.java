package fpt.legendcoffee.service.serviceImpl;

import fpt.legendcoffee.entity.User;
import fpt.legendcoffee.repository.UserRepository;
import fpt.legendcoffee.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public Optional<User> findByEmail(String name) {
        return userRepository.findByEmail(name);
    }
}
