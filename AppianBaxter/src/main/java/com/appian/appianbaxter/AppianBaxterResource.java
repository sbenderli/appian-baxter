package com.appian.appianbaxter;

import com.appian.appianbaxter.domainentity.Camera;
import com.appian.appianbaxter.domainentity.CameraStatus;
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
import org.joda.time.DateTime;

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
    
    @Path("io/clearbuffer")
    @POST
    @Timed
    public Response clearBuffer() {
        return Response
                .ok("Cleared buffer. Contents were: " + io.readResult())
                .build();
    }

    @Path("io/killall")
    @POST
    @Timed
    public Response terminateProcess() {
        CommandResult result = new CommandResult(io.getLastSentCommand(),
                io.killRunningProcesses());
        return Response.ok(result).build();
    }

    @GET
    @Path("/image/test")
    @Produces("image/jpeg")
    @Timed
    public Response getTestImage() throws IOException {
        BufferedImage image = ImageIO.read(new File("/home/serdar/Downloads/trump.jpg"));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpeg", baos);
        byte[] imageData = baos.toByteArray();

        // uncomment line below to send non-streamed
        return Response.ok(imageData).build();

        // uncomment line below to send streamed
        // return Response.ok(new ByteArrayInputStream(imageData)).build();
    }

    @GET
    @Path("/camera/left")
    @Produces("image/jpeg")
    @Timed
    public Response getImageFromLeftCamera()
            throws InterruptedException, IOException {
        return Response.ok(getImageFromCamera(Camera.LEFT)).build();
    }

    @GET
    @Path("/camera/right")
    @Produces("image/jpeg")
    @Timed
    public Response getImageFromRightCamera()
            throws InterruptedException, IOException {
        return Response.ok(getImageFromCamera(Camera.RIGHT)).build();
    }

    @GET
    @Path("/camera/head")
    @Produces("image/jpeg")
    @Timed
    public Response getImageFromHeadCamera()
            throws InterruptedException, IOException {
        return Response.ok(getImageFromCamera(Camera.HEAD)).build();
    }

    private byte[] getImageFromCamera(Camera camera)
            throws IOException, InterruptedException {
        DateTime now = DateTime.now();

        //is camera enabled?
        String statusString = io.sendCommand(
                new Command("rosrun baxter_tools camera_control.py -l", true))
                .getResult();

        CameraStatus cameraStatus = CameraStatus.getStatusFromString(statusString);
        if (!cameraStatus.isCameraOpen(camera)) {
            //Send command and wait for result
            CommandResult result = io.sendCommand(
                    new Command(getEnableCameraCommand(camera), true));
        }

        //Make a folder for this request and start capturing images
        String imageFolderPath = String.format("%s/images/%s",
                io.getFolderPath(), now.getMillis());
        String command = String.format("cd %s "
                + "&& rosrun image_view extract_images"
                + " image:=/cameras/%s/image _sec_per_frame:=0.1",
                imageFolderPath, Camera.LEFT);

        File imageFolder = FileUtils.makeDirectory(imageFolderPath);
        if (imageFolder == null) {
            throw new RuntimeException("Could not create directory.");
        }

        //start taking pictures
        io.sendCommand(new Command(command, false));
        //wait until a picture is taken
        File image = FileUtils.getLastImage(imageFolder);
        while (image == null) {
            Thread.sleep(100);
            image = FileUtils.getLastImage(imageFolder);
        }
        io.killRunningProcesses();

        return FileUtils.getImageDataToSend(image);
    }

    private String getEnableCameraCommand(Camera camera) {
        //Open this camera and close the other two
        String setupCameras = "rosrun baxter_tools camera_control.py -c %s "
                + "&& rosrun baxter_tools camera_control.py -c %s "
                + "&& rosrun baxter_tools camera_control.py -o %s -r 1280x800"
                + "&& rosrun baxter_tools camera_control.py -l";
        switch (camera) {
            case LEFT:
                setupCameras = String.format(setupCameras,
                        Camera.HEAD, Camera.RIGHT, Camera.LEFT);
                break;
            case RIGHT:
                setupCameras = String.format(setupCameras,
                        Camera.HEAD, Camera.LEFT, Camera.RIGHT);
                break;
            case HEAD:
                setupCameras = String.format(setupCameras,
                        Camera.LEFT, Camera.RIGHT, Camera.HEAD);
                break;
            default:
                throw new AssertionError(camera.name());
        }
        return setupCameras;
    }

}
