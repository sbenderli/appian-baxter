package com.appian.appianbaxter;

import com.appian.appianbaxter.domainentity.Command;
import com.appian.appianbaxter.domainentity.ReturnEntity;
import com.google.common.base.Optional;
import com.jcraft.jsch.JSchException;
import com.yammer.metrics.annotation.Timed;
import java.util.concurrent.atomic.AtomicLong;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 *
 * @author serdar
 */
@Path("/appian-baxter")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AppianBaxterResource {

    private final String template;
    private final String defaultName;
    private final AtomicLong counter;

    private final String password = "rethink";
    private final String user = "ruser";
    private final String host = "169.254.10.131";

    private final ProcessBuilder pb;
    
    public AppianBaxterResource(
            ProcessBuilder pb,
            String template, String defaultName) {
        this.pb = pb;
        this.template = template;
        this.defaultName = defaultName;
        this.counter = new AtomicLong();
    }

//    @Path("status")
//    @GET
//    @Timed
//    public Status getStatus() {
//        
//    }

    @POST
    @Timed
    public ReturnEntity postCommand(Optional<Command> command)
            throws JSchException, IOException, InterruptedException {
        String line;
        StringBuilder sb = new StringBuilder();
        Process process = pb.start();
        
        OutputStream stdout = process.getOutputStream();
        InputStream stdin = process.getInputStream();

        BufferedReader reader = new BufferedReader(new InputStreamReader(stdin));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stdout));

        writer.write(command.get().getCommand()+"\n");
        writer.flush();       
        do {
            line = reader.readLine();
            sb.append(line).append("\n");
            System.out.println("Stdout: " + line);
        } while(reader.ready() && line != null && !line.trim().equals("--EOF--"));
        
        reader.close();
        writer.close();
        process.destroy();

        
        ReturnEntity result = new ReturnEntity();
        result.setCommandResult(sb.toString());
        return result;
    }
}
