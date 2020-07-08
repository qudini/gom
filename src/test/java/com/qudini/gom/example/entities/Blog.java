package com.qudini.gom.example.entities;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public final class Blog extends Entity {

    private final String name;
    private final List<Article> articles = new ArrayList<>();

    public Blog(String name) {
        this.name = name;
    }

}
