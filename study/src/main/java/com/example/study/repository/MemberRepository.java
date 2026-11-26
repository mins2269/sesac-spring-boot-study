package com.example.study.repository;

import java.util.Optional;

import com.example.study.domain.Member;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

    boolean existsByEmail(String email);

    @EntityGraph(attributePaths = "profile")
    Optional<Member> findByEmail(String email);

    /** 프로필이 필요한 조회는 한 번의 join으로 가져온다. */
    @EntityGraph(attributePaths = "profile")
    Optional<Member> findWithProfileById(Long id);
}
