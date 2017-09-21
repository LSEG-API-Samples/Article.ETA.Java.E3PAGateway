package com.thomsonreuters.atr.gateway.apa;

import com.thomsonreuters.atr.gateway.fix.ConfigurationStrings;
import com.thomsonreuters.upa.fdm.config.FixDomainJSONConfiguration;
import com.thomsonreuters.upa.fdm.dictionary.FixDomainTypes;
import com.thomsonreuters.upa.fdm.exceptions.FixDomainException;
import org.apache.commons.cli.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/** Copyright (C) 2017 Thomson Reuters. All rights reserved.
 * Processes array of command line arguments and builds application configuration file
 */
public class CommandLineProcessor {

    // Configuration default values
    static final String DEFAULT_PORT = "14003";
    static final String DEFAULT_FIELDS = "/etc/APAGateway/omm/FDMFixFieldDictionary";
    static final String DEFAULT_ENUMS = "/etc/APAGateway/omm/FDMenumtypes.def";
    static final String DEFAULT_POST_FIELDS = "/etc/APAGateway/omm/RDMFieldDictionary";
    static final String DEFAULT_POST_ENUMS = "/etc/APAGateway/omm/enumtype.def";
    private static final String DEFAULT_RULES_FILE = "/etc/APAGateway/rules/rules.json";
    private static final String DEFAULT_USER = "rmds";
    private static final String DEFAULT_FIX_VERSION = "FIXT.1.1";
    private static final String DEFAULT_HEARTBEAT = "30";
    private static final String JSON_LOG_PATH = "/var/log/APAGateway/json";
    private static final String DEFAULT_TIMEZONE = "EST";
    private static final String DEFAULT_EODTIME = "22:00";
    private static final String DEFAULT_UPTIME = "22:05";
    private static final String DEFAULT_DOWNTIME = "21:59";
    private static final String DEFAULT_FILELOG = "/var/log/APAGateway/fix";
    private static final String DEFAULT_STARTTIME = "00:00:00";
    private static final String DEFAULT_ENDTIME = "23:59:59";
    private static final String DEFAULT_USESSL= "N";

    // Configuration  file that is built and returned to app
    private FixDomainJSONConfiguration fixdomainconfig;

    // Assorted parameters that we need to be visible to accessors
    private String streamName;
    private String serviceName;
    private boolean verbose = false;

    private boolean nofix = false;

    private Map<String, Object> simulatorConfig = new HashMap<>();

    // Apache commons command line parser
    private final CommandLineParser parser = new BasicParser();
    private final HelpFormatter formatter = new HelpFormatter();
    private final Options options = new Options();

    // Mode with which to process command line
    public enum CommandlineMode {
        CLIENTMODE,
        PROVIDERMODE,
        SIMULATOR
    }

    /**
     * Build configuration from command line array
     *
     * @param mode with which which to process file
     * @param args command line argument array
     * @throws FixDomainException exception if arguments do not work
     */
    public CommandLineProcessor(CommandlineMode mode, String[] args) throws FixDomainException {
        switch (mode) {
            case CLIENTMODE:
                processConsumerConfig(args);
                break;
            case PROVIDERMODE:
                processProviderConfig(args);
                break;
            case SIMULATOR:
                processSimulatorConfig(args);
                break;

        }
    }

    /**
     * Get configuration object
     *
     * @return the configuration object
     */
    FixDomainJSONConfiguration getFixDomainConfig() {
        return fixdomainconfig;
    }

    /**
     * Return private stream user name
     *
     * @return the stream name
     */
    String getStreamName() {
        return streamName;
    }

    /**
     * Get service name
     *
     * @return the service name
     */
    String getServiceName() {
        return serviceName;
    }

    boolean isNofix() {
        return nofix;
    }


