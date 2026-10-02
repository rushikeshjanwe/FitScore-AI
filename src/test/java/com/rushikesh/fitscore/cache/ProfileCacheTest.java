package com.rushikesh.fitscore.cache;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class ProfileCacheTest {

    @Test
    void sameTextGivesSameKey() {
        assertEquals(ProfileCache.key("jd", "Java developer"), ProfileCache.key("jd", "Java developer"));
    }

    @Test
    void differentTextGivesDifferentKey() {
        assertNotEquals(ProfileCache.key("jd", "Java developer"), ProfileCache.key("jd", "Python developer"));
    }

    @Test
    void resumeAndJdKeysDoNotClash() {
        assertNotEquals(ProfileCache.key("resume", "same text"), ProfileCache.key("jd", "same text"));
    }
}
