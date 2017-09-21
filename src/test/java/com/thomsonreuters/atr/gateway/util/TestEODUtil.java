package com.thomsonreuters.atr.gateway.util;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalField;
import java.util.Calendar;

import static org.junit.Assert.fail;

/**
 * Created by UC216786 on 6/20/2017.
 */
public class TestEODUtil {
    @Rule
    public TemporaryFolder folder= new TemporaryFolder();

    //@Test
    public void testCleanSerialNumberFiles(){
        try {
            File ASKINNERFile = folder.newFile("ASKINNER");
            File ASKOUTERFile = folder.newFile("ASKOUTER");
            File BIDINNERFile = folder.newFile("BIDINNER");
            File BIDOUTERFile = folder.newFile("BIDOUTER");
            Instant now = Instant.now();
            LocalDateTime ldtNow = LocalDateTime.ofInstant(now, ZoneId.of("UTC"));
            //now is between eod yesterday and eod today, keep file
            EODUtil.cleanSerialNumberFiles(folder.getRoot().getPath(), ldtNow.get(ChronoField.HOUR_OF_DAY) + 2 + ":00");
            Assert.assertTrue(ASKINNERFile.exists());
            Assert.assertTrue(ASKOUTERFile.exists());
            Assert.assertTrue(BIDINNERFile.exists());
            Assert.assertTrue(BIDOUTERFile.exists());
            //now is after EOD today, keep file
            EODUtil.cleanSerialNumberFiles(folder.getRoot().getPath(), ldtNow.get(ChronoField.HOUR_OF_DAY) -2 + ":00");
            Assert.assertTrue(ASKINNERFile.exists());
            Assert.assertTrue(ASKOUTERFile.exists());
            Assert.assertTrue(BIDINNERFile.exists());
            Assert.assertTrue(BIDOUTERFile.exists());
            Instant yesterday = Instant.now();
            yesterday = yesterday.minus(1, ChronoUnit.DAYS);
            //creation time is before eod yesterday
            java.nio.file.Files.setAttribute(ASKINNERFile.toPath(), "creationTime", java.nio.file.attribute.FileTime.from(yesterday));
            java.nio.file.Files.setAttribute(ASKOUTERFile.toPath(), "creationTime", java.nio.file.attribute.FileTime.from(yesterday));
            java.nio.file.Files.setAttribute(BIDINNERFile.toPath(), "creationTime", java.nio.file.attribute.FileTime.from(yesterday));
            java.nio.file.Files.setAttribute(BIDOUTERFile.toPath(), "creationTime", java.nio.file.attribute.FileTime.from(yesterday));
            EODUtil.cleanSerialNumberFiles(folder.getRoot().getPath(), (ldtNow.get(ChronoField.HOUR_OF_DAY) + 2) + ":00");
            Assert.assertFalse(ASKINNERFile.exists());
            Assert.assertFalse(ASKOUTERFile.exists());
            Assert.assertFalse(BIDINNERFile.exists());
            Assert.assertFalse(BIDOUTERFile.exists());
        } catch (IOException e) {
            fail();
            e.printStackTrace();
        }

    }
}
