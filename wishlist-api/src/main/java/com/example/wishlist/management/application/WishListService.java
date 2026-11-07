package com.example.wishlist.management.application;

import com.example.wishlist.management.domain.Wish;
import com.example.wishlist.management.domain.WishRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WishListService {
    private final WishRepository wishRepository;

    public WishListService(WishRepository wishRepository) {
        this.wishRepository = wishRepository;
    }

    public WishDto add(WishDto request) {
        return WishDto.from(wishRepository.add(request.toDomain()));
    }

    public List<WishDto> getWishList() {
        return wishRepository.getWishList().stream().map(WishDto::from).toList();
    }

    public WishDto getWish(long id) {
        return WishDto.from(wishRepository.getWish(id));
    }

    public WishDto updateWish(long id, WishDto request) {
        Wish wish = request.toDomain();
        wish.setId(id);   // URL의 id 기준 처리도 서비스로 이동
        return WishDto.from(wishRepository.updateWish(wish));
    }

    public void deleteWish(long id) {
        wishRepository.deleteWish(id);
    }

    public List<WishDto> getRecommendWishList(long budget) {
        return wishRepository.getRecommendWishList(budget).stream().map(WishDto::from).toList();
    }
}