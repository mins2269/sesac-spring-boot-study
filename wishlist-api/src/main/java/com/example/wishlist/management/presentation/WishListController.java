package com.example.wishlist.management.presentation;

import com.example.wishlist.management.application.WishDto;
import com.example.wishlist.management.application.WishListService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class WishListController {
    private final WishListService wishListService;

    public WishListController(WishListService wishListService) {
        this.wishListService = wishListService;
    }

    // 위시리스트에 아이템 등록
    @PostMapping("/wish")
    public WishDto register(@RequestBody WishDto request) {
        return wishListService.add(request);
    }

    // 전체 목록 조회
    @GetMapping("/wish")
    public List<WishDto> getWishList() {
        return wishListService.getWishList();
    }

    // 등록한 id의 아이템 조회
    @GetMapping("/wish/{id}")
    public WishDto getWish(@PathVariable long id) {
        return wishListService.getWish(id);
    }

    // id 기반 수정
    @PutMapping("/wish/{id}")
    public WishDto updateWish(@PathVariable long id, @RequestBody WishDto request) {
        return wishListService.updateWish(id, request);
    }

    // id 기반 삭제
    @DeleteMapping("/wish/{id}")
    public void deleteWish(@PathVariable long id) {
        wishListService.deleteWish(id);
    }

    // 예산 안에서 살 수 있는 아이템을 최대 3개 추천
    @GetMapping("/wish/recommend/{budget}")
    public List<WishDto> getRecommendWishList(@PathVariable long budget) {
        return wishListService.getRecommendWishList(budget);
    }
}