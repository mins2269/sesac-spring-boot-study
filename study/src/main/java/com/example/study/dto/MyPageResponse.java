package com.example.study.dto;

import java.util.List;

public record MyPageResponse(
        Long id,
        String email,
        String name,
        String introduction,
        List<StudySummaryResponse> ownedStudies) {

    public static MyPageResponse of(MemberResponse member, MyStudiesResponse studies) {
        return new MyPageResponse(member.id(), member.email(), member.name(), member.introduction(),
                studies.ownedStudies());
    }
}
