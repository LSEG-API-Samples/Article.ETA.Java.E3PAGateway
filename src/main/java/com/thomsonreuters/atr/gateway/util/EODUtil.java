package com.thomsonreuters.atr.gateway.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

/** Copyright (C) 2017 Thomson Reuters. All rights reserved.
 */
public class EODUtil {
    private final static Logger logger = LoggerFactory.getLogger(EODUtil.class);

    public static void cleanSerialNumberFiles(final String path2Files, final String eodTime)
    {
        cleanSerialNumberFile(path2Files, "ASKINNER", eodTime);
        cleanSerialNumberFile(path2Files, "ASKOUTER", eodTime);
        cleanSerialNumberFile(path2Files, "BIDINNER", eodTime);
        cleanSerialNumberFile(path2Files, "BIDOUTER", eodTime);
    }
    /*
    * eodTime is string with HH:mm format
     */
    private static void cleanSerialNumberFile(final String path2Files, final String fileName, final String eodTime){
        try {
            java.nio.file.Path file = java.nio.file.Paths.get(path2Files, fileName);
            java.nio.file.attribute.BasicFileAttributes attr = java.nio.file.Files.readAttributes(file, java.nio.file.attribute.BasicFileAttributes.class);
            //utc time
            java.nio.file.attribute.FileTime creationTime = attr.creationTime();
            String[] hourMinStr = eodTime.split(":");
            int hh = Integer.parseInt(hourMinStr[0]);
            int mm = Integer.parseInt(hourMinStr[1]);
            Instant eodYesterday = Instant.now();
            eodYesterday = eodYesterday.minus(1, ChronoUnit.DAYS);
            LocalDateTime ldtYesterday = LocalDateTime.ofInstant(eodYesterday, ZoneId.of("UTC"));
            ldtYesterday = ldtYesterday.withHour(hh);
            ldtYesterday = ldtYesterday.withMinute(mm);
            ldtYesterday = ldtYesterday.withSecond(0);
            eodYesterday = ldtYesterday.toInstant(ZoneOffset.UTC);

            Instant eodToday = Instant.now();
            LocalDateTime ldtToday = LocalDateTime.ofInstant(eodToday, ZoneId.of("UTC"));
            ldtToday = ldtToday.withHour(hh);
            ldtToday = ldtToday.withMinute(mm);
            ldtToday = ldtToday.withSecond(0);
            eodToday = ldtToday.toInstant(ZoneOffset.UTC);


            Instant now = Instant.now();
            boolean deleteFile = false;
            boolean creationTimeBeforeYesterday = creationTime.toMillis() <= eodYesterday.toEpochMilli();
            boolean creationTimeBetweenYesterdayAndToday = creationTime.toMillis() > eodYesterday.toEpochMilli() && creationTime.toMillis() <= eodToday.toEpochMilli();
            boolean creationTimeAfterToday = creationTime.toMillis() > eodToday.toEpochMilli();

            //assume now is after creation time
            //creation time is before eod yesteray, delete file
            if(creationTimeBeforeYesterday){
                deleteFile = true;
            }else
                //creation time is between eod yesterday and eod today and now is before eod today, keep the file
                if(creationTimeBetweenYesterdayAndToday && now.toEpochMilli() <= eodToday.toEpochMilli()){
                    deleteFile = false;
                }else
                    //creation time is between eod yesterday and eod today and now is after eod today, delete the file
                    if(creationTimeBetweenYesterdayAndToday && now.toEpochMilli() > eodToday.toEpochMilli()){
                        deleteFile = true;
                    }else
                        //creationg time is after eod today and now is after eod today, keep the file
                        if(creationTimeAfterToday && now.toEpochMilli() > eodToday.toEpochMilli()){
                            deleteFile = false;
                        }
            if(deleteFile){
                java.nio.file.Files.delete(file);
            }
        }catch (final InvalidPathException ipe){
            logger.error("Exception:", ipe);
            //don't care if file is not there
        }catch (final IOException ie) {
            logger.error("can't get attribute for file: {} ", path2Files + "/" + fileName);
        }
    }
}
