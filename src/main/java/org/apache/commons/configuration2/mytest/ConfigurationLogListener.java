package org.apache.commons.configuration2.mytest;


import org.apache.commons.configuration2.event.ConfigurationEvent;
import org.apache.commons.configuration2.event.EventListener;

public class ConfigurationLogListener implements EventListener<ConfigurationEvent> {
    @Override
    public void onEvent(ConfigurationEvent event) {
        if (!event.isBeforeUpdate()) {
            // only display events after the modification was done
            System.out.println("Received event!");
            System.out.println("Type = " + event.getEventType());
            if (event.getPropertyName() != null) {
                System.out.println("Property name = " + event.getPropertyName());
            }
            if (event.getPropertyValue() != null) {
                System.out.println("Property value = " + event.getPropertyValue());
            }
        }
    }
}
