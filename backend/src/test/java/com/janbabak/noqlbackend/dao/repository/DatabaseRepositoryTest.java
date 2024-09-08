package com.janbabak.noqlbackend.dao.repository;

import com.janbabak.noqlbackend.model.entity.Database;
import com.janbabak.noqlbackend.model.entity.User;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.UUID;

@DataJpaTest
class DatabaseRepositoryTest {

    @Autowired
    private DatabaseRepository databaseRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Filter databases by user id")
    void testFilterDatabasesByUserId() {
        // given
        User user1 = User.builder()
                .id(UUID.randomUUID())
                .build();

        User user2 = User.builder()
                .id(UUID.randomUUID())
                .build();

        Database database1 = Database.builder()
                .id(UUID.randomUUID())
                .user(user1)
                .build();

        Database database2 = Database.builder()
                .id(UUID.randomUUID())
                .user(user1)
                .build();

        Database database3 = Database.builder()
                .id(UUID.randomUUID())
                .user(user2)
                .build();

        userRepository.save(user2);
        userRepository.save(user1);

        databaseRepository.save(database1);
        databaseRepository.save(database2);
        databaseRepository.save(database3);

        List<Database> expected = List.of(database1, database2);

        // when
        List<Database> actual = databaseRepository.findAllByUserId(user1.getId());

        // then
        assertEquals(actual, expected);
    }

}