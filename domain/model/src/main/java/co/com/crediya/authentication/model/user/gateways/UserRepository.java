package co.com.crediya.authentication.model.user.gateways;
import co.com.crediya.authentication.model.user.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserRepository {

    Mono<User> save(User user);

    Mono<User> findByEmail(String email);

    Mono<Boolean> existsByEmail(String email);

    Mono<User> findById(Long id);

    Flux<User> findAll();

    Mono<User> update(User user);

    Mono<Void> deleteById(Long id);
}
