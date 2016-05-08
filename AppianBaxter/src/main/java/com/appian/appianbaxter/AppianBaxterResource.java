package com.appian.appianbaxter;

import com.appian.appianbaxter.domainentity.Camera;
import com.appian.appianbaxter.domainentity.CameraStatus;
import com.appian.appianbaxter.domainentity.Command;
import com.appian.appianbaxter.domainentity.CommandResult;
import com.appian.appianbaxter.domainentity.SensorReading;
import com.appian.appianbaxter.domainentity.SensorType;
import com.appian.appianbaxter.domainentity.Status;
import com.yammer.metrics.annotation.Timed;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
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
    private static boolean recordingOngoing = false;
    private static Integer recordingPid = null;

    public AppianBaxterResource(BaxterIO io) {
        this.io = io;
        
        initBaxter();
    }

    //<editor-fold defaultstate="collapsed" desc="Status">
    @Path("status")
    @GET
    @Timed
    public Response getBaxterStatus() {
         return Response.ok(getStatus()).build();
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
    
    //<editor-fold defaultstate="collapsed" desc="Titration">
    private String startPicture = "startpic";
    private String initialize = "initialize";
    private String drip = "drip";
    private String stopPicture = "stoppic";
    private String end = "end";
    private String wave = "wave";
    @Path("titration/startpicture")
    @POST
    @Timed
    public Response startPicture() {
        Command command  = new Command(
            "rosrun baxter_examples xdisplay_image.py --file=baxter1.jpg",
            true);
        io.sendCommand(command);
        command = new Command(
                "rosrun baxter_examples baxter_titrate.py -s " + startPicture, true, -1);
        return Response.ok(io.sendCommand(command)).build();
    }
    
    @Path("titration/initialize")
    @POST
    @Timed
    public Response initialize() {
      Command command  = new Command(
            "rosrun baxter_examples xdisplay_image.py --file=baxter2.jpg",
            true);
        io.sendCommand(command);
        
        command = new Command(
                "rosrun baxter_examples baxter_titrate.py -s " + initialize, true, -1);
        return Response.ok(io.sendCommand(command)).build();
    }
    
    @Path("titration/drip")
    @POST
    @Timed
    public Response drip() {
        Command command  = new Command(
            "rosrun baxter_examples xdisplay_image.py --file=baxter3.jpg",
            true);
        io.sendCommand(command);
        
      command = new Command(
                "rosrun baxter_examples baxter_titrate.py -s " + drip, true, -1);
        return Response.ok(io.sendCommand(command)).build();
    }
    
    @Path("titration/stoppicture")
    @POST
    @Timed
    public Response stopPicture() {
        Command command  = new Command(
            "rosrun baxter_examples xdisplay_image.py --file=baxter4.jpg",
            true);
        io.sendCommand(command);
        
        command = new Command(
                "rosrun baxter_examples baxter_titrate.py -s " + stopPicture, true, -1);
        return Response.ok(io.sendCommand(command)).build();
    }
    
    @Path("titration/end")
    @POST
    @Timed
    public Response end() {
        Command command  = new Command(
            "rosrun baxter_examples xdisplay_image.py --file=baxter5.jpg",
            true);
        io.sendCommand(command);
        
        command = new Command(
                "rosrun baxter_examples baxter_titrate.py -s " + end, true, -1);
        CommandResult result = io.sendCommand(command);
        
        //reset the image on baxter's face
        command = new Command(
            "rosrun baxter_examples xdisplay_image.py --file=baxter_welcome.jpg",
            true);
        io.sendCommand(command);
        return Response.ok(result).build();
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

    @Path("io/read/{pid}")
    @GET
    @Timed
    public Response read(@PathParam("pid") Integer pid) {
        CommandResult result = io.readResult(pid);
        return Response.ok(result).build();
    }
    
    @Path("io/killall")
    @POST
    @Timed
    public Response killAllProcesses() {
        return Response
                .ok(io.killAllProcesses())
                .build();
    }


    @Path("io/kill/{pid}")
    @POST
    @Timed
    public Response killProcess(@PathParam("pid") Integer pid) {
        //If this is the recording PID, clear it out so we can allow new recordings
        if (pid.equals(recordingPid)) {
            recordingPid = null;
        }
        
        
        CommandResult result = new CommandResult(
                io.getLastSentCommand(pid),
                io.killProcessAndItsChildren(pid),
                pid);
        result.setSuccess(true);
        return Response.ok(result).build();
    }
    

    @GET
    @Path("/sensor/ph")
    @Timed
    public Response getPh() throws IOException {
        CommandResult result = io.sendCommand(
                new Command("./GoIO_DeviceCheck", true));
        return Response.ok(getPhFromResult(result)).build();
    }
    

    @GET
    @Path("/image/test")
    @Produces("image/jpeg")
    @Timed
    public Response getTestImage() throws IOException {
        BufferedImage image = ImageIO.read(
                new File("/home/serdar/Downloads/trump.jpg"));

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

    //For now, head camera is not available
//    @GET
//    @Path("/camera/head")
//    @Produces("image/jpeg")
//    @Timed
//    public Response getImageFromHeadCamera()
//            throws InterruptedException, IOException {
//        return Response.ok(getImageFromCamera(Camera.HEAD)).build();
//    }
    
    @POST
    @Path("/record")
    @Timed
    public Response record() throws IOException, InterruptedException {
        if (recordingPid != null) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("Another recording in progress. Please kill: " + recordingPid)
                    .build();
        }
        //io.sendCommand(new Command("rm recording.txt", true));
        CommandResult result = io.sendCommand(
                new Command("rosrun baxter_examples joint_recorder.py -f recording.txt", false));
        Thread.sleep(2000);
        
        
        recordingPid = result.getPid();
        return Response.ok(recordingPid).build();
    }
    @POST
    @Path("/playback")
    @Timed
    public Response playback() throws IOException {
        CommandResult result = io.sendCommand(
                new Command("rosrun baxter_examples joint_trajectory_file_playback.py -f recording.txt", true, 60));
        return Response.ok(result).build();
    }
    @POST
    @Path("/wave")
    @Timed
    public Response wave() throws IOException {
        Command command = new Command(
                "rosrun baxter_examples baxter_titrate.py -s " + wave, true, -1);
        return Response.ok(io.sendCommand(command)).build();
    }

    private Status getStatus() {
        CommandResult result = io.sendCommand(
                new Command("rosrun baxter_tools enable_robot.py -s"));
        return Status.getStatusFromString(result.getMessage());
    }

    private byte[] getImageFromCamera(Camera camera)
            throws IOException, InterruptedException {
        DateTime now = DateTime.now();

        //is camera enabled?
        CommandResult result = io.sendCommand(
                new Command("rosrun baxter_tools camera_control.py -l", true));

        CameraStatus cameraStatus = CameraStatus.getStatusFromString(
                result.getMessage());
        if (!cameraStatus.isCameraOpen(camera)) {
            //Send command and wait for result
            result = io.sendCommand(
                    new Command(getEnableCameraCommand(), true));
        }

        //Make a folder for this request and start capturing images
        String imageFolderPath = String.format("%s/images/%s",
                io.getFolderPath(), now.getMillis());
        File imageFolder = FileUtils.makeDirectory(imageFolderPath);
        if (imageFolder == null) {
            throw new RuntimeException("Could not create directory.");
        }

        //start taking pictures
        String command = String.format("cd %s "
                + "&& rosrun image_view extract_images"
                + " image:=/cameras/%s/image _sec_per_frame:=0.1",
                imageFolderPath, camera);
        result = io.sendCommand(new Command(command, false));
        //wait until a picture is taken
        File image = null;
        int count = 0;
        while (image == null && count < 100) {
            Thread.sleep(100);
            image = FileUtils.getLastImage(imageFolder);
            count++;
        }

        //destroy the image process
        io.killProcessAndItsChildren(result.getPid());

        byte[] imageData = 
                image == null ? null : FileUtils.getImageDataToSend(image);
        FileUtils.deleteDirectory(imageFolder);
        return imageData;
    }

    private String getEnableCameraCommand() {
        //Currently only left and right camera are supported.
        //So, close head cam and turn them both on
        String setupCameras = String.format(
                "rosrun baxter_tools camera_control.py -c %s "
                + "&& rosrun baxter_tools camera_control.py -o %s -r 1280x800"
                + "&& rosrun baxter_tools camera_control.py -o %s -r 1280x800"
                + "&& rosrun baxter_tools camera_control.py -l",
                Camera.HEAD, Camera.LEFT, Camera.RIGHT);
        return setupCameras;
    }

    //Run this method to init Baxter...
    private void initBaxter() {
        //enable baxter
        CommandResult result = io.sendCommand(
                new Command("rosrun baxter_tools enable_robot.py -e", true));
        
        //enable cameras
        result = io.sendCommand(new Command(getEnableCameraCommand(), true));
        
        //start the trajectory server
        Command cmd = new Command(
                "rosrun baxter_interface joint_trajectory_action_server.py",
                false);
        result = io.sendCommand(cmd);
        
        //Put welcome to Appian on screen
        cmd = new Command(
            "rosrun baxter_examples xdisplay_image.py --file=baxter_welcome.jpg",
            true);
        io.sendCommand(cmd);
        
        cmd = new Command(
            "rosrun baxter_examples gripper_cuff_control.py", false);
        result = io.sendCommand(cmd);
    }

    private static double getPhFromResult(CommandResult result) {
        double ph = 0;
        String[] tokens = result.getMessage().split(System.lineSeparator());
        //last token is the average reading
        Pattern p = Pattern.compile("(\\d+(?:\\.\\d+)?)");
        Matcher m = p.matcher(tokens[tokens.length - 1]);
        while (m.find()) {
            String phToken = m.group();
            ph = Double.parseDouble(phToken);
            break;
        }
        return ph;
    }
}
