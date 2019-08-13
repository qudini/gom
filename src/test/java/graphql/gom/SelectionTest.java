package graphql.gom;

import org.junit.Test;

import java.util.HashSet;
import java.util.LinkedHashSet;

import static java.util.stream.Collectors.joining;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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

    @Test
    public void empty() {
        Selection selection = Selection.empty();
        assertFalse(selection.contains("foo"));
        assertEquals("", selection.stream().collect(joining()));
    }

}
