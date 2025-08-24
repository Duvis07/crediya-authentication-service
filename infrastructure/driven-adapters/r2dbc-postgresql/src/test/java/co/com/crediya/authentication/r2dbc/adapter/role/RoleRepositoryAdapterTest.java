package co.com.crediya.authentication.r2dbc.adapter.role;

import co.com.crediya.authentication.r2dbc.adapter.RoleRepositoryAdapter;
import co.com.crediya.authentication.r2dbc.entity.RoleEntity;
import co.com.crediya.authentication.r2dbc.mapper.RoleEntityMapper;
import co.com.crediya.authentication.r2dbc.repository.RoleEntityRepository;
import co.com.crediya.authentication.model.role.Role;
import co.com.crediya.authentication.model.role.gateways.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RoleRepositoryAdapterTest {

    private RoleEntityRepository roleEntityRepository;
    private RoleEntityMapper roleEntityMapper;
    private RoleRepository roleRepositoryAdapter;

    private RoleEntity roleEntity;
    private Role roleModel;

    @BeforeEach
    void setUp() {
        roleEntityRepository = mock(RoleEntityRepository.class);
        roleEntityMapper = mock(RoleEntityMapper.class);

        roleRepositoryAdapter = new RoleRepositoryAdapter(roleEntityRepository, roleEntityMapper);

        roleEntity = RoleEntity.builder().id(1L).name("Solicitante").build();
        roleModel = Role.builder().id(1L).name("Solicitante").build();
    }

    @Test
    void findByIdSuccess() {
        when(roleEntityRepository.findById(1L)).thenReturn(Mono.just(roleEntity));
        when(roleEntityMapper.toModel(roleEntity)).thenReturn(roleModel);

        StepVerifier.create(roleRepositoryAdapter.findById(1L))
                .expectNext(roleModel)
                .verifyComplete();

        verify(roleEntityRepository).findById(1L);
        verify(roleEntityMapper).toModel(roleEntity);
    }

    @Test
    void findByIdEmpty() {
        when(roleEntityRepository.findById(99L)).thenReturn(Mono.empty());

        StepVerifier.create(roleRepositoryAdapter.findById(99L))
                .verifyComplete();

        verify(roleEntityRepository).findById(99L);
        verify(roleEntityMapper, never()).toModel(any());
    }

    @Test
    void findByNameSuccess() {
        when(roleEntityRepository.findByName("Solicitante")).thenReturn(Mono.just(roleEntity));
        when(roleEntityMapper.toModel(roleEntity)).thenReturn(roleModel);

        StepVerifier.create(roleRepositoryAdapter.findByName("Solicitante"))
                .expectNext(roleModel)
                .verifyComplete();

        verify(roleEntityRepository).findByName("Solicitante");
        verify(roleEntityMapper).toModel(roleEntity);
    }

    @Test
    void findByNameEmpty() {
        when(roleEntityRepository.findByName("Inexistente")).thenReturn(Mono.empty());

        StepVerifier.create(roleRepositoryAdapter.findByName("Inexistente"))
                .verifyComplete();

        verify(roleEntityRepository).findByName("Inexistente");
        verify(roleEntityMapper, never()).toModel(any());
    }
}
