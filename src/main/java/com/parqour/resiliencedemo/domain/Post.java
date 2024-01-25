package com.parqour.resiliencedemo.domain;

public record Post(
    int userId,
    int id,
    String title,
    String body
) {

}
