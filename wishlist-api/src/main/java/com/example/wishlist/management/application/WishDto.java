package com.example.wishlist.management.application;

import com.example.wishlist.management.domain.Wish;

public record WishDto(Long id, String name, Integer price) {

    // 도메인 → DTO (응답용)
    public static WishDto from(Wish wish) {
        return new WishDto(wish.getId(), wish.getName(), wish.getPrice());
    }

    // DTO → 도메인 (요청용). 이름·가격 검증은 Wish 생성자가 수행
    public Wish toDomain() {
        return new Wish(id == null ? 0 : id, name, price);
    }
}