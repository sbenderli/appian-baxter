
package com.appian.appianbaxter;

import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;

/**
 * AppianBaxter Service file - Initialization of resources, datasources, etc.
 *
 * @author serdar
 */
public class AppianBaxterService extends Service<AppianBaxterConfiguration> {
    public static void main(String[] args) throws Exception {
        if(args.length == 0) {
            args = new String[2];
            args[0]="server";
            args[1]="configs/appian-baxter.yml";
        }
        new AppianBaxterService().run(args);
    }

    @Override
    public void initialize(Bootstrap<AppianBaxterConfiguration> bootstrap) {
        bootstrap.setName("appian-baxter");
    }

    @Override
    public void run(AppianBaxterConfiguration configuration,
                    Environment environment) {
        final String template = configuration.getTemplate();
        final String defaultName = configuration.getDefaultName();
        environment.addResource(new AppianBaxterResource(template, defaultName));
        environment.addHealthCheck(new TemplateHealthCheck(template));
    }
}
