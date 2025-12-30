package com.janbabak.noqlbackend.dao.repository;

import com.janbabak.noqlbackend.model.entity.Database;
import com.janbabak.noqlbackend.model.entity.User;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.UUID;

@DataJpaTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DatabaseRepositoryTest {

    @Autowired
    private DatabaseRepository databaseRepository;

    @Autowired
    private UserRepository userRepository;

    @AfterAll
    void tearDown() {
        databaseRepository.deleteAll();
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

        final Database database1 = Database.builder()
                .id(UUID.randomUUID())
                .user(user1)
                .build();

        final Database database2 = Database.builder()
                .id(UUID.randomUUID())
                .user(user1)
                .build();

        final Database database3 = Database.builder()
                .id(UUID.randomUUID())
                .user(user2)
                .build();

        userRepository.save(user2);
        userRepository.save(user1);

        databaseRepository.save(database1);
        databaseRepository.save(database2);
        databaseRepository.save(database3);

        final List<Database> expected = List.of(database1, database2);

        // when
        final List<Database> actual = databaseRepository.findAllByUserId(user1.getId());

        // then
        assertEquals(actual, expected);
    }

}