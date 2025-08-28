package com.devteria.identityservice.constant;

import lombok.Getter;

@Getter
public enum SelectorMovieDetail {
    TITLE("TITLE"),
    DESCRIPTION("DESCRIPTION"),
    CONTENT("CONTENT"),
    TRAILER("TRAILER"),
    ACTORS("ACTORS"),
    CATEGORY("CATEGORY"),
    COUNTRIES("COUNTRIES"),
    DIRECTORS("DIRECTORS"),
    THUMBNAIL_URL("THUMBNAIL_URL"),
    RELEASE_YEAR("RELEASE_YEAR"),
    POSTER_URL("POSTER_URL"),
    VIDEO_URL("VIDEO_URL"),
    VIDEO_SUBTITLE_URL("VIDEO_SUBTITLE"),
    VIDEO_DUBBING_URL("VIDEO_DUBBING_URL"),
    EPISODE_SERVER_NAME("EPISODE_SERVER_NAME"),
    SUBTITLE_BUTTON("SUBTITLE_BUTTON"),
    BUDDING_BUTTON("BUDDING_BUTTON"),
    ;

    private final String value;

    SelectorMovieDetail(String value) {
        this.value = value;
    }

}
