package com.devteria.identityservice.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class ServerData extends BaseEntity{
    @ManyToOne(optional = false)
    @JoinColumn(name = "episode_id")
    Episode episode;

    @Column(name = "name")
    String name;

    @Column(name = "slug")
    String slug;

    @Column(name = "filename")
    String filename;

    @Column(name = "link_embed")
    String link_embed;

    @Column(name = "link_m3u8")
    String link_m3u8;
}


