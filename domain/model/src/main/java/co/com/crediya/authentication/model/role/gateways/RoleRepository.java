package co.com.crediya.authentication.model.role.gateways;

import co.com.crediya.authentication.model.role.Role;
import reactor.core.publisher.Mono;

public interface RoleRepository {

    Mono<Role> findByName(String name);

    Mono<Role> findById(Long id);
}