package com.devteria.identityservice.entity;

import com.devteria.identityservice.constant.MovieStatus;
import com.devteria.identityservice.constant.MovieType;
import jakarta.persistence.*;
import java.util.Set;
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
public class Movie extends BaseEntity{
    @OneToOne
    Tmdb tmdb;

    @OneToOne
    Imdb imdb;

    @Column(nullable = false)
    String title;

    @Column(unique = true)
    String slug;

    @Column(name = "origin_name")
    String originName;

    @Column(name = "content", columnDefinition = "TEXT")
    String content;

    @Enumerated(EnumType.STRING)
    MovieType type; // series | single

    @Enumerated(EnumType.STRING)
    MovieStatus status; // completed | ongoing | unknown

    String posterUrl;
    String thumbnailUrl;

    @Column(name = "is_copyright")
    Boolean isCopyright;

    @Column(name = "sub_docquyen")
    Boolean subDocquyen;

    @Column(name = "chieurap")
    Boolean chieurap;

    @Column(name = "trailer_url")
    String trailerUrl;

    String time;

    @Column(name = "episode_current")
    String episodeCurrent;

    @Column(name = "episode_total")
    String episodeTotal;

    @Column(name = "quality")
    String quality; // FHD

    @Column(name = "lang")
    String lang; // Vietsub + Long tieng

    String notify;

    String showtimes;

    Integer year;

    Integer view;

    @ManyToMany
    @JoinTable(
            name = "movie_actor",
            joinColumns = @JoinColumn(name = "movie_id"),
            inverseJoinColumns = @JoinColumn(name = "actor_id"))
    Set<Actor> actors;

    @ManyToMany
    @JoinTable(
            name = "movie_director",
            joinColumns = @JoinColumn(name = "movie_id"),
            inverseJoinColumns = @JoinColumn(name = "director_id"))
    Set<Director> directors;

    @ManyToMany
    @JoinTable(
            name = "movie_category",
            joinColumns = @JoinColumn(name = "movie_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id"))
    Set<Category> categories;

    @ManyToMany
    @JoinTable(
            name = "movie_country",
            joinColumns = @JoinColumn(name = "movie_id"),
            inverseJoinColumns = @JoinColumn(name = "country_id"))
    Set<Country> countries;

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<Episode> episodes;
}


