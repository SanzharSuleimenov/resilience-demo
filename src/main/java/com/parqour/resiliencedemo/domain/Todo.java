package com.parqour.resiliencedemo.domain;

public record Todo(int userId,
                   int id,
                   String title,
                   boolean completed) {

}
