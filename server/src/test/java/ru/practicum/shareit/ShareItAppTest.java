package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ShareItServerApplicationTest {

    @Test
    void contextLoads() {
    }

    @Test
    void applicationStarts() {
        ShareItApp.main(new String[]{});
    }
}