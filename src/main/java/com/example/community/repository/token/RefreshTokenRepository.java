package com.example.community.repository.token;

import com.example.community.domain.RefreshToken;
import com.example.community.domain.User;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByUser(User user);
}
