package graphql.gom.example.entities;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import static graphql.gom.example.entities.Database.ID_GENERATOR;

@Getter
@EqualsAndHashCode
public abstract class Entity {

    private final int id = ID_GENERATOR.incrementAndGet();

}
