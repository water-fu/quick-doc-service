package cn.mxleader.quickdoc.config;

import cn.mxleader.quickdoc.entities.FsDetail;
import cn.mxleader.quickdoc.entities.FsOwner;
import cn.mxleader.quickdoc.entities.UserEntity;
import cn.mxleader.quickdoc.service.*;
import cn.mxleader.quickdoc.service.impl.KafkaServiceImpl;
import cn.mxleader.quickdoc.service.impl.ReactiveUserServiceImpl;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;

import static cn.mxleader.quickdoc.common.utils.AuthenticationUtil.SYSTEM_ADMIN_GROUP_OWNER;
import static cn.mxleader.quickdoc.security.config.WebSecurityConfig.AUTHORITY_ADMIN;
import static cn.mxleader.quickdoc.security.config.WebSecurityConfig.AUTHORITY_USER;

@SpringBootConfiguration
@ConditionalOnClass(KafkaService.class)
@EnableConfigurationProperties(QuickDocProperties.class)
public class QuickDocConfiguration {
    private final Logger log = LoggerFactory.getLogger(QuickDocConfiguration.class);

    private final QuickDocProperties quickDocProperties;

    public QuickDocConfiguration(QuickDocProperties quickDocProperties) {
        this.quickDocProperties = quickDocProperties;
    }

    @Bean
    @ConditionalOnProperty(prefix = "quickdoc", value = "stream-topic")
    public KafkaService kafkaService(KafkaTemplate<String, String> kafkaTemplate) {
        return new KafkaServiceImpl(kafkaTemplate, quickDocProperties.getStreamTopic());
    }

    @Bean
    CommandLineRunner setupConfiguration(ReactiveUserServiceImpl reactiveUserService,
                                         ReactiveCategoryService reactiveCategoryService,
                                         ReactiveDirectoryService reactiveDirectoryService,
                                         ReactiveQuickDocConfigService reactiveQuickDocConfigService) {
        return args -> {
            reactiveQuickDocConfigService.getQuickDocConfig()
                    .flatMap(quickDocConfig -> {
                        if (!quickDocConfig.getInitialized()) {
                            // 初始化Admin管理账号
                            reactiveUserService
                                    .saveUser(new UserEntity(ObjectId.get(), "root",
                                            "chenbichao",
                                            new String[]{AUTHORITY_ADMIN, AUTHORITY_USER},
                                            "administrators")).subscribe();

                            // 初始化文件分类
                            reactiveCategoryService.addCategory("照片").subscribe();
                            reactiveCategoryService.addCategory("音乐").subscribe();
                            reactiveCategoryService.addCategory("图书").subscribe();
                            reactiveCategoryService.addCategory("视频").subscribe();

                            // 初始化根目录
                            FsOwner[] configOwners = {SYSTEM_ADMIN_GROUP_OWNER};
                            reactiveDirectoryService.saveDirectory("config", quickDocConfig.getId(),
                                    false, configOwners).subscribe();

                            FsOwner[] rootOwners = {SYSTEM_ADMIN_GROUP_OWNER};
                            reactiveDirectoryService.saveDirectory("root", quickDocConfig.getId(),
                                    true, rootOwners).subscribe();

                            quickDocConfig.setInitialized(true);
                        }
                        quickDocConfig.setStartup(new Date());
                        return reactiveQuickDocConfigService.saveQuickDocConfig(quickDocConfig);
                    })
                    .onErrorMap(v -> {
                        log.warn(v.getMessage());
                        return v;
                    })
                    .subscribe(v -> log.info(v.toString()));
        };
    }

    //@Bean
    CommandLineRunner initCategory(ReactiveCategoryService reactiveCategoryService) {
        return args -> {
            for (int i = 0; i < 10; i++) {
                Thread.sleep(280);
                reactiveCategoryService.addCategory("科技").subscribe();
            }
            reactiveCategoryService.findByType("科技").subscribe(System.out::println);

            // 更改分类名
            reactiveCategoryService.renameCategory("科技", "人文")
                    .onErrorMap(v -> {
                        log.warn(v.getMessage());
                        return v;
                    })
                    .subscribe(System.out::println);

            // 删除分类
            reactiveCategoryService.deleteCategory("人文俩hi")
                    .onErrorMap(v -> {
                        log.warn(v.getMessage());
                        return v;
                    })
                    .subscribe(System.out::println);
        };
    }

    //@Bean
    CommandLineRunner uploadLocalFiles(ReactiveFileService reactiveFileService) {
        return args -> {
            File directory = new File("E:\\BaiduYunDownload\\发展路线图");
            if (directory.exists() && directory.isDirectory()) {
                Flux.just(directory.listFiles())
                        .filter(file -> file.getName().toLowerCase().endsWith(".pdf"))
                        .map(
                                file -> {
                                    try {
                                        FsDetail fsDetail = new FsDetail(ObjectId.get(),
                                                file.getName(),
                                                file.length(),
                                                StringUtils.getFilenameExtension(file.getName()).toLowerCase(),
                                                new Date(),
                                                ObjectId.get(),
                                                ObjectId.get(),
                                                null,
                                                false,
                                                getRandomOwners(),
                                                null,
                                                null);
                                        reactiveFileService.storeFile(
                                                fsDetail,
                                                new FileInputStream(file))
                                                .subscribe();
                                    } catch (IOException exp) {
                                        exp.printStackTrace();
                                    }
                                    return file;
                                }
                        ).subscribe(v -> log.info(v.toString()));
            }
        };
    }

    private FsOwner[] getRandomOwners() {
        FsOwner owners[] = new FsOwner[3];
        owners[0] = new FsOwner("chenbichao", FsOwner.Type.TYPE_PRIVATE, 7);
        owners[1] = new FsOwner("陈毕超", FsOwner.Type.TYPE_PRIVATE, 7);
        owners[2] = new FsOwner("Michael Chen", FsOwner.Type.TYPE_PUBLIC, 7);
        return owners;
    }

}