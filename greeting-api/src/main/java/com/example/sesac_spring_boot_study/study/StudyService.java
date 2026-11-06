package com.example.sesac_spring_boot_study.study;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class StudyService {
    private static int CAPCITY = 3;
    private List<Participant> participants = new ArrayList<>();
    private int nextid = 0;

    public synchronized String register(String name){
        // 빈값, 누락 거절
        if(name == null || name.trim().isEmpty()){
            return "이름을 입력해 주세요.";
        }

        // 공백 제거
        name = name.trim();

        // 중복 이름 제거
        for (Participant participant : participants) {
            if(participant.name.equals(name)){
                return "이미 신청하였습니다.";
            }
        }

        // 성공
        int id = nextid++;
        String status = participants.size() < CAPCITY ? "확정" : "대기";
        participants.add(new Participant(id, name, status));

        return "신청 완료 - id: " + id + ", 이름: " + name + ", 상태: " +  status;
    }
    // 단순 조회여서 문제가 되지 않지만 조회하던 중 취소나 새로운 삽입을 하면 문제가 된다.
    public String list(){
        if(participants.isEmpty()){
            return "참가자가 없습니다.";
        }
        String result = "";
        for(Participant participant : participants){
            result += participant.toString() + "\n";
        }
        return result;
    }
    public synchronized String cancel(String memberId){
        // 빈 값, 누락
        if(memberId == null || memberId.trim().isEmpty()){
            return "빈 값 또는 값이 누락 되었습니다.";
        }

        // 숫자 아님
        int integerId;
        try{
            integerId = Integer.parseInt(memberId.trim());
        } catch (NumberFormatException e){
            return "id는 숫자여야 합니다.";
        }

        // 취소 대상 찾기
        Participant target = null;
        for(Participant participant : participants){
            if(participant.id == integerId){
                target = participant;
                break;
            }
        }
        if(target == null){
            return "해당 id가 없습니다.";
        }

        // 삭제
        participants.remove(target);

        // 삭제 + 대기자
        if(target.status.equals("대기")){
            return "취소 완료, 대기자 id: " + target.id + ", 이름: " +  target.name;
        }

        // 삭제 + 확정자
        for(Participant participant : participants){
            if(participant.status.equals("대기")){
                participant.status = "확정";
                return "취소 완료, 확정자 id: " + target.id + ", 이름: " + target.name
                        + " / 승격 id: " + participant.id + ", 이름: " + participant.name;
            }
        }

        // 대기자가 없을 때
        return "취소 완료, 확장자 id: " + target.id + ", 이름: " + target.name + ", 승격할 대기자 없음";
    }
    public synchronized String capacity(String capacity){
        // 값 누락, 빈 값 검사
        if(capacity == null || capacity.trim().isEmpty()){
            return "현재 정원 수: " +  CAPCITY + ", 실패 이유: 빈 값 또는 값이 누락 되었습니다.";
        }

        // 숫자 검사
        int integercapacity;
        try{
            integercapacity = Integer.parseInt(capacity.trim());
        } catch (NumberFormatException e){
            return "현재 정원 수: " +  CAPCITY + ", 실패 이유: 숫자가 아닌게 입력되었습니다.";
        }

        //  0, 음수, 11 이상 검사
        if(integercapacity < 1 || integercapacity > 10){
            return "현재 정원 수: " +  CAPCITY + ", 실패 이유: 1~10 정수만 허용합니다.";
        }

        // 현재 확정 인원보다 작으면
        int count = 0;
        for(Participant participant : participants){
            if(participant.status.equals("확정")){
                count++;
            }
        }
        if(integercapacity < count){
            return "현재 정원 수: " +  CAPCITY + ", 실패 이유: 정원 변경을 할 수 없습니다.(현재 확정 인원보다 작다)";
        }

        // 정원 늘리기
        int promotenum = integercapacity - count;
        CAPCITY = integercapacity;
        for(Participant participant : participants){
            if(promotenum == 0){
                break;
            }
            if(participant.status.equals("대기")){
                participant.status = "확정";
                promotenum--;
            }
        }
        return "정원을 " + CAPCITY + "으로 변경하였습니다.";
    }

    static class Participant {
        int id;
        String name;
        String status;

        Participant(int id, String name, String status) {
            this.id = id;
            this.name = name;
            this.status = status;
        }
        @Override
        public String toString() {
            return "id: " + id + ", 이름: " + name + ", 상태: " + status;
        }
    }
}