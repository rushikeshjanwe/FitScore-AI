package com.rushikesh.fitscore.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rushikesh.fitscore.model.ExtractedProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Optional;

/**
 * Caches the LLM extraction result (ExtractedProfile) in Redis.
 *
 * Key   = "fitscore:profile:<kind>:<sha256 of the input text>"
 * Value = ExtractedProfile as JSON, expires after a TTL
 *
 * Same resume or same job description text -> same key -> no second LLM call.
 * If Redis is down, the cache is skipped and the app still works (fail open).
 */
@Component
public class ProfileCache {

    private static final Logger log = LoggerFactory.getLogger(ProfileCache.class);

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;
    private final Duration ttl;

    public ProfileCache(StringRedisTemplate redis,
                        ObjectMapper objectMapper,
                        @Value("${fitscore.cache.ttl-hours:24}") long ttlHours) {
        this.redis = redis;
        this.objectMapper = objectMapper;
        this.ttl = Duration.ofHours(ttlHours);
    }

    public Optional<ExtractedProfile> get(String kind, String text) {
        try {
            String json = redis.opsForValue().get(key(kind, text));
            if (json == null) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(json, ExtractedProfile.class));
        } catch (Exception e) {
            log.warn("Cache read failed, continuing without cache: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public void put(String kind, String text, ExtractedProfile profile) {
        try {
            redis.opsForValue().set(key(kind, text), objectMapper.writeValueAsString(profile), ttl);
        } catch (Exception e) {
            log.warn("Cache write failed, continuing without cache: {}", e.getMessage());
        }
    }

    static String key(String kind, String text) {
        return "fitscore:profile:" + kind + ":" + sha256(text);
    }

    private static String sha256(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(text.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
