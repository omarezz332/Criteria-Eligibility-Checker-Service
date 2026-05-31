package com.eligibility.application.service;

import com.eligibility.infrastructure.persistence.entity.RefreshTokenEntity;
import com.eligibility.infrastructure.persistence.entity.UserEntity;
import com.eligibility.infrastructure.persistence.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional
    public RefreshTokenEntity create(UserEntity user) {
        RefreshTokenEntity token = new RefreshTokenEntity(
                UUID.randomUUID(),
                UUID.randomUUID().toString(),
                user,
                LocalDateTime.now().plusSeconds(refreshExpiration / 1000)
        );
        return refreshTokenRepository.save(token);
    }

    @Transactional
    public RefreshTokenEntity validate(String token) {
        RefreshTokenEntity refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        if (refreshToken.isRevoked()) {
            throw new IllegalArgumentException("Refresh token has been revoked");
        }
        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new IllegalArgumentException("Refresh token has expired. Please log in again.");
        }
        return refreshToken;
    }

    @Transactional
    public RefreshTokenEntity rotate(RefreshTokenEntity old) {
        old.revoke();
        refreshTokenRepository.save(old);
        return create(old.getUser());
    }

    @Transactional
    public void revokeAllForUser(UUID userId) {
        refreshTokenRepository.deleteAllByUserId(userId);
    }
}