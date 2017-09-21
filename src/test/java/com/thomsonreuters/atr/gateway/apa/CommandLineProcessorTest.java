package com.thomsonreuters.atr.gateway.apa;

import com.thomsonreuters.atr.gateway.fix.ConfigurationStrings;
import com.thomsonreuters.upa.fdm.config.FixDomainJSONConfiguration;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.Assert.*;

/** Copyright (C) 2017 Thomson Reuters. All rights reserved.
 */
public class CommandLineProcessorTest {



    @Test
    public void testDefaults() throws Exception {

        CommandLineProcessor commandLineProcessor = new CommandLineProcessor(CommandLineProcessor.CommandlineMode.PROVIDERMODE,
                buildArray(null, null, null, null, null, null));

        FixDomainJSONConfiguration configuration = commandLineProcessor.getFixDomainConfig();



        assertTrue(CommandLineProcessor.DEFAULT_FIELDS
                .equals(configuration.getConfigurationString(com.thomsonreuters.upa.framework.dispatcher.ConfigurationStrings.ProviderFieldsFile).get()));

        assertTrue(CommandLineProcessor.DEFAULT_ENUMS
                .equals(configuration.getConfigurationString(com.thomsonreuters.upa.framework.dispatcher.ConfigurationStrings.ProviderEnumssFile).get()));

        assertTrue(CommandLineProcessor.DEFAULT_PORT
                .equals(configuration.getConfigurationString(com.thomsonreuters.upa.framework.dispatcher.ConfigurationStrings.ProviderChannelPorts).get()));


        assertTrue("128"
                .equals(configuration.getConfigurationString(com.thomsonreuters.upa.framework.dispatcher.ConfigurationStrings.ProviderDomains).get()));

        assertTrue(CommandLineProcessor.DEFAULT_POST_FIELDS
                .equals(configuration.getConfigurationString(com.thomsonreuters.upa.framework.dispatcher.ConfigurationStrings.ProviderPostFieldsFile).get()));

        assertTrue(CommandLineProcessor.DEFAULT_POST_ENUMS
                .equals(configuration.getConfigurationString(com.thomsonreuters.upa.framework.dispatcher.ConfigurationStrings.ProviderPostEnumssFile).get()));



        Optional<String> fixtest = configuration.getConfigurationString(ConfigurationStrings.Downtime);

        assertTrue(fixtest.isPresent());
        System.err.println(fixtest.get());
        assertTrue("12:59".equals(fixtest.get()));

    }


    @Test
    public void testOverrides() throws Exception {

        CommandLineProcessor commandLineProcessor = new CommandLineProcessor(CommandLineProcessor.CommandlineMode.PROVIDERMODE,
                buildArray("foobar", "129", "../etc/fields", "../etc/enums" , "../etc/posts", "../etc/postsnums"));

        FixDomainJSONConfiguration configuration = commandLineProcessor.getFixDomainConfig();


        Optional<String> fixtest = configuration.getConfigurationString(ConfigurationStrings.Downtime);

        assertTrue(fixtest.isPresent());
        System.err.println(fixtest.get());
        assertTrue("12:59".equals(fixtest.get()));

    }

    private String[] buildArray(String port, String domain, String fixdictionary, String fixenums, String postdictionary,
                                String postenums)  {

        ArrayList<String> args = new ArrayList<>();

        args.add("../libs/*");
        args.add("APAProducer");

        if (port != null) {
            args.add("--port");
            args.add(port);
        }

        if (domain != null) {
            args.add("--domain");
            args.add(domain);
        }

        if (fixdictionary != null) {
            args.add("--fixdictionary");
            args.add(fixdictionary);
        }

        if (fixenums != null) {
            args.add("--fixenums");
            args.add(fixenums);
        }

        if (postdictionary != null) {
            args.add("--postdictionary");
            args.add(postdictionary);
        }

        if (postenums != null) {
            args.add("--postenums");
            args.add(postenums);
        }

        args.add("--serviceid");
        args.add("142");


        args.add("--sender");
        args.add("APA");

        args.add("--target");
        args.add("TRITON");

        args.add("--verbose");

        args.add("--version");
        args.add("FIX.X.X");

        args.add("--service");
        args.add("APA_SERVICE");

        args.add("--fixaddress");
        args.add("192.168.1.1");

        args.add("--fixport");
        args.add("54321");

        args.add("--connectiontype");
        args.add("INITIATOR");

        args.add("--heartbeat");
        args.add("75");

        args.add("--timezone");
        args.add("UTC");

        args.add("--eodtime");
        args.add("13:00");

        args.add("--uptime");
        args.add("13:05");

        args.add("--downtime");
        args.add("12:59");

        args.add("--adhuser");
        args.add("foobar");

        return args.toArray(new String[args.size()]);


    }




}