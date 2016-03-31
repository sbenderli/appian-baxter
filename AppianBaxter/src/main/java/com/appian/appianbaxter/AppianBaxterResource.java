package com.appian.appianbaxter;

import com.appian.appianbaxter.domainentity.Command;
import com.appian.appianbaxter.domainentity.ReturnEntity;
import com.appian.appianbaxter.domainentity.Status;
import com.google.common.base.Optional;
import com.jcraft.jsch.JSchException;
import com.yammer.metrics.annotation.Timed;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import javax.ws.rs.GET;

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
    public Status getBaxterStatus() throws IOException {
        return getStatus();
    }

    private Status getStatus() throws IOException {
        io.sendCommand("rosrun baxter_tools enable_robot.py -s");
        return Status.getStatusFromString(io.getResult());
    }

    @Path("status/enable")
    @POST
    @Timed
    public Status enable() throws IOException {
        io.sendCommand("rosrun baxter_tools enable_robot.py -e");
        io.getResult();
        return getStatus();
    }

    @Path("status/disable")
    @POST
    @Timed
    public Status disable() throws IOException {
        io.sendCommand("rosrun baxter_tools enable_robot.py -d");
        io.getResult();
        return getStatus();
    }

    @Path("status/reset")
    @POST
    @Timed
    public Status reset() throws IOException {
        io.sendCommand("rosrun baxter_tools enable_robot.py -r");
        io.getResult();
        return getStatus();
    }
//</editor-fold>

    @POST
    @Timed
    public ReturnEntity postCommand(Optional<Command> command)
            throws JSchException, IOException, InterruptedException {
        io.sendCommand(command.get());
        ReturnEntity result = new ReturnEntity();
        result.setCommandResult(io.getResult());
        return result;

    }
}
