package bg.softuni.mobilelele.service;

import bg.softuni.mobilelele.model.dto.UserLoginDto;
import bg.softuni.mobilelele.model.dto.UserRegisterDto;
import bg.softuni.mobilelele.model.entity.UserEntity;
import bg.softuni.mobilelele.repository.UserRepository;
import bg.softuni.mobilelele.user.CurrentUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    private UserRepository userRepository;
    private CurrentUser currentUser;
    private PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, CurrentUser currentUser, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.currentUser = currentUser;
        this.passwordEncoder = passwordEncoder;
    }

    public void registerAndLogin(UserRegisterDto userRegisterDto) {

        UserEntity newUser =
                new UserEntity()
                        .setActive(true)
                        .setEmail(userRegisterDto.getEmail())
                        .setFirstName(userRegisterDto.getFirstName())
                        .setLastName(userRegisterDto.getLastName())
                        .setPassword(passwordEncoder.encode(userRegisterDto.getPassword()));

        newUser = userRepository.save(newUser);

        login(newUser);
    }

    public boolean login(UserLoginDto loginDto) {
        Optional<UserEntity> userOpt = userRepository
                .findByEmail(loginDto.getUsername());

        if (userOpt.isEmpty()) {
            LOGGER.info("User with [{}] not found !", loginDto.getUsername());
            return false;
        }

//        boolean success = userOpt.get().getPassword().equals(loginDto.getPassword());

        String rowPassword = loginDto.getPassword();
        String hashedPassword = userOpt.get().getPassword();

        boolean success = passwordEncoder
                .matches(rowPassword, hashedPassword);

        if (success) {
            login(userOpt.get());
        } else {
            logout();
        }

        return success;
    }

    private void login(UserEntity userEntity) {
        currentUser
                .setLoggedIn(true)
                .setName(userEntity.getFirstName() + " " + userEntity.getLastName());
    }

    public void logout() {
        currentUser.clear();
    }
}
