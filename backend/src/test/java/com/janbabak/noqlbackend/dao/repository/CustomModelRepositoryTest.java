package com.janbabak.noqlbackend.dao.repository;

import com.janbabak.noqlbackend.model.entity.CustomModel;
import com.janbabak.noqlbackend.model.entity.User;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CustomModelRepositoryTest {

    @Autowired
    private CustomModelRepository customModelRepository;

    @Autowired
    private UserRepository userRepository;

    @AfterAll
    void tearDown() {
        customModelRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Filter databases by user id")
    void testFilterDatabasesByUserId() {
        // given
        final User user1 = User.builder()
                .id(UUID.randomUUID())
                .email("user1@email.cz")
                .build();

        final User user2 = User.builder()
                .id(UUID.randomUUID())
                .email("user2@email.cz")
                .build();

        final CustomModel model1 = CustomModel.builder()
                .id(UUID.randomUUID())
                .user(user1)
                .build();

        final CustomModel model2 = CustomModel.builder()
                .id(UUID.randomUUID())
                .user(user1)
                .build();

        final CustomModel model3 = CustomModel.builder()
                .id(UUID.randomUUID())
                .user(user2)
                .build();

        userRepository.save(user2);
        userRepository.save(user1);

        customModelRepository.save(model1);
        customModelRepository.save(model2);
        customModelRepository.save(model3);

        final List<CustomModel> expected = List.of(model1, model2);

        // when
        final List<CustomModel> actual = customModelRepository.findAllByUserId(user1.getId());

        // then
        assertEquals(actual, expected);
    }

}