    /**
     * Process consumer configuration parameters
     *
     * @param args the command line arguments
     * @throws FixDomainException exception when parameters are invalid
     */
    private void processConsumerConfig(String[] args) throws FixDomainException {

        // Set up cli object
        options.addOption("h", "host", true, "Host for ADS")
                .addOption("p", "port", true, "Port for ADS")
                .addOption("s", "service", true, "Service name for private stream")
                .addOption("f", "fixdictionary", true, "FIX dictionary. Defaults to " + DEFAULT_POST_FIELDS)
                .addOption("x", "fixenums", true, "FIX enums. Defaults to " + DEFAULT_POST_ENUMS)
                .addOption("u", "user", true, "User id to establish private stream and connect to ADS defaults to " + DEFAULT_USER)
                .addOption("v", "verbose", false, "Enable verbose logging")
                .addOption("n", "name", true, "User name for stream, if different from ADS connection")
                .addOption("c", "client", false, "indicates client class should be run");


        // Parse
        try {
            CommandLine line = parser.parse(options, args);

            // Must have a host
            String host;
            if (line.hasOption("host")) {
                host = line.getOptionValue("host");
            } else {
                formatter.printHelp("APAConsumer: need a host to connect to.\n\n\n ", options);
                throw new FixDomainException("APAConsumer: need a host to connect to.");
            }

            // Must have a port
            String port;
            if (line.hasOption("port")) {
                port = line.getOptionValue("port");
            } else {
                formatter.printHelp("APAConsumer: need a port to connect to.\n\n\n ", options);
                throw new FixDomainException("APAConsumer: need a port to connect to.");
            }

            // Must have a service
            if (line.hasOption("service")) {
                serviceName = line.getOptionValue("service");
            } else {
                formatter.printHelp("APAConsumer: need a service to connect to.\n\n\n ", options);
                throw new FixDomainException("APAConsumer: need a service to connect to.");
            }

            // Must have a trep user
            String userName = DEFAULT_USER;
            if (line.hasOption("user")) {
                userName = line.getOptionValue("user");
            }

            // If no stream name, reuse ADS user name
            if (line.hasOption("name")) {
                streamName = line.getOptionValue("name");
            } else {
                // We know user name is set if we made it here
                streamName = userName;
            }

            // We may have an alternative location for the dictionary files
            String fixdictionary = DEFAULT_POST_FIELDS;
            String fixenums = DEFAULT_POST_ENUMS;
            if (line.hasOption("fixdictionary")) {
                fixdictionary = line.getOptionValue("fixdictionary");
            }

            if (line.hasOption("fixenums")) {
                fixenums = line.getOptionValue("fixenums");
            }

            // Verbose logging is optional
            if (line.hasOption("verbose")) {
                verbose = true;
            }

            // Create config object
            fixdomainconfig = makeConsumerConfig(userName, serviceName, fixdictionary, fixenums, host, port, verbose);
        } catch (JSONException | ParseException jse) {
            throw new FixDomainException(jse.getMessage());
        }
    }

    /**
     * Build consumer configuration object
     *
     * @param userName            - ads user name
     * @param upStreamServiceName - trep service name
     * @param host                - ads host
     * @param port                - ads port
     * @return the config object
     * @throws JSONException thrown on error
     */
    private static FixDomainJSONConfiguration makeConsumerConfig(String userName,
                                                                 String upStreamServiceName,
                                                                 String fixdictionary,
                                                                 String fixenums,
                                                                 String host,
                                                                 String port,
                                                                 boolean verbose) throws JSONException, FixDomainException {

        JSONObject dictionariesObject = new JSONObject()
                .put("File", fixdictionary)
                .put("Enums", fixenums);

        // Load our service name and dictionary information
        JSONObject consumer = new JSONObject().put("UpstreamServiceName", upStreamServiceName)
                .put("DataDictionary", dictionariesObject);

        // Add trace logging and consumer node
        JSONObject application = new JSONObject()
                .put("XMLTrace", verbose)
                .put("Consumer", consumer);

        // Save host and port information into arrays
        String[] hosts = new String[1];
        hosts[0] = host;
        String[] ports = new String[1];
        ports[0] = port;

        // Build connection node
        JSONObject consumerObject = new JSONObject()
                .put("Addresses", new JSONArray(hosts))
                .put("Ports", new JSONArray(ports))
                .put("RetryTimer", "5");

        // Build login node
        JSONObject loginObject = new JSONObject()
                .put("UserName", userName)
                .put("ApplicationName", "APAConsumer")
                .put("Type", "consumer");

        // Glue it all together
        JSONObject frameworkObject = new JSONObject()
                .put("ConsumerChannel", consumerObject)
                .put("Application", application)
                .put("ConsumerLogin", loginObject);
        JSONObject jsonObject = new JSONObject().put("UPAFramework", frameworkObject);

        return new FixDomainJSONConfiguration(jsonObject);
    }

