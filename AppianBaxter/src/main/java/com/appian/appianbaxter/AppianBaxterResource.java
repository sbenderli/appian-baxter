package com.appian.appianbaxter;

import com.appian.appianbaxter.domainentity.Command;
import com.appian.appianbaxter.domainentity.CommandResult;
import com.appian.appianbaxter.domainentity.Status;
import com.yammer.metrics.annotation.Timed;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.GET;
import javax.ws.rs.core.Response;

/**
 *
 * @author serdar
 */
@Path("/appian-baxter")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AppianBaxterResource {

    private final BaxterIO io;

    public AppianBaxterResource(BaxterIO io) {
        this.io = io;
    }

    //<editor-fold defaultstate="collapsed" desc="Status">
    @Path("status")
    @GET
    @Timed
    public Response getBaxterStatus() {
        return Response.ok(getStatus()).build();
    }

    private Status getStatus() {
        CommandResult result = io.sendCommand(
                new Command("rosrun baxter_tools enable_robot.py -s"));
        return Status.getStatusFromString(result.getResult());
    }

    @Path("status/enable")
    @POST
    @Timed
    public Response enable() {
        CommandResult result = io.sendCommand(
                new Command("rosrun baxter_tools enable_robot.py -e"));
        return Response.ok(getStatus()).build();
    }

    @Path("status/disable")
    @POST
    @Timed
    public Response disable() {
        CommandResult result = io.sendCommand(
                new Command("rosrun baxter_tools enable_robot.py -d"));
        return Response.ok(getStatus()).build();
    }

    @Path("status/reset")
    @POST
    @Timed
    public Response reset() {
        CommandResult result = io.sendCommand(
                new Command("rosrun baxter_tools enable_robot.py -r"));
        return Response.ok(getStatus()).build();
    }
//</editor-fold>

    @POST
    @Timed
    public Response postCommand(Command command) {
        return Response.ok(io.sendCommand(command)).build();
    }

    @Path("io/write")
    @POST
    @Timed
    public Response write(Command command) {
        return Response.ok(io.sendCommand(command)).build();
    }

    @Path("io/read")
    @POST
    @Timed
    public Response read() {
        CommandResult result = new CommandResult(io.getLastSentCommand(),
                io.readResult());
        return Response.ok(getStatus()).build();
    }

    @Path("io/terminate")
    @POST
    @Timed
    public Response terminateProcess() {
        CommandResult result = new CommandResult(io.getLastSentCommand(),
                io.restartProcess() ? "Terminated" : "Not Terminated");
        return Response.ok(result).build();
    }

    @GET
    @Path("/image")
    @Produces("image/jpeg")
    @Timed
    public Response getFullImage() throws IOException {
        BufferedImage image = ImageIO.read(new File("/home/serdar/Downloads/trump.jpg"));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpeg", baos);
        byte[] imageData = baos.toByteArray();

        // uncomment line below to send non-streamed
        return Response.ok(imageData).build();

        // uncomment line below to send streamed
        // return Response.ok(new ByteArrayInputStream(imageData)).build();
    }
}
