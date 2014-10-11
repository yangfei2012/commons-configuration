package org.apache.commons.configuration2;

import org.apache.commons.configuration2.builder.BasicConfigurationBuilder;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.builder.fluent.XMLBuilderParameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.tree.xpath.XPathExpressionEngine;


public class MyTest {

    public static void main(String[] args) {

        Parameters params = new Parameters();
        FileBasedConfigurationBuilder<PropertiesConfiguration> builder =
                new FileBasedConfigurationBuilder<PropertiesConfiguration>(PropertiesConfiguration.class)
                        .configure(params.properties()
                                .setFileName("mytest.properties"));
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


        System.out.println("xxxxxxxxx");
    }
}
