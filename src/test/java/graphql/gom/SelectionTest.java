package graphql.gom;

import org.junit.Test;

import java.util.HashSet;
import java.util.LinkedHashSet;

import static java.util.stream.Collectors.joining;
import static org.junit.Assert.*;

public final class SelectionTest {

    @Test
    public void contains() {
        Selection selection = new DefaultSelection(new HashSet<String>() {{
            add("foo");
        }});
        assertTrue(selection.contains("foo"));
        assertFalse(selection.contains("bar"));
    }

    @Test
    public void stream() {
        Selection selection = new DefaultSelection(new LinkedHashSet<String>() {{
            add("foo");
            add("bar");
        }});
        assertEquals("foobar", selection.stream().collect(joining()));
    }

}
