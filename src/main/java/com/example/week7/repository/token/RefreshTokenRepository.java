package com.example.week7.repository.token;

import com.example.week7.domain.RefreshToken;
import com.example.week7.domain.User;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByUser(User user);
}
