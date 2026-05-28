package com.hanium.smart_drain.auth.repository;

import com.hanium.smart_drain.auth.entity.RefreshToken;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    @Modifying
    @Query("""
        update RefreshToken rt
           set rt.revoked = true,
               rt.revokedAt = :revokedAt
         where rt.userId = :userId
           and rt.revoked = false
    """)
    int revokeAllActiveTokensByUserId(@Param("userId") Long userId, @Param("revokedAt") LocalDateTime revokedAt);
}
