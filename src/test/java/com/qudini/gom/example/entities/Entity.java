package com.qudini.gom.example.entities;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public abstract class Entity {

    private final int id = Database.ID_GENERATOR.incrementAndGet();

}