    /**
     * Process configuration variables for a provider
     *
     * @param args command line arguments
     * @throws FixDomainException thrown on configuration error
     */
    private void processProviderConfig(String[] args) throws FixDomainException {

        // Set up command line processor
        options.addOption("p", "port", true, "Port to listen on. Defaults to " + DEFAULT_PORT)
                .addOption("a", "adhuser", true, "ADH user name. Defaults to " + DEFAULT_USER)
                .addOption("d", "domain", true, "TREP service domain default is " + FixDomainTypes.PIXL_QUERY_DOMAIN)
                .addOption("f", "fixdictionary", true, "FIX dictionary. Defaults to " + DEFAULT_FIELDS)
                .addOption("i", "serviceid", true, "Service Id to advertise")
                .addOption("q", "postdictionary", true, "Post dictionary. Defaults to " + DEFAULT_POST_FIELDS)
                .addOption("s", "sender", true, "FIX SenderCompId")
                .addOption("t", "target", true, "FIX TargetCompId")
                .addOption("v", "verbose", false, "Enable verbose logging")
                .addOption("w", "postenums", true, "Post enums. Defaults to " + DEFAULT_POST_ENUMS)
                .addOption("x", "fixenums", true, "FIX enums. Defaults to " + DEFAULT_ENUMS)
                .addOption("k", "version", true, "FIX Version: default is " + DEFAULT_FIX_VERSION)
                .addOption("y", "service", true, "Service name to advertise")

                .addOption("e", "fixaddress", true, "FIX IP Address")
                .addOption("g", "fixport", true, "FIX service port")
                .addOption("h", "connectiontype", true, "FIX connection type default: " + ConfigurationStrings.ConnectionInitiator)
                .addOption("j", "heartbeat", true, "FIX heartbeat interval default: 30 seconds")
                .addOption("z", "json", true, "JSON log path default: /var/log/APAGateway/json")
                .addOption("fl", "filelog", true, "qfx file log path default: /var/log/APAGateway/fix")
                .addOption("st", "starttime", true, "start time for fix connection default: 00:00:00")
                .addOption("et", "endtime", true, "end time for fix connection default: 23:59:59")
                .addOption("ssl", "useSSL", true, "SockertUseSSL default: N")

                .addOption("l", "timezone", true, "FIX engine timezone default: " + DEFAULT_TIMEZONE)
                .addOption("m", "eodtime", true, "FIX end-of-day time default " + DEFAULT_EODTIME)
                .addOption("n", "uptime", true, "FIX session up time default " + DEFAULT_UPTIME)
                .addOption("o", "downtime", true, "FIX session down time default " + DEFAULT_DOWNTIME)
                .addOption("N", "nofix", false, "do not start fix engine ")
                .addOption("R", "rulesfile", true,"path to file with JSON transformation rules")
                .addOption("S", "server", false, "indicates server class should be run");

        try {
            CommandLine line = parser.parse(options, args);

            // We need a port
            String port = DEFAULT_PORT;
            if (line.hasOption("port")) {
                port = line.getOptionValue("port");
            }

            // We need a service name
            String service = "";
            if (line.hasOption("service")) {
                service = line.getOptionValue("service");
            } else {
                formatter.printHelp("APAProducer: need a service to call myself.\n\n\n ", options);
                System.exit(-1);
            }

            // We need a TREP service ID
            String serviceId;
            if (line.hasOption("serviceid")) {
                serviceId = line.getOptionValue("serviceid");
            } else {
                formatter.printHelp("APAProducer: need a serviceId to call myself.\n\n\n ", options);
                throw new FixDomainException("APAProducer: need a serviceid to call myself.");
            }

            // We need an adh user name
            String userName = DEFAULT_USER;
            if (line.hasOption("adhuser")) {
                userName = line.getOptionValue("adhuser");
            }

            // We may have alternative locations for dictionary files
            String fixdictionary = DEFAULT_FIELDS;
            String postdictionary = DEFAULT_POST_FIELDS;
            String fixenums = DEFAULT_ENUMS;
            String postenums = DEFAULT_POST_ENUMS;
            if (line.hasOption("fixdictionary")) {
                fixdictionary = line.getOptionValue("fixdictionary");
            }

            if (line.hasOption("fixenums")) {
                fixenums = line.getOptionValue("fixenums");
            }

            if (line.hasOption("postdictionary")) {
                postdictionary = line.getOptionValue("postdictionary");
            }

            if (line.hasOption("postenums")) {
                postenums = line.getOptionValue("postenums");
            }

            // We may have an alternative domain
            String domain = Integer.toString(FixDomainTypes.PIXL_QUERY_DOMAIN);
            if (line.hasOption("domain")) {
                domain = line.getOptionValue("domain");
            }

            // Verbose logging is optional
            if (line.hasOption("verbose")) {
                verbose = true;
            }

            String senderCompId;
            if (line.hasOption("sender")) {
                senderCompId = line.getOptionValue("sender");
            } else {
                formatter.printHelp("APAProducer: need a sender!\n\n\n ", options);
                throw new FixDomainException("APAProducer: need a sender.");
            }

            String targetCompId;
            if (line.hasOption("target")) {
                targetCompId = line.getOptionValue("target");
            } else {
                formatter.printHelp("APAProducer: need a target!\n\n\n ", options);
                throw new FixDomainException("APAProducer: need a target.");
            }

            String fixVersion = DEFAULT_FIX_VERSION;
            if (line.hasOption("version")) {
                fixVersion = line.getOptionValue("version");
            }


            // We need a fix ip address
            String fixAddress;
            if (line.hasOption("fixaddress")) {
                fixAddress = line.getOptionValue("fixaddress");
            } else {
                formatter.printHelp("APAProducer: need a FIX address\n\n\n ", options);
                throw new FixDomainException("APAProducer: need a fix address.");
            }

            // We need a service port
            String fixPort;
            if (line.hasOption("fixport")) {
                fixPort = line.getOptionValue("fixport");
            } else {
                formatter.printHelp("APAProducer: need a FIX port\n\n\n ", options);
                throw new FixDomainException("APAProducer: need a fix port.");

            }

            String fixConnectionType = ConfigurationStrings.ConnectionInitiator;
            if (line.hasOption("connectiontype")) {
                fixPort = line.getOptionValue("connectiontype");
            }

            String heartbeat = DEFAULT_HEARTBEAT;
            if (line.hasOption("heartbeat")) {
                heartbeat = line.getOptionValue("heartbeat");
            }

            String jsonLogPath = JSON_LOG_PATH;
            if (line.hasOption("json")) {
                jsonLogPath = line.getOptionValue("json");
            }

            String fileLogPath = DEFAULT_FILELOG;
            if (line.hasOption("filelog")) {
                fileLogPath = line.getOptionValue("filelog");
            }

            String startTime = DEFAULT_STARTTIME;
            if (line.hasOption("starttime")) {
                startTime = line.getOptionValue("starttime");
            }

            String endTime = DEFAULT_ENDTIME;
            if (line.hasOption("endtime")) {
                endTime = line.getOptionValue("endtime");
            }

            String useSSL = DEFAULT_USESSL;
            if (line.hasOption("useSSL")) {
                useSSL = line.getOptionValue("useSSL");
            }

            String timezone = DEFAULT_TIMEZONE;
            if (line.hasOption("timezone")) {
                timezone = line.getOptionValue("timezone");
            }

            String eodtime = DEFAULT_EODTIME;
            if (line.hasOption("eodtime")) {
                eodtime = line.getOptionValue("eodtime");
            }

            String uptime = DEFAULT_UPTIME;
            if (line.hasOption("uptime")) {
                uptime = line.getOptionValue("uptime");
            }

            String downtime = DEFAULT_DOWNTIME;
            if (line.hasOption("downtime")) {
                downtime = line.getOptionValue("downtime");
            }

            if (line.hasOption("nofix")) {
                nofix = true;
            }

            String rulesFile = DEFAULT_RULES_FILE;
            if (line.hasOption("rulesfile")) {
                rulesFile = line.getOptionValue("rulesfile");
            }

            fixdomainconfig = makeProviderConfig(serviceId, service,
                    userName,
                    port,
                    domain,
                    fixdictionary,
                    fixenums,
                    postdictionary,
                    postenums,
                    verbose,
                    senderCompId,
                    targetCompId,
                    fixVersion,
                    fixAddress,
                    fixPort,
                    fixConnectionType,
                    heartbeat,
                    jsonLogPath,
                    fileLogPath,
                    startTime,
                    endTime,
                    useSSL,
                    timezone,
                    eodtime,
                    uptime,
                    downtime,
                    rulesFile);
        } catch (JSONException | ParseException jse) {
            throw new FixDomainException(jse.getMessage());
        }

    }

