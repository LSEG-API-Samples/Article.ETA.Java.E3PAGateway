package com.thomsonreuters.atr.gateway.apa;

import com.google.common.eventbus.Subscribe;
import com.thomsonreuters.upa.codec.DataDictionary;
import com.thomsonreuters.upa.fdm.config.FixDomainJSONConfiguration;
import com.thomsonreuters.upa.fdm.dictionary.DataDictionaryManager;
import com.thomsonreuters.upa.fdm.dictionary.InPlaceDataDictionaryManager;
import com.thomsonreuters.upa.fdm.encoders.FixMessageToFieldListEncoder;
import com.thomsonreuters.upa.fdm.encoders.FixToFixMessage;
import com.thomsonreuters.upa.fdm.encoders.PostMessageEncoder;
import com.thomsonreuters.upa.fdm.exceptions.FixDomainException;
import com.thomsonreuters.upa.fdm.messages.AckMessage;
import com.thomsonreuters.upa.fdm.messages.FixMessage;
import com.thomsonreuters.upa.fdm.messages.PostMessage;
import com.thomsonreuters.upa.framework.client.Consumer;
import com.thomsonreuters.upa.framework.events.*;
import com.thomsonreuters.upa.framework.exception.FrameworkException;

import java.io.*;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Stream;

/** Copyright (C) 2017 Thomson Reuters. All rights reserved.
 * Sample APA client application
*/
@SuppressWarnings("WeakerAccess")
public class APAConsumer implements Runnable {

    public static CommandLineProcessor commandLineProcessor;
    private Consumer consumer;
    private String userName;
    private String serviceName;
    private FixToFixMessage fixToFixMessage;
    private FixMessageToFieldListEncoder fixMessageToFieldListEncoder;
    private PostMessageEncoder postMessageEncoder;
    private StreamInfo streamInfo;
    private FrameworkServiceInfo serviceInfo;
    private boolean consoleStarted = false;
    private String currentDirectory;


    public APAConsumer() throws FrameworkException {
        init();
    }

    private void init() throws FrameworkException {
        try {

            // Create a Framework Consumer
            FixDomainJSONConfiguration configuration = commandLineProcessor.getFixDomainConfig();
            System.out.println("APAConsumer configuration: " + configuration.toString());

            consumer = new Consumer(configuration);

            // Add self as a subscriber (this registers callback below with Eventbus)
            consumer.addSubscriber(this);

            // Save user and service names for use when we open a stream
            userName = commandLineProcessor.getStreamName();
            serviceName = commandLineProcessor.getServiceName();

        } catch (FixDomainException | FrameworkException fe) {
            System.err.println("Error initializing: " + fe.getMessage());
            throw new FrameworkException(fe.getMessage());
        }
    }


    public static void main(String[] args) {

        try {

            // Initialize command line processor
            commandLineProcessor = new CommandLineProcessor(CommandLineProcessor.CommandlineMode.CLIENTMODE, args);
            APAConsumer apaConsumer = new APAConsumer();
            apaConsumer.start();
        } catch (Throwable e) {
            System.err.println("Exiting on exception " + e.getMessage());
        }
    }


    /**
     * Starts processing thread
     */
    protected void start() {

        try {
            Thread t = consumer.start();
            t.join();
        } catch (InterruptedException ie) {
            System.err.println("Interrupted. Exiting.");
            System.exit(-1);
        }

    }

    protected void openNewStream() {
        if ((streamInfo == null) && (serviceInfo.getState() == FrameworkServiceInfo.FrameworkServiceState.UP)) {
            try {
                System.err.println("Opening a stream");
                consumer.openPrivateStream(userName, serviceInfo);
            } catch (FrameworkException fe) {
                System.out.println("Error opening a new stream: " + fe.getMessage());
            }
        } else {
            System.out.println("Can't open another stream. We already have one");
        }
    }

    private void sendAPostMessage(StreamInfo streamInfo, String command) {

        if (command.length() < 3) {
            System.err.println("Please pass a file name in " +  currentDirectory + " after the 'p'");
            return;
        }

        String filename = currentDirectory + File.separatorChar + command.substring(2);

        try {
            BufferedReader inputReader =  new BufferedReader(new FileReader(filename));

            Stream<String> lines = inputReader.lines();
            lines.forEach(
                    (bad_fix)->{
                        //String bad_fix = inputReader.readLine();
                        System.err.println("Sending " + bad_fix);
                        PostMessage postMessage = fixToFixMessage.encodeToPostMessage(bad_fix, '|');

                        if (streamInfo != null) {
                            postMessage.setWantsAck(true);
                            postMessage.setPostId(1001);
                            try {
                                consumer.submitMessage(streamInfo, postMessage, postMessageEncoder);
                            }catch (final FrameworkException fe){
                                System.err.println("Error sending fix message. ErrorCode:  " + fe.getErrorCode() + " Message: " + fe.getMessage());
                            }
                            System.err.println("Sent message.");
                        } else {
                            System.err.println("Can't send a message without a stream.");
                        }
                    }
            );
        } catch (FileNotFoundException fnfe) {
            System.err.println("Fix file " + filename + " not found");
        }
    }

