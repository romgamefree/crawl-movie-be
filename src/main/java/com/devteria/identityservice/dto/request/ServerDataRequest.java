package com.devteria.identityservice.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ServerDataRequest {
    String episodeId;
    String name;
    String slug;
    String filename;
    String link_embed;
    String link_m3u8;
}