    /**
     * Build producer configuraton file
     *
     * @param serviceId      TREP service id
     * @param serviceName    TREP service name
     * @param userName       ADH user name
     * @param port           ADH port
     * @param domain         TREP message domain
     * @param fixdictionary  fix dictionary location
     * @param fixenums       fix enums location
     * @param postdictionary post dictionary location
     * @param postenums      post enums location
     * @param verbose        verbose logging flag
     * @return the configuraation object
     * @throws FixDomainException thrown on config error
     * @throws JSONException      thrown on JSON error
     */
    private static FixDomainJSONConfiguration makeProviderConfig(String serviceId,
                                                                 String serviceName,
                                                                 String userName,
                                                                 String port,
                                                                 String domain,
                                                                 String fixdictionary,
                                                                 String fixenums,
                                                                 String postdictionary,
                                                                 String postenums,
                                                                 boolean verbose,
                                                                 String senderCompId,
                                                                 String targetCompId,
                                                                 String fixVersion,
                                                                 String fixAddress,
                                                                 String fixPort,
                                                                 String connectionType,
                                                                 String heartbeat,
                                                                 String jsonLogPath,
                                                                 String fileLogPath,
                                                                 String startTime,
                                                                 String endTime,
                                                                 String useSSL,
                                                                 String timezone,
                                                                 String eodtime,
                                                                 String uptime,
                                                                 String downtime,
                                                                 String rulesFile)
            throws FixDomainException, JSONException {

        // Prepare both dictionary nodes
        JSONObject dictionariesObject = new JSONObject()
                .put("File", fixdictionary)
                .put("Enums", fixenums);

        JSONObject postDictionariesObject = new JSONObject()
                .put("File", postdictionary)
                .put("Enums", postenums);


        // Provider options
        JSONObject provider = new JSONObject()
                .put("ServiceId", serviceId)
                .put("Domains", domain)
                .put("Name", serviceName)
                .put("XMLTrace", Boolean.toString(verbose))
                .put("DataDictionary", dictionariesObject)
                .put("EnablePost", "true")
                .put("PostDataDictionary", postDictionariesObject)
                .put("AllowPublicStreams", "TRUE")
                .put("RulesFile", rulesFile);

        JSONObject application = new JSONObject().put("Provider", provider);

        // TREP channel
        JSONObject providerChannel = new JSONObject()
                .put("Port", port)
                .put("RetryTimer", "5");

        String[] names = new String[1];
        names[0] = userName;
        JSONObject loginObject = new JSONObject().put("UserName", new JSONArray(names));

        // Glue it all together
        JSONObject frameworkObject = new JSONObject()
                .put("ProviderLogin", loginObject)
                .put("ProviderChannel", providerChannel)
                .put("Application", application);

        // Fix connection
        JSONObject fixObject = new JSONObject()
                .put("SenderCompId", senderCompId)
                .put("TargetCompId", targetCompId)
                .put("Version", fixVersion);

        JSONObject fixConnectionObject = new JSONObject()
                .put("ConnectionType", connectionType)
                .put("Heartbeat", heartbeat)
                .put("Address", fixAddress)
                .put("Port", fixPort)
                .put("JsonLogPath", jsonLogPath)
                .put("FileLogPath", fileLogPath)
                .put("StartTime", startTime)
                .put("EndTime", endTime)
                .put("UseSSL", useSSL);

        JSONObject fixScheduleObject = new JSONObject()
                .put("Timezone", timezone)
                .put("EODTime", eodtime)
                .put("UpTime", uptime)
                .put("DownTime", downtime);

        JSONObject fixSessionManager = new JSONObject()
                .put("FIX", fixObject)
                .put("Connection", fixConnectionObject)
                .put("Schedule", fixScheduleObject);
        frameworkObject.put("FixSessionManager", fixSessionManager);

        JSONObject jsonObject = new JSONObject().put("UPAFramework", frameworkObject);


        // Return it
        return new FixDomainJSONConfiguration(jsonObject);
    }

