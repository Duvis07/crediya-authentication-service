package co.com.crediya.authentication.r2dbc.adapter.user;


import co.com.crediya.authentication.model.role.Role;
import co.com.crediya.authentication.model.user.User;
import co.com.crediya.authentication.r2dbc.adapter.RoleRepositoryAdapter;
import co.com.crediya.authentication.r2dbc.adapter.UserRepositoryAdapter;
import co.com.crediya.authentication.r2dbc.entity.UserEntity;
import co.com.crediya.authentication.r2dbc.mapper.UserEntityMapper;
import co.com.crediya.authentication.r2dbc.repository.UserEntityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserRepositoryAdapterTest {

    private UserEntityRepository userEntityRepository;
    private UserEntityMapper userEntityMapper;
    private RoleRepositoryAdapter roleRepositoryAdapter;
    private UserRepositoryAdapter userRepositoryAdapter;

    private User userDomain;
    private UserEntity userEntity;
    private Role role;

    @BeforeEach
    void setUp() {
        userEntityRepository = mock(UserEntityRepository.class);
        userEntityMapper = mock(UserEntityMapper.class);
        roleRepositoryAdapter = mock(RoleRepositoryAdapter.class);

        userRepositoryAdapter = new UserRepositoryAdapter(
                userEntityRepository, userEntityMapper, roleRepositoryAdapter);

        userDomain = User.builder()
                .id(1L)
                .firstName("Juan")
                .lastName("Pérez")
                .email("juan@example.com")
                .role(null)
                .build();

        userEntity = UserEntity.builder()
                .id(1L)
                .firstName("Juan")
                .lastName("Pérez")
                .email("juan@example.com")
                .roleId(10L)
                .build();

        role = Role.builder().id(10L).name("Solicitante").build();
    }

    @Test
    void saveUserSuccess() {
        when(userEntityMapper.toEntity(userDomain)).thenReturn(userEntity);
        when(userEntityRepository.save(userEntity)).thenReturn(Mono.just(userEntity));
        when(userEntityMapper.toDomain(userEntity)).thenReturn(userDomain);

        StepVerifier.create(userRepositoryAdapter.save(userDomain))
                .expectNext(userDomain)
                .verifyComplete();

        verify(userEntityRepository).save(userEntity);
    }

    @Test
    void findByEmailWithRole() {
        when(userEntityRepository.findByEmail("juan@example.com")).thenReturn(Mono.just(userEntity));
        when(userEntityMapper.toDomain(userEntity)).thenReturn(userDomain);
        when(roleRepositoryAdapter.findById(10L)).thenReturn(Mono.just(role));

        StepVerifier.create(userRepositoryAdapter.findByEmail("juan@example.com"))
                .expectNextMatches(user -> user.getRole().getName().equals("Solicitante"))
                .verifyComplete();

        verify(userEntityRepository).findByEmail("juan@example.com");
        verify(roleRepositoryAdapter).findById(10L);
    }


    @Test
    void existsByEmailReturnsTrue() {
        when(userEntityRepository.existsByEmail("juan@example.com")).thenReturn(Mono.just(true));

        StepVerifier.create(userRepositoryAdapter.existsByEmail("juan@example.com"))
                .expectNext(true)
                .verifyComplete();
    }


    @Test
    void findAllUsers() {
        when(userEntityRepository.findAll()).thenReturn(Flux.just(userEntity));
        when(userEntityMapper.toDomain(userEntity)).thenReturn(userDomain);
        when(roleRepositoryAdapter.findById(10L)).thenReturn(Mono.just(role));

        StepVerifier.create(userRepositoryAdapter.findAll())
                .expectNextMatches(user -> user.getRole().getName().equals("Solicitante"))
                .verifyComplete();

        verify(userEntityRepository).findAll();
    }
}
