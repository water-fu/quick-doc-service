package cn.techfan.quickdoc.repository;

import cn.techfan.quickdoc.common.entities.FsCategory;
import cn.techfan.quickdoc.common.entities.FsCategory;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.mongodb.repository.Query;
import reactor.core.publisher.Mono;

public interface ReactiveCategoryRepository extends ReactiveMongoRepository<FsCategory, Long> {

    Mono<FsCategory> findByType(String type);

}