    private void changeDirectory(String name) {

        currentDirectory = name.substring(2);

        File currentFile = new File(currentDirectory);

        System.err.println("Current directory: " + currentDirectory);
        System.err.println("-----------------\nFile list: ");

        File[] filesList = currentFile.listFiles();
        if (filesList != null) {
            for (File file : filesList) {
                if (file.isFile()) {
                    if (file.getName().charAt(0) != '.')
                        System.err.println(file.getName());
                }
            }
        }
        System.err.println("-----------------");
        printHelp();
    }



    @SuppressWarnings("unused")
    @Subscribe
    public void processFixMessage(FixMessage fields) {
        System.err.println("\nReceived FIX Message: \n" + fields);
        printHelp();
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void processAckMessage(AckMessage ackMessage) {
        System.err.println("\nReceived Ack Message: \n" + ackMessage.toString());
        printHelp();
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void processChannelEvent(ChannelInfo channelInfo) {

        /*
         * Channels are recovered automatically, but an application may
         * want to do something in response to changes in channel state.
         *
         * Note that "UP" and "READY" are discrete states.
         */
        switch (channelInfo.getConnectionState()) {
            case READY:
                System.err.println("Channel is ready.");
                break;
            case UP:
                System.err.println("Channel is up, but not ready yet.");
                break;
            case DOWN:
                System.err.println("Channel is down and not recoverable.");
                break;
            case DOWN_RECOVER:
                System.err.println("Channel is down and will be recovered.");
                break;
            default:
                System.err.println("Received an unexpected channel state. Status: " + channelInfo.getConnectionState());
        }
    }


    /**
     * As services become available or unavailable the Framework sends FrameworkServiceInfos to us
     *
     * @param serviceInfo service events
     */
    @Subscribe
    public void processFrameworkServiceInfo(FrameworkServiceInfo serviceInfo) {

        System.err.println("Received service info: " + serviceInfo.getSourceName() + " " + serviceInfo.getState());
        switch (serviceInfo.getState()) {

            case UP:
                if (serviceInfo.getSourceName().equals(serviceName)) {

                    // The service info should have a dictionary within it
                    Optional<DataDictionary> optDictionary = serviceInfo.getDictionary();

                    if (optDictionary.isPresent()) {
                        // Create a dictionary manager with the dictionary supplied by the service
                        DataDictionaryManager dictionaryManager = new InPlaceDataDictionaryManager(optDictionary.get());
                        fixToFixMessage = new FixToFixMessage(dictionaryManager);
                        fixMessageToFieldListEncoder = new FixMessageToFieldListEncoder(dictionaryManager);
                        postMessageEncoder = new PostMessageEncoder(dictionaryManager, 60);
                        this.serviceInfo = serviceInfo;
                        openNewStream();
                    } else {
                        System.err.println("No dictionary!");
                    }

                }
                if (!consoleStarted) {
                    printHelp();
                    consoleStarted = true;
                    new Thread(this).start();
                }

                break;
            case DOWN:
                System.err.println("Service is down. Waiting for it to return");
                break;
        }
    }

    protected void printHelp() {
        System.err.println("Commands:\nx = exit program\np filename = post message (FIX separated with pipes |)\nc = change directory for post files\n");
    }


    @Subscribe
    public void processDictionaryInfo(FrameworkDictionaryInfo dictionaryInfo) {
        System.err.println("Received dictionary: " + dictionaryInfo.getFieldsName() + " Fields : "
                + dictionaryInfo.getDictionary().numberOfEntries() + " Enums : " + dictionaryInfo.getDictionary().enumTableCount());
    }


    @Override
    public void run() {

        Scanner scanner = new Scanner(System.in);

        changeDirectory("c " + "/etc/apagateway/posts");
        //changeDirectory("c " + "./clienthome");

        while (true) {
            System.err.println("Please enter a command (h for help): ");
            String command = scanner.nextLine();
            if (command.isEmpty()) {
                continue;
            }

            switch (command.substring(0, 1)) {

                case "x":
                    System.out.println("Exiting program.");
                    System.exit(1);
                    break;

                case "h":
                    printHelp();
                    break;

                case "p":
                    sendAPostMessage(streamInfo, command);
                    break;

                case "c":
                    changeDirectory(command);
                    break;
            }
        }
    }

    /**
     * Changes in stream state are sent via ReliableConsumerStreamInfo
     *
     * @param streamInfo our stream information
     */
    @Subscribe
    public void processConsumerStreamInfo(ReliableConsumerStreamInfo streamInfo) {

        // This app only uses one stream. More than one can be opened. Although some
        // service may only support one, apps can talk to many services at once.

        // Use the state to figure out what to do
        switch (streamInfo.getState()) {

            case CLOSED:
                System.err.println("Stream is closed forever. Exiting.");
                break;

            case CLOSED_RECOVERING:
                System.err.println("Stream is recovering. Waiting for it to return.");
                //noinspection UnusedAssignment,AssignmentToNull
                this.streamInfo = null;
                break;

            case OPEN:
                System.err.println("Stream is up!!!");

                if (consoleStarted) {
                    printHelp();
                }

                // Now that the stream is open, send a message
                this.streamInfo = streamInfo;
                break;

            case PENDING:
                if (this.streamInfo != null) {
                    System.err.println("POST was NACKED. Recovering stream.");
                    this.streamInfo = null;
                }
                break;
            default:
                System.err.println("Unexpected stream status " + streamInfo.getState());
        }
    }


}
