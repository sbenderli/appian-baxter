package com.appian.appianbaxter;

import com.appian.appianbaxter.domainentity.Command;
import com.appian.appianbaxter.domainentity.Saying;
import com.google.common.base.Optional;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.yammer.metrics.annotation.Timed;
import java.util.concurrent.atomic.AtomicLong;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.ProcessBuilder.Redirect;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

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

    public AppianBaxterResource(String template, String defaultName) {
        this.template = template;
        this.defaultName = defaultName;
        this.counter = new AtomicLong();
    }

    @GET
    @Timed
    public Saying sayHello(@QueryParam("name") Optional<String> name) {
        return new Saying(counter.incrementAndGet(),
                String.format(template, name.or(defaultName)));
    }

    @POST
    @Timed
    public Command postCommand(Optional<Command> command)
            throws JSchException, IOException, InterruptedException {
        
//        JSch jsch = new JSch();
//        Session session = jsch.getSession(user, host);
//        session.setPassword(password);
//        session.setConfig("StrictHostKeyChecking", "no");
//        System.out.println("Establishing Connection...");
//        session.connect();
//        System.out.println("Connection established.");
//
//        ChannelExec channel = (ChannelExec) session.openChannel("exec");
//        BufferedReader in = new BufferedReader(new InputStreamReader(channel.getInputStream()));
//        channel.setCommand("pwd;");
//        channel.connect();
//
//        String msg = null;
//        while ((msg = in.readLine()) != null) {
//            System.out.println(msg);
//        }
//
//        channel.disconnect();
//        session.disconnect();

//        Map<String,Object> credentials = new HashMap<>();
//        credentials.put("password", "rethink");
//        RobotRaconteurNode.s().connectService(
//                "AppianBaxter.local","ruser", credentials, null);
//        RobotRaconteurNode.s().shutdown();

        String line;
        Scanner scan = new Scanner(System.in);
        File wd = new File("/home/serdar/ros_ws");
        ProcessBuilder pb = new ProcessBuilder("/bin/bash");
        pb.directory(wd);
        
        //pb.redirectInput(ProcessBuilder.Redirect.INHERIT);
        //pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);    
        
        pb.redirectError(Redirect.INHERIT);
        pb.redirectErrorStream(true);
        Process process = pb.start();
        
        OutputStream stdout = process.getOutputStream();
        InputStream stderr = process.getErrorStream();
        InputStream stdin = process.getInputStream();

        BufferedReader reader = new BufferedReader(new InputStreamReader(stdin));
        BufferedReader errorReader = new BufferedReader(new InputStreamReader(stderr));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stdout));
        

        writer.write(command.get().getCommandName()+"\n");
        writer.flush();       
        line = reader.readLine();
        while (line != null && !line.trim().equals("--EOF--")) {
            System.out.println("Stdout: " + line);
            if(!reader.ready()) break;
            
            line = reader.readLine();
            
        }
        
        
    
//        while (scan.hasNext()) {
//            String input = scan.nextLine();
//            if (input.trim().equals("exit")) {
//                // Putting 'exit' amongst the echo --EOF--s below doesn't work.
//                writer.write("exit\n");
//            } else {
//                //writer.write("((" + input + ") && echo --EOF--) || echo --EOF--\n");
//                writer.write(input+"\n");
//            }
//            writer.flush();
//
//            line = reader.readLine();
//            while (line != null && !line.trim().equals("--EOF--")) {
//                System.out.println("Stdout: " + line);
//                line = reader.readLine();
//            }
//            if (line == null) {
//                break;
//            }
//        }
        //process.waitFor();
        
        
        return command.get();
    }
}
