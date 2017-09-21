package com.thomsonreuters.atr.gateway.apa;

import com.thomsonreuters.upa.framework.events.ReliableProducerStreamInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.*;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.easymock.EasyMock.createMock;

/** Copyright (C) 2017 Thomson Reuters. All rights reserved.
 */
@SuppressWarnings("OptionalGetWithoutIsPresent")
@RunWith(PowerMockRunner.class)
@PrepareForTest({ReliableProducerStreamInfo.class})
public class ConsumerWatchListTest {



    private ReliableProducerStreamInfo streamInfo1;
    private ReliableProducerStreamInfo streamInfo2;
    private ReliableProducerStreamInfo streamInfo3;
    private ReliableProducerStreamInfo streamInfo4;


    private final ConsumerWatchList watchList = new ConsumerWatchList();


    @Before
    public void setUp() throws Exception {
                streamInfo1 = createMock(ReliableProducerStreamInfo.class);
                streamInfo2 = createMock(ReliableProducerStreamInfo.class);
                streamInfo3 = createMock(ReliableProducerStreamInfo.class);
                streamInfo4 = createMock(ReliableProducerStreamInfo.class);
    }


    @Test
    public void testAddGetNode() throws Exception {

        watchList.addNode("test1", streamInfo1);
        watchList.addNode("test2", streamInfo2);
        watchList.addNode("test3", streamInfo3);
        watchList.addNode("test4", streamInfo4);

        Optional<ReliableProducerStreamInfo> got1 = watchList.getNode("test1");
        assertTrue(got1.isPresent());
        assertEquals(streamInfo1, got1.get());

        Optional<ReliableProducerStreamInfo> got2 = watchList.getNode("test2");
        assertTrue(got2.isPresent());
        assertEquals(streamInfo2, got2.get());

        Optional<ReliableProducerStreamInfo> got3 = watchList.getNode("test3");
        assertTrue(got3.isPresent());
        assertEquals(streamInfo3, got3.get());

        Optional<ReliableProducerStreamInfo> got4 = watchList.getNode("test4");
        assertTrue(got4.isPresent());
        assertEquals(streamInfo4, got4.get());

        // Not going to spend a lot of time verifying that hashmaps work...
        Collection<ReliableProducerStreamInfo> info = watchList.getEntries();
        assertEquals(4, info.size());

        watchList.deleteNode("test2");

        // One less now
        info = watchList.getEntries();
        assertEquals(3, info.size());


    }

}