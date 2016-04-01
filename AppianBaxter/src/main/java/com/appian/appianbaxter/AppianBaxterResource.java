package com.appian.appianbaxter;

import com.appian.appianbaxter.domainentity.Command;
import com.appian.appianbaxter.domainentity.CommandResult;
import com.appian.appianbaxter.domainentity.Status;
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
    public Status getBaxterStatus() {
        return getStatus();
    }

    private Status getStatus() {
        CommandResult result = io.sendCommand(
                "rosrun baxter_tools enable_robot.py -s");
        return Status.getStatusFromString(result.getResult());
    }

    @Path("status/enable")
    @POST
    @Timed
    public Status enable() {
        CommandResult result = io.sendCommand(
                "rosrun baxter_tools enable_robot.py -e");
        return getStatus();
    }

    @Path("status/disable")
    @POST
    @Timed
    public Status disable() {
        CommandResult result = io.sendCommand(
                "rosrun baxter_tools enable_robot.py -d");
        return getStatus();
    }

    @Path("status/reset")
    @POST
    @Timed
    public Status reset() {
        CommandResult result = io.sendCommand(
                "rosrun baxter_tools enable_robot.py -r");
        return getStatus();
    }
//</editor-fold>

    @POST
    @Timed
    public CommandResult postCommand(Command command) {
        return io.sendCommand(command);
    }
}
