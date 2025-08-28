package com.devteria.identityservice.configuration;

import java.util.HashSet;
import java.util.Set;

import com.devteria.identityservice.constant.SelectorMovieDetail;
import com.devteria.identityservice.entity.Selector;
import com.devteria.identityservice.entity.SelectorItem;
import com.devteria.identityservice.repository.SelectorItemRepository;
import com.devteria.identityservice.repository.SelectorRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.devteria.identityservice.constant.PredefinedRole;
import com.devteria.identityservice.entity.Role;
import com.devteria.identityservice.entity.User;
import com.devteria.identityservice.repository.RoleRepository;
import com.devteria.identityservice.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ApplicationInitConfig {

    PasswordEncoder passwordEncoder;

    @NonFinal
    static final String ADMIN_USER_NAME = "admin";

    @NonFinal
    static final String ADMIN_PASSWORD = "admin";

    @Bean
    @ConditionalOnProperty(
            prefix = "spring",
            value = "datasource.driverClassName",
            havingValue = "com.mysql.cj.jdbc.Driver")
    ApplicationRunner applicationRunner(UserRepository userRepository, RoleRepository roleRepository, SelectorRepository selectorRepository, SelectorItemRepository selectorItemRepository) {
        log.info("Initializing application.....");
        return args -> {
            if (userRepository.findByUsername(ADMIN_USER_NAME).isEmpty()) {
                log.info("Creating admin user...");
                roleRepository.save(Role.builder()
                        .name(PredefinedRole.USER_ROLE)
                        .description("User role")
                        .build());

                Role adminRole = roleRepository.save(Role.builder()
                        .name(PredefinedRole.ADMIN_ROLE)
                        .description("Admin role")
                        .build());

                var roles = new HashSet<Role>();
                roles.add(adminRole);

                User user = User.builder()
                        .username(ADMIN_USER_NAME)
                        .password(passwordEncoder.encode(ADMIN_PASSWORD))
                        .roles(roles)
                        .build();

                userRepository.save(user);
                log.warn("admin user has been created with default password: admin, please change it");
            }
            if(selectorRepository.findAll().isEmpty()) {
                log.info("Creating default selector...");
                
                // Tạo Selector trước
                Selector selector123Hd = selectorRepository.save(Selector.builder()
                        .name("123hd")
                        .note("system default selector")
                        .build());

                // Sau đó tạo SelectorItem với reference đến Selector
                SelectorItem descriptionSelector = selectorItemRepository.save(SelectorItem.builder()
                        .selector(selector123Hd)
                        .name(SelectorMovieDetail.DESCRIPTION.getValue())
                        .query("meta[name=description]")
                        .note("system default selector")
                        .build());

                SelectorItem titleSelector = selectorItemRepository.save(SelectorItem.builder()
                        .selector(selector123Hd)
                        .name(SelectorMovieDetail.TITLE.getValue())
                        .query("h1.entry-title a")
                        .note("system default selector")
                        .build());

                SelectorItem videoUrlSelector = selectorItemRepository.save(SelectorItem.builder()
                        .selector(selector123Hd)
                        .name(SelectorMovieDetail.VIDEO_URL.getValue())
                        .query(".embed-responsive-item")
                        .attribute("src")
                        .note("system default selector")
                        .build());

                SelectorItem defaultEpisodeServerNameSelector = selectorItemRepository.save(SelectorItem.builder()
                        .selector(selector123Hd)
                        .name(SelectorMovieDetail.EPISODE_SERVER_NAME.getValue())
                        .query("#halim-list-server_n > ul > li")
                        .attribute("")
                        .note("system default selector")
                        .build());

                SelectorItem buddingButtonUrlSelector = selectorItemRepository.save(SelectorItem.builder()
                        .selector(selector123Hd)
                        .name(SelectorMovieDetail.BUDDING_BUTTON.getValue())
                        .query("#content > div > table > tbody > tr > th.selectmvbutton.lmselect-1")
                        .attribute("")
                        .note("system default selector")
                        .build());

                SelectorItem subtitleButtonUrlSelector = selectorItemRepository.save(SelectorItem.builder()
                        .selector(selector123Hd)
                        .name(SelectorMovieDetail.SUBTITLE_BUTTON.getValue())
                        .query("#content > div > table > tbody > tr > th.selectmvbutton.lmselect-2")
                        .attribute("")
                        .note("system default selector")
                        .build());

//                SelectorItem videoSubtitleUrlSelector = selectorItemRepository.save(SelectorItem.builder()
//                        .selector(selector123Hd)
//                        .name(SelectorMovieDetail.VIDEO_SUBTITLE_URL.getValue())
//                        .query(".embed-responsive-item")
//                        .attribute("src")
//                        .note("system default selector")
//                        .build());
//
//                SelectorItem videoDubbingUrlSelector = selectorItemRepository.save(SelectorItem.builder()
//                        .selector(selector123Hd)
//                        .name(SelectorMovieDetail.VIDEO_SUBTITLE_URL.getValue())
//                        .query(".embed-responsive-item")
//                        .attribute("src")
//                        .note("system default selector")
//                        .build());


                SelectorItem thumbnailUrlSelector = selectorItemRepository.save(SelectorItem.builder()
                        .selector(selector123Hd)
                        .name(SelectorMovieDetail.THUMBNAIL_URL.getValue())
                        .query("#content > div > div.halim-movie-wrapper.tpl-2 > div.movie_info.col-xs-12 > div.movie-poster.col-md-4 > img")
                        .attribute("src")
                        .note("system default selector")
                        .build());

//                Selector selectorNung2Hdd = selectorRepository.save(Selector.builder()
//                        .name("nhung2-hdd")
//                        .note("system default selector")
//                        .build());
//
//                SelectorItem descriptionSelectorNung2Hdd = selectorItemRepository.save(SelectorItem.builder()
//                        .selector(selectorNung2Hdd)
//                        .name(SelectorMovieDetail.DESCRIPTION.getValue())
//                        .query("meta[name=description]")
//                        .note("system default selector")
//                        .build());
//
//                SelectorItem contentSelectorNung2Hdd = selectorItemRepository.save(SelectorItem.builder()
//                        .selector(selectorNung2Hdd)
//                        .name(SelectorMovieDetail.CONTENT.getValue())
//                        .query("div.dt_content_text")
//                        .note("system default selector")
//                        .build());
//
//                SelectorItem titleSelectorNung2Hdd = selectorItemRepository.save(SelectorItem.builder()
//                        .selector(selectorNung2Hdd)
//                        .name(SelectorMovieDetail.TITLE.getValue())
//                        .query("div.title h1 a")
//                        .note("system default selector")
//                        .build());
//
//                SelectorItem videoUrlSelectorNung2Hdd = selectorItemRepository.save(SelectorItem.builder()
//                        .selector(selectorNung2Hdd)
//                        .name(SelectorMovieDetail.VIDEO_URL.getValue())
//                        .query("#frame")
//                        .attribute("src")
//                        .note("system default selector")
//                        .build());
//
//                SelectorItem directorSelectorNung2Hdd = selectorItemRepository.save(SelectorItem.builder()
//                        .selector(selectorNung2Hdd)
//                        .name(SelectorMovieDetail.DIRECTORS.getValue())
//                        .query("#content > div > div.leftC > div:nth-child(8) > div > div.dt_content_wraper > div > div:nth-child(3)")
//                        .note("system default selector")
//                        .build());
//
//                SelectorItem actorSelectorNung2Hdd = selectorItemRepository.save(SelectorItem.builder()
//                        .selector(selectorNung2Hdd)
//                        .name(SelectorMovieDetail.ACTORS.getValue())
//                        .query("#content > div > div.leftC > div:nth-child(8) > div > div.dt_content_wraper > div > div:nth-child(4)")
//                        .note("system default selector")
//                        .build());
//
//                SelectorItem trailerSelectorNung2Hdd = selectorItemRepository.save(SelectorItem.builder()
//                        .selector(selectorNung2Hdd)
//                        .name(SelectorMovieDetail.TRAILER.getValue())
//                        .query("#content > div > div.leftC > div:nth-child(8) > div > div.filmalti > div.filmaltiaciklama > div > iframe")
//                        .attribute("src")
//                        .note("system default selector")
//                        .build());
//
//                SelectorItem thumbnailUrlSelectorNung2Hdd = selectorItemRepository.save(SelectorItem.builder()
//                        .selector(selectorNung2Hdd)
//                        .name(SelectorMovieDetail.THUMBNAIL_URL.getValue())
//                        .query("#content > div > div.leftC > div:nth-child(8) > div > div.filmalti > div.filmaltiimg > img")
//                        .attribute("src")
//                        .note("system default selector")
//                        .build());
//
//                SelectorItem posterUrlSelectorNung2Hdd = selectorItemRepository.save(SelectorItem.builder()
//                        .selector(selectorNung2Hdd)
//                        .name(SelectorMovieDetail.POSTER_URL.getValue())
//                        .query("#content > div > div.leftC > div:nth-child(8) > div > div.filmalti > div.filmaltiimg > img")
//                        .attribute("src")
//                        .note("system default selector")
//                        .build());

                log.info("Default selector has been created");
            }
            log.info("Application initialization completed .....");
        };
    }

}
