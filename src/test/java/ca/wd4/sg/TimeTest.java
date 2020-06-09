package ca.wd4.sg;

import ca.wd4.sg.core.Time;
import org.junit.Assert;
import org.junit.Test;

public class TimeTest {

    @Test
    public void conflictTest1() {
        Time time1 = new Time(4, 12, 30, 13, 20);
        Time time2 = new Time(4, 13, 30, 14, 20);
        Assert.assertTrue(!time1.hasConflict(time2));
    }

}
