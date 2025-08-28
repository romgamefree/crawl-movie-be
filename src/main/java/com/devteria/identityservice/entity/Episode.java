package com.devteria.identityservice.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity(name = "episode")
public class Episode extends BaseEntity {
    @Column(name = "server_name")
    String serverName; // nếu cần phân biệt server #Hà Nội (Vietsub)

    @ManyToOne(optional = false)
    @JoinColumn(name = "movie_id")
    Movie movie;

    @OneToMany(mappedBy = "episode", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<ServerData> serverData;
}
