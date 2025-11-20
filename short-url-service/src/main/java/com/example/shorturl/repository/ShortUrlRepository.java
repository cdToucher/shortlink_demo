package com.example.shorturl.repository;

import com.example.shorturl.model.ShortUrl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShortUrlRepository extends JpaRepository<ShortUrl, Long> {
    
    Optional<ShortUrl> findByShortCode(String shortCode);
    
    Optional<ShortUrl> findByOriginalUrlAndBusinessId(String originalUrl, String businessId);
    
    boolean existsByShortCode(String shortCode);
    
    @Modifying
    @Query("UPDATE ShortUrl s SET s.clickCount = s.clickCount + 1 WHERE s.shortCode = :shortCode")
    void incrementClickCount(@Param("shortCode") String shortCode);
    
    @Query("SELECT s FROM ShortUrl s WHERE s.shortCode = :shortCode AND s.isActive = true")
    Optional<ShortUrl> findActiveByShortCode(@Param("shortCode") String shortCode);
    
    @Query("SELECT MAX(s.id) FROM ShortUrl s")
    Optional<Long> getMaxId();
}