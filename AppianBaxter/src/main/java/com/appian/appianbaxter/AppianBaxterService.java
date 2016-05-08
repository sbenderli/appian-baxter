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
    private BaxterIO baxterIO;
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            args = new String[2];
            args[0] = "server";
            args[1] = "configs/dev.yml";
        }
        AppianBaxterService service = new AppianBaxterService();
        service.attachShutDownHook();
        service.run(args);
    }

    @Override
    public void initialize(Bootstrap<AppianBaxterConfiguration> bootstrap) {
        bootstrap.setName("appian-baxter-service");
    }

    @Override
    public void run(AppianBaxterConfiguration configuration,
            Environment environment) throws IOException {
        
        baxterIO = getBaxterIO(configuration);
        environment.addResource(
                new AppianBaxterResource(baxterIO));
        environment.addHealthCheck(new TemplateHealthCheck());
    }

    private BaxterIO getBaxterIO(AppianBaxterConfiguration configuration)
            throws IOException {
        File rosDir = new File(configuration.getRosWsDirectory());
        ProcessBuilder pb = new ProcessBuilder("/bin/bash");
        pb.directory(rosDir);
        pb.redirectErrorStream(true);

        //Use for debugging - this will make the output appear in the console
        //only
        //pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        return new BaxterIO(pb);
    }

    public void attachShutDownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                baxterIO.killAllProcesses();
                System.out.println("Killed all processes");
            }
        });
        System.out.println("Shut Down Hook Attached.");
    }
}