    /**
     * Process consumer configuration parameters
     *
     * @param args the command line arguments
     * @throws FixDomainException exception when parameters are invalid
     */
    private void processSimulatorConfig(String[] args) throws FixDomainException {
        // Set up cli object
        options.addOption("p", "port", true, "Port to accept connection")
                .addOption("fv", "fixversion", true, "fixversion")
                .addOption("s", "sender", true, "SenderCompID")
                .addOption("t", "target", true, "TargetCompID")
                .addOption("ssl", "useSSL", true, "SockertUseSSL default: N")
                .addOption("ks", "keyStore", true, "SocketKeyStore default: ")
                .addOption("pw", "password", true, "SocketKeyStorePassword default: 123456")
                .addOption("c", "simulator", false, "indicates simulator class should be run");
        try {
            // Parse
            CommandLine line = parser.parse(options, args);

            // Must have a port
            String port = DEFAULT_PORT;
            if (line.hasOption("port")) {
                port = line.getOptionValue("port");
            } else {
                formatter.printHelp("FixResponder: need a port to connect to.\n\n\n ", options);
                System.exit(-1);
            }
            String fixVersion = DEFAULT_FIX_VERSION;
            if (line.hasOption("fixversion")) {
                fixVersion = line.getOptionValue("fixversion");
            } else {
                formatter.printHelp("FixResponder: need a fixVersion.\n\n\n ", options);
                System.exit(-1);
            }
            String senderCompID = "";
            if (line.hasOption("sender")) {
                senderCompID = line.getOptionValue("sender");
            } else {
                formatter.printHelp("FixResponder: need a senderCompID .\n\n\n ", options);
                System.exit(-1);
            }
            String targetCompID = "";
            if (line.hasOption("target")) {
                targetCompID = line.getOptionValue("target");
            } else {
                formatter.printHelp("FixResponder: need a target.\n\n\n ", options);
                System.exit(-1);
            }

            String useSSL = "N";
            if (line.hasOption("useSSL")) {
                useSSL = line.getOptionValue("useSSL");
            }

            String keyStorePath = "";
            if (line.hasOption("keyStore")) {
                keyStorePath = line.getOptionValue("keyStore");
            }
            if("Y".equals(useSSL) && "".equals(keyStorePath)){
                formatter.printHelp("FixResponder: need a KeyStorePath when using SSL.\n\n\n ", options);
                System.exit(-1);
            }

            String password = "";
            if (line.hasOption("password")) {
                password = line.getOptionValue("password");
            }

            simulatorConfig.put("port", port);
            //simulatorConfig.put("verbose", verbose);
            simulatorConfig.put("FixVersion", fixVersion);
            simulatorConfig.put("SenderCompID", senderCompID);
            simulatorConfig.put("TargetCompID", targetCompID);
            simulatorConfig.put("UseSSL", useSSL);
            simulatorConfig.put("KeyStorePath", keyStorePath);
            simulatorConfig.put("KeyStorePassword", password);

        } catch (ParseException pe) {
            pe.printStackTrace();
        }
    }

    public Map<String, Object> getSimulatorConfig() {
        return this.simulatorConfig;
    }
}
