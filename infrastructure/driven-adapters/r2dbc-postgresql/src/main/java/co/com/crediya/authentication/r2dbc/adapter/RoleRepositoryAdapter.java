package co.com.crediya.authentication.r2dbc.adapter;

import co.com.crediya.authentication.r2dbc.repository.RoleEntityRepository;
import co.com.crediya.authentication.model.role.Role;
import co.com.crediya.authentication.model.role.gateways.RoleRepository;
import co.com.crediya.authentication.r2dbc.mapper.RoleEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class RoleRepositoryAdapter implements RoleRepository {

    private final RoleEntityRepository roleEntityRepository;

    private final RoleEntityMapper roleEntityMapper;

    @Override
    public Mono<Role> findById(Long id) {
        return roleEntityRepository.findById(id)
                .map(roleEntityMapper::toModel);
    }

    @Override
    public Mono<Role> findByCode(String code) {
        return roleEntityRepository.findByName(code)
                .map(roleEntityMapper::toModel);
    }
}
