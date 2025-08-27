package co.com.crediya.authentication.r2dbc.adapter;

import co.com.crediya.authentication.model.user.User;
import co.com.crediya.authentication.model.user.gateways.UserRepository;
import co.com.crediya.authentication.r2dbc.entity.UserEntity;
import co.com.crediya.authentication.r2dbc.mapper.UserEntityMapper;
import co.com.crediya.authentication.r2dbc.repository.UserEntityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
@Slf4j
public class UserRepositoryAdapter implements UserRepository {

    private final UserEntityRepository userEntityRepository;
    private final UserEntityMapper userEntityMapper;
    private final RoleRepositoryAdapter roleRepositoryAdapter;

    @Override
    @Transactional
    public Mono<User> save(User user) {
        log.debug("Saving user to database: {}", user.getEmail());
        return Mono.just(user)
                .map(userEntityMapper::toEntity)
                .flatMap(userEntityRepository::save)
                .map(userEntityMapper::toDomain)
                .doOnSuccess(savedUser -> log.debug("User successfully saved with ID: {}", savedUser.getId()));
    }

    @Override
    public Mono<User> findByEmail(String email) {
        log.debug("Searching user by email: {}", email);
        return userEntityRepository.findByEmail(email)
                .flatMap(this::enrichWithRole)
                .doOnNext(user -> log.debug("User found with email: {}", email));
    }

    @Override
    public Mono<Boolean> existsByEmail(String email) {
        return userEntityRepository.existsByEmail(email);
    }

    @Override
    public Flux<User> findAll() {
        log.debug("Fetching all users from database");
        return userEntityRepository.findAll()
                .flatMap(this::enrichWithRole)
                .doOnComplete(() -> log.debug("All users fetched successfully"));
    }

    // PRIVATE METHOD - Solo para operaciones de CONSULTA
    private Mono<User> enrichWithRole(UserEntity entity) {
        User userDomain = userEntityMapper.toDomain(entity);
        
        if (entity.getRoleId() == null) {
            return Mono.just(userDomain);
        }
        
        return roleRepositoryAdapter.findById(entity.getRoleId())
                .map(role -> userDomain.toBuilder().role(role).build())
                .switchIfEmpty(Mono.just(userDomain));
    }
}