package com.rushikesh.fitscore.repository;

import com.rushikesh.fitscore.model.FitScoreResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FitScoreRepository extends JpaRepository<FitScoreResult, Long> {
}
