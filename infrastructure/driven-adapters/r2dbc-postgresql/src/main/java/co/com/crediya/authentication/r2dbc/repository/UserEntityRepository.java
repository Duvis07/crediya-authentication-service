package co.com.crediya.authentication.r2dbc.repository;

import co.com.crediya.authentication.r2dbc.entity.UserEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import reactor.core.publisher.Mono;

public interface UserEntityRepository extends ReactiveCrudRepository<UserEntity, Long>, ReactiveQueryByExampleExecutor<UserEntity> {

    Mono<UserEntity> findByEmail(String email);

    Mono<UserEntity> findByDocumentId(String documentId);

    Mono<Boolean> existsByEmail(String email);
}
