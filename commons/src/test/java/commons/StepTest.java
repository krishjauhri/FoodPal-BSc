package commons;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StepTest {

    private Step s1, s2, s3;

    @BeforeEach
    void setUp(){
        s1 = new Step(1, "Mix ingredients");
        s2 = new Step(2, "Heat the pan");
        s3 = new Step(1, "Mix ingredients");
    }

    @Test
    void testEquals() {
        assertEquals(s1, s3);
    }

    @Test
    void testHashCode() {
        assertNotEquals(s1.hashCode(), s2.hashCode());
    }
}