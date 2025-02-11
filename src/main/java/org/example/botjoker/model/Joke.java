package org.example.botjoker.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Joke {
    @Column(length = 2550000)
    private String body;
    private String category;
    @Id
    private Long id;
    private double rating;
}
