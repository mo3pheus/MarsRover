package space.exploration.mars.rover.kernel;

import junit.framework.TestCase;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Test;
import space.exploration.mars.rover.utils.FileUtil;

import java.io.File;
import java.util.List;

public class LogRetrievalTest extends TestCase {
    private DateTime startDate;
    private DateTime endDate;

    @Override
    public void setUp() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd~HH:mm:ss");
        startDate = formatter.parseDateTime("2018-05-29~00:00:00");
        endDate = formatter.parseDateTime("2018-05-30~00:00:00");
    }

    @Test
    public void testLogCrawling() {
        List<File> files = FileUtil.getLogFiles(startDate, endDate, "roverStatusReports/");
        System.out.println("=========================================================================");
        System.out.println("Running log file retrieval test for");
        System.out.println("startDate = " + startDate + " endDate = " + endDate);
        assertTrue(files.size() == 52);
        System.out.println("=========================================================================");
    }
}