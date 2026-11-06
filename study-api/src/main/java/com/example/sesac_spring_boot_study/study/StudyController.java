package com.example.sesac_spring_boot_study.study;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/study")
public class StudyController {
    private StudyService service;

    public StudyController(StudyService service) {
        this.service = service;
    }

    @PostMapping("/participants")
    public String register(@RequestParam(required = false) String name){
        // required = false인 이유는 name이 안 와도 에러 없이 메서드가 실행되어
        // 예외 처리를 직접 하려는 조건 때문이다
        // 여기서 name이 안 온다는 건 null값이나 name은 왔지만 값이 비어있거나
        return service.register(name);
    }

    @GetMapping("/participants")
    public String list(){
        return service.list();
    }

    @PostMapping("/cancel")
    public String cancel(@RequestParam(required = false) String memberId){
        return service.cancel(memberId);
    }

    @PostMapping("/capacity")
    public String capacity(@RequestParam(required = false) String capacity){
        return service.capacity(capacity);
    }
}