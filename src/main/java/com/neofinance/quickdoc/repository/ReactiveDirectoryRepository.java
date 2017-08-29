package com.neofinance.quickdoc.repository;

import com.neofinance.quickdoc.common.entities.FsCategory;
import com.neofinance.quickdoc.common.entities.FsDirectory;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReactiveDirectoryRepository extends ReactiveMongoRepository<FsDirectory, Long> {

    Mono<FsDirectory> findByPathAndParent(String path, Long parent);

    Flux<FsDirectory> findAllByParent(Long parent);
}
