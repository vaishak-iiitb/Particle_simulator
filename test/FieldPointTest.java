import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import org.junit.jupiter.api.Test;

import custom.Pack.FieldPoint.FieldPoint;

public class FieldPointTest {
    @Test
    public void testFieldPointParsing() {
        String input = "[100.0, 200.0]/50.0/A";
        FieldPoint fp = FieldPoint.parse(input);
        
        assertThat(fp, notNullValue());
        assertThat(fp.getFieldStrength(), is(50.0f));
        assertThat(fp.getType(), is("A"));
    }
}