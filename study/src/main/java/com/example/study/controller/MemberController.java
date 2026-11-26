package com.example.study.controller;

import com.example.study.common.auth.LoginMember;
import com.example.study.common.auth.PublicApi;
import com.example.study.dto.MemberResponse;
import com.example.study.dto.SignupRequest;
import com.example.study.dto.UpdateMemberRequest;
import com.example.study.service.MemberService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PublicApi
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MemberResponse signUp(@Valid @RequestBody SignupRequest request) {
        return memberService.signUp(request);
    }

    @GetMapping("/me")
    public MemberResponse getMe(@LoginMember Long memberId) {
        return memberService.getMember(memberId);
    }

    @PatchMapping("/me")
    public MemberResponse updateMe(@LoginMember Long memberId,
            @Valid @RequestBody UpdateMemberRequest request) {
        return memberService.updateMember(memberId, request);
    }
}
