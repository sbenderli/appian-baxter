
package com.appian.appianbaxter;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yammer.dropwizard.config.Configuration;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * The configuration file for AppianBaxter service
 *
 * @author serdar
 */
public class AppianBaxterConfiguration extends Configuration {
    @NotEmpty
    @JsonProperty
    private String rosWsDirectory;

    public String getRosWsDirectory() {
        return rosWsDirectory;
    }

    
}
