package com.example.study.repository;

import com.example.study.domain.StudyHistory;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyHistoryRepository extends JpaRepository<StudyHistory, Long> {
}
