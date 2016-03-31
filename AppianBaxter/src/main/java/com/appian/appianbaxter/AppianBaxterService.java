
package com.appian.appianbaxter;

import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;
import java.io.File;
import java.io.IOException;

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
                    Environment environment) throws IOException {
        File rosDir = new File(configuration.getRosWsDirectory());
        ProcessBuilder pb = new ProcessBuilder("/bin/bash");
        pb.directory(rosDir);   
        pb.redirectError(ProcessBuilder.Redirect.INHERIT);
        pb.redirectErrorStream(true);
        Process process = pb.start();
        BaxterIO io = new BaxterIO(process);
        environment.addResource(new AppianBaxterResource(io));
        environment.addHealthCheck(new TemplateHealthCheck());
    }
}
