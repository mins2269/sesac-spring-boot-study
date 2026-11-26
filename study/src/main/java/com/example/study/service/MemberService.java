package com.example.study.service;

import com.example.study.common.exception.BusinessException;
import com.example.study.common.exception.ErrorCode;
import com.example.study.domain.Member;
import com.example.study.dto.MemberResponse;
import com.example.study.dto.SignupRequest;
import com.example.study.dto.UpdateMemberRequest;
import com.example.study.repository.MemberRepository;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public MemberService(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public MemberResponse signUp(SignupRequest request) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }
        Member member = new Member(
                request.email(),
                passwordEncoder.encode(request.password()),
                request.name(),
                request.introduction());
        try {
            // 동시에 가입하면 위 검사를 함께 통과할 수 있다. 최종 판정은 DB unique 제약이 한다.
            memberRepository.saveAndFlush(member);
        } catch (DataIntegrityViolationException e) {
            if (isDuplicateEmail(e)) {
                throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
            }
            throw e;
        }
        return MemberResponse.from(member);
    }

    public MemberResponse getMember(Long memberId) {
        return MemberResponse.from(findMember(memberId));
    }

    @Transactional
    public MemberResponse updateMember(Long memberId, UpdateMemberRequest request) {
        if (request.name() == null && request.introduction() == null) {
            throw new BusinessException(ErrorCode.NOTHING_TO_UPDATE);
        }
        Member member = findMember(memberId);
        if (request.name() != null) {
            member.changeName(request.name());
        }
        if (request.introduction() != null) {
            member.changeIntroduction(request.introduction());
        }
        // 변경 감지로 커밋할 때 반영된다.
        return MemberResponse.from(member);
    }

    private Member findMember(Long memberId) {
        // 세션의 회원이 DB에 없으면 로그인 상태로 보지 않는다.
        return memberRepository.findWithProfileById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
    }

    private boolean isDuplicateEmail(DataIntegrityViolationException e) {
        return e.getCause() instanceof ConstraintViolationException violation
                && violation.getConstraintName() != null
                && violation.getConstraintName().endsWith(Member.EMAIL_UNIQUE_CONSTRAINT);
    }
}
