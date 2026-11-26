package com.example.study.domain;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.example.study.common.exception.BusinessException;
import com.example.study.common.exception.ErrorCode;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

@Entity
public class Study {

    public static final int NAME_MAX_LENGTH = 50;
    public static final int DESCRIPTION_MAX_LENGTH = 2000;
    public static final int MIN_CAPACITY = 2;
    public static final int MAX_CAPACITY = 100;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private Member owner;

    @Column(nullable = false, length = NAME_MAX_LENGTH)
    private String name;

    @Column(nullable = false, length = DESCRIPTION_MAX_LENGTH)
    private String description;

    @Column(nullable = false)
    private int capacity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecruitStatus recruitStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DayOfWeek dayOfWeek;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    /** 스터디장을 포함한 가입 정보 */
    @OneToMany(mappedBy = "study", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StudyMember> members = new ArrayList<>();

    protected Study() {
    }

    private Study(Member owner, String name, String description, int capacity, RecruitStatus recruitStatus,
            DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
        this.owner = owner;
        this.name = name;
        this.description = description;
        this.capacity = capacity;
        this.recruitStatus = recruitStatus;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    /** 스터디를 만들고 스터디장을 첫 참여자로 등록한다. */
    public static Study open(Member owner, String name, String description, int capacity,
            RecruitStatus recruitStatus, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
        if (capacity < MIN_CAPACITY || capacity > MAX_CAPACITY) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        if (!startTime.isBefore(endTime)) {
            throw new BusinessException(ErrorCode.INVALID_STUDY_TIME);
        }
        Study study = new Study(owner, name, description, capacity, recruitStatus, dayOfWeek, startTime, endTime);
        study.addMember(owner);
        return study;
    }

    /** 같은 요일에 시간이 겹치는지 확인한다. 끝과 시작이 맞닿는 것은 겹침이 아니다. */
    public boolean overlaps(Study other) {
        return dayOfWeek == other.dayOfWeek
                && startTime.isBefore(other.endTime)
                && other.startTime.isBefore(endTime);
    }

    public void changeRecruitStatus(Long requesterId, RecruitStatus recruitStatus) {
        validateOwner(requesterId);
        this.recruitStatus = recruitStatus;
    }

    public void validateOwner(Long memberId) {
        if (!isOwner(memberId)) {
            throw new BusinessException(ErrorCode.NOT_STUDY_OWNER);
        }
    }

    public boolean isOwner(Long memberId) {
        return owner.getId().equals(memberId);
    }

    public boolean hasMember(Long memberId) {
        return members.stream().anyMatch(studyMember -> studyMember.isMember(memberId));
    }

    public StudyRole roleOf(Long memberId) {
        if (isOwner(memberId)) {
            return StudyRole.OWNER;
        }
        return hasMember(memberId) ? StudyRole.MEMBER : StudyRole.NONE;
    }

    /** 삭제 시점의 스터디장과 참여자에게 남길 이력. 이미 탈퇴한 회원은 포함되지 않는다. */
    public List<StudyHistory> deletionHistories(LocalDateTime endedAt) {
        return members.stream()
                .map(studyMember -> {
                    Member member = studyMember.getMember();
                    HistoryReason reason = isOwner(member.getId())
                            ? HistoryReason.OWNER_DELETED
                            : HistoryReason.STUDY_DELETED;
                    return new StudyHistory(member, this, reason, endedAt);
                })
                .toList();
    }

    /** 양방향 관계의 양쪽 값을 함께 맞춘다. */
    private void addMember(Member member) {
        members.add(new StudyMember(this, member));
    }

    public Long getId() {
        return id;
    }

    public Member getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getCapacity() {
        return capacity;
    }

    public RecruitStatus getRecruitStatus() {
        return recruitStatus;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public List<StudyMember> getMembers() {
        return Collections.unmodifiableList(members);
    }
}
