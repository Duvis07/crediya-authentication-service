package co.com.crediya.authentication.model.user.gateways;
import co.com.crediya.authentication.model.user.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserRepository {

    Mono<User> save(User user);

    Mono<User> findByEmail(String email);

    Mono<User> findByDocumentId(String documentId);

    Mono<Boolean> existsByEmail(String email);

    Flux<User> findAll();
}
