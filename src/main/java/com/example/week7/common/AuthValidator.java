package com.example.week7.common;

import com.example.week7.common.exception.custom.BadRequestException;
import com.example.week7.common.exception.custom.DuplicatedException;
import com.example.week7.common.exception.custom.ForbiddenException;
import com.example.week7.domain.User;
import com.example.week7.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import static com.example.week7.common.exception.ErrorMessage.*;

@RequiredArgsConstructor
@Component
public class AuthValidator {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void validate(User loginUser, User owner) throws ForbiddenException {
        if (!loginUser.getId().equals(owner.getId())) {
            throw new ForbiddenException(FORBIDDEN);
        }
    }

    public void isExistEmail(String email) {
        if(userRepository.findByEmail(email).isPresent()) {
            throw new DuplicatedException(EMAIL_DUPLICATED);
        }
    }

    public void isExistNickname(String nickname) {
        if(userRepository.findByNickname(nickname).isPresent()) {
            throw new DuplicatedException(NICKNAME_DUPLICATED);
        }
    }

    public void checkPassword(String password, String passwordCheck) {
        if (!password.equals(passwordCheck)) {
            throw new BadRequestException(PASSWORD_MISMATCH);
        }
    }
}
