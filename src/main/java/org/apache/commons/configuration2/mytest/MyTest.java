package org.apache.commons.configuration2.mytest;

import org.apache.commons.configuration2.AbstractConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.BasicConfigurationBuilder;
import org.apache.commons.configuration2.builder.EventListenerParameters;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.ReloadingFileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.FileBasedBuilderParameters;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.builder.fluent.PropertiesBuilderParameters;
import org.apache.commons.configuration2.builder.fluent.XMLBuilderParameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.event.ConfigurationEvent;
import org.apache.commons.configuration2.event.EventListener;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.reloading.PeriodicReloadingTrigger;
import org.apache.commons.configuration2.tree.xpath.XPathExpressionEngine;

import java.io.File;
import java.util.concurrent.TimeUnit;


public class MyTest {

    public static void main(String[] args) {

        testPropertiesLoad();

        //testEvent4Configuration();

        System.out.println("xxxxxxxxx");
    }

    public static void testPropertiesLoad() {
        Parameters params = new Parameters();
        PropertiesBuilderParameters propertiesParams = params.properties();
        propertiesParams.setFileName("mytest.properties");

        FileBasedConfigurationBuilder<PropertiesConfiguration> builder =
                new FileBasedConfigurationBuilder<PropertiesConfiguration>(PropertiesConfiguration.class)
                        .configure(propertiesParams);
        try
        {
            Configuration config = builder.getConfiguration();
            String backColor = config.getString("colors.background");
            System.out.println(backColor);
        }
        catch(ConfigurationException cex)
        {
            // loading of the configuration file failed
        }
    }

    public static void testEvent4Configuration() {
        Parameters params = new Parameters();
        PropertiesBuilderParameters propertiesParams = params.properties();
        propertiesParams.setFileName("mytest.properties");

        FileBasedConfigurationBuilder<PropertiesConfiguration> builder =
                new FileBasedConfigurationBuilder<PropertiesConfiguration>(PropertiesConfiguration.class)
                        .configure(propertiesParams);

        try {
            AbstractConfiguration config = builder.getConfiguration();
            String backColor = config.getString("colors.background");
            System.out.println(backColor);

            config.addEventListener(ConfigurationEvent.ADD_PROPERTY, new ConfigurationLogListener());

            config.addProperty("newProperty", "newValue"); // will fire an event

        } catch(ConfigurationException cex) {}
    }

    public static void testEvent4ConfigurationBuilder() {
        Parameters params = new Parameters();
        PropertiesBuilderParameters propertiesParams = params.properties();

        EventListenerParameters eventListenerParams = new EventListenerParameters();

        // fluent API
        FileBasedConfigurationBuilder<PropertiesConfiguration> builder =
                new FileBasedConfigurationBuilder<PropertiesConfiguration>(PropertiesConfiguration.class)
                        .configure(
                                propertiesParams.setFileName("mytest.properties"),
                                eventListenerParams.addEventListener(
                                        ConfigurationEvent.ANY,
                                        new ConfigurationLogListener())
                        );

        try {
            Configuration config = builder.getConfiguration();
            String backColor = config.getString("colors.background");
            System.out.println(backColor);

        } catch(ConfigurationException cex) {}
    }

    public static void testReload() {
        // Read data from this file
        File propertiesFile = new File("config.properties");

        Parameters params = new Parameters();
        FileBasedBuilderParameters fileBasedParams = params.fileBased();
        fileBasedParams.setFile(propertiesFile);

        ReloadingFileBasedConfigurationBuilder<PropertiesConfiguration> builder =
                new ReloadingFileBasedConfigurationBuilder<PropertiesConfiguration>(PropertiesConfiguration.class)
                        .configure(fileBasedParams);

        PeriodicReloadingTrigger trigger = new PeriodicReloadingTrigger(
                builder.getReloadingController(), null, 1, TimeUnit.MINUTES);

        trigger.start();
    }
}
