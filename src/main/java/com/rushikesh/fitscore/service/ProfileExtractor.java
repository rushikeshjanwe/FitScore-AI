package com.rushikesh.fitscore.service;

import com.rushikesh.fitscore.cache.ProfileCache;
import com.rushikesh.fitscore.exception.ProfileExtractionException;
import com.rushikesh.fitscore.model.ExtractedProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * The only place where the LLM is used.
 * It turns messy text into a clean ExtractedProfile object.
 * The LLM never decides the score.
 *
 * Results are cached in Redis, so the same resume or job description
 * is only sent to the LLM once.
 */
@Service
public class ProfileExtractor {

    private static final Logger log = LoggerFactory.getLogger(ProfileExtractor.class);
    private static final int MAX_CHARS = 8000;
    private static final int MAX_ATTEMPTS = 2;

    private static final String RESUME_PROMPT = """
            You extract structured data from a resume.
            Return:
            - skills: technical skills, languages, frameworks, tools and technologies explicitly mentioned.
              Use short canonical names (e.g. "Spring Boot", "PostgreSQL", "Kafka").
            - yearsOfExperience: total years of professional experience as a whole number.
              If it is not stated, estimate from the work history dates. Use 0 if unknown.
            Do not invent skills that are not in the text.

            RESUME:
            %s
            """;

    private static final String JD_PROMPT = """
            You extract structured data from a job description.
            Return:
            - skills: technical skills, languages, frameworks, tools and technologies the role requires or prefers.
              Use short canonical names (e.g. "Spring Boot", "PostgreSQL", "Kafka").
            - yearsOfExperience: the minimum years of experience required as a whole number.
              If a range is given, use the lower bound. Use 0 if not stated.
            Do not invent skills that are not in the text.

            JOB DESCRIPTION:
            %s
            """;

    private final ChatClient chatClient;
    private final ProfileCache cache;

    public ProfileExtractor(ChatClient.Builder chatClientBuilder, ProfileCache cache) {
        this.chatClient = chatClientBuilder.build();
        this.cache = cache;
    }

    public ExtractedProfile fromResume(String resumeText) {
        return extract("resume", RESUME_PROMPT, resumeText);
    }

    public ExtractedProfile fromJobDescription(String jdText) {
        return extract("jd", JD_PROMPT, jdText);
    }

    private ExtractedProfile extract(String kind, String promptTemplate, String text) {
        Optional<ExtractedProfile> cached = cache.get(kind, text);
        if (cached.isPresent()) {
            log.info("Cache hit for {} - skipping LLM call", kind);
            return cached.get();
        }

        ExtractedProfile profile = callLlm(promptTemplate, text);
        cache.put(kind, text, profile);
        return profile;
    }

    private ExtractedProfile callLlm(String promptTemplate, String text) {
        String prompt = promptTemplate.formatted(truncate(text));
        RuntimeException last = null;

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                ExtractedProfile profile = chatClient.prompt()
                        .user(prompt)
                        .call()
                        .entity(ExtractedProfile.class);

                if (profile == null || profile.skills() == null) {
                    throw new IllegalStateException("LLM returned an empty profile");
                }
                return profile;
            } catch (RuntimeException e) {
                last = e;
                log.warn("Profile extraction attempt {}/{} failed: {}", attempt, MAX_ATTEMPTS, e.getMessage());
            }
        }
        throw new ProfileExtractionException("Could not extract a profile from the LLM response", last);
    }

    private String truncate(String text) {
        return text.length() <= MAX_CHARS ? text : text.substring(0, MAX_CHARS);
    }
}
