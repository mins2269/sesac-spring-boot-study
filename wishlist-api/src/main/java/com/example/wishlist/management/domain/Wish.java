package com.example.wishlist.management.domain;

public class Wish {
    long id;
    String name;
    Integer price;



    public Wish(long id, String name, Integer price) {
        // 이름이 널, 빈 문자열, 공백만 있는 이름은 거부
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("이름은 필수입니다.");
        }
        // 가격 검사
        if(price == null || price < 0){
            throw new IllegalArgumentException("가격은 0 이상이어야 합니다. price=" + price);
        }
        this.id = id;
        // 공백 제거
        this.name = name.trim();
        this.price = price;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Integer getPrice() {
        return price;
    }

    public boolean sameId(long id){
        return this.id == id;
    }
}
