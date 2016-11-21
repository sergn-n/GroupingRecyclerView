package ru.ncom.groupingrvadapter;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {


    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void t1() {
        GroupedList<String> gl = new GroupedList<>(null);
        gl.addAll("a","b");
        assertEquals(2,gl.size());
    }
}