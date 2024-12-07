package com.hbbank.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hbbank.backend.domain.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    
    RefreshToken findByUser_Id(Long userId);
    
    void deleteByUser_Id(Long userId);
    
    @Modifying
    @Query("UPDATE RefreshToken r SET r.token = :#{#refreshToken.token}, r.expiryDate = :#{#refreshToken.expiryDate} WHERE r.user.id = :#{#refreshToken.user.id}")
    void updateRefreshToken(@Param("refreshToken") RefreshToken refreshToken);
}
