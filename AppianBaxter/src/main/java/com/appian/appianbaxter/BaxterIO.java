package com.appian.appianbaxter;

import com.appian.appianbaxter.domainentity.Command;
import com.appian.appianbaxter.domainentity.CommandResult;
import com.appian.appianbaxter.util.TimeoutInputStream;
import com.google.common.primitives.Ints;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStreamWriter;
import java.lang.ProcessBuilder.Redirect;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * An object that manages IO to/from baxter
 *
 * @author serdar
 */
public class BaxterIO {

    private final boolean USE_TIMEOUT_READ = true;
    private final ProcessBuilder pb;
    private Command lastSentCommand;

    private final static int READ_BUFFER = 1024;
    private final static int READ_TIMEOUT = 50000;
    private final static int READ_CLOSE_TIMEOUT = 10000;

    private Process process;
    private int parentPid;

    private BufferedReader reader;
    private BufferedWriter writer;

    private final Redirect redirectInput;
    private final Redirect redirectOutput;
    
    public static String START_FOLDER;

    public BaxterIO(ProcessBuilder pb) {
        this.pb = pb;
        redirectInput = pb.redirectInput();
        redirectOutput = pb.redirectOutput();

        initNewProcess();
    }
    
    public String getFolderPath() {
        return pb.directory().getAbsolutePath();
    }

    public Command getLastSentCommand() {
        return lastSentCommand;
    }

    private void initNewProcess() {
        try {
            this.process = pb.start();
        } catch (IOException ex) {
            throw new RuntimeException("Failed to start the process");
        }
        this.parentPid = getPidOfProcess(this.process);
        this.reader = new BufferedReader(
                new InputStreamReader(
                        USE_TIMEOUT_READ
                                ? new TimeoutInputStream(
                                        process.getInputStream(), READ_BUFFER,
                                        READ_TIMEOUT, READ_CLOSE_TIMEOUT)
                                : process.getInputStream()));
        this.writer = new BufferedWriter(
                new OutputStreamWriter(process.getOutputStream()));
    }

    public CommandResult sendCommand(Command command) {
        lastSentCommand = command;
        if (command == null || command.getCommand() == null
                || command.getCommand().isEmpty()) {
            return new CommandResult(null, null);
        }

        write(command.getCommand(), this.writer);

        return new CommandResult(command,
                redirectOutput == Redirect.INHERIT
                || !command.isWaitForResult() ? null : readResult());
    }

    public String readResult() {
        return read(this.reader);
    }

    public String killRunningProcesses() {
        StringBuilder sb = new StringBuilder("Killed following processes: ");
        List<Integer> subProcesPids = new ArrayList<>();

        //Start a new process to get sub pids and kill them
        //We can't do this from main process because it might be
        //running a process currently
        try {
            Process tempProcess = pb.start();
            BufferedReader debugReader = new BufferedReader(
                    new InputStreamReader(tempProcess.getInputStream()));
            BufferedWriter debugWriter = new BufferedWriter(
                    new OutputStreamWriter(tempProcess.getOutputStream()));

            //send the command that will get sub pids
            //returned string will look like this:
            //PID
            //1234
            //5678
            //7890
            String[] tokens = writeAndRead(
                    String.format("ps --ppid %s | awk '{ print $1 }'",
                            this.parentPid), debugWriter, debugReader)
                    .split(System.getProperty("line.separator"));
            for (String token : tokens) {
                Integer temp = Ints.tryParse(token);
                if (temp != null) {
                    subProcesPids.add(temp);
                }
            }

            //Kill each process
            for (Integer pid : subProcesPids) {
                String pidName = writeAndRead(
                        String.format("ps -p %s -o command=", pid),
                        debugWriter, debugReader).trim();
                sb.append(System.getProperty("line.separator"))
                        .append(String.format("%s (%s)", pidName, pid));

                write(String.format("kill -int %s", pid),
                        debugWriter);
            }

            //destroy the temp process
            tempProcess.destroy();

            return sb.toString();
        } catch (IOException ex) {
            return "Something went wrong. Partially " + sb.toString();
        }
    }

    public boolean restartProcess() {
        killRunningProcesses();
        process.destroy();
        initNewProcess();
        return true;
    }

    //<editor-fold defaultstate="collapsed" desc="Private methods">
    private String writeAndRead(String command,
            BufferedWriter bufferedWriter, BufferedReader bufferedReader) {
        write(command, bufferedWriter);
        return read(bufferedReader);
    }

    private void write(String command,
            BufferedWriter bufferedWriter) {
        try {
            bufferedWriter.write(command + System.getProperty("line.separator"));
            bufferedWriter.flush();
        } catch (IOException ex) {
            throw new RuntimeException("Failed to write to process");
        }
    }

    private String read(BufferedReader bufferedReader) {
        String line;
        StringBuilder sb = new StringBuilder();

        try {
            do {
                line = bufferedReader.readLine();
                sb.append(System.getProperty("line.separator")).append(line);
                System.out.println("Stdout: " + line);
            } while (bufferedReader.ready() && line != null);
        } catch (InterruptedIOException e) {
            //Do something
            sb.append("---Read timed eout---");
        } catch (IOException e) {
            sb.append("IOException occurred: ").append(e.getMessage());
            //TODO: restart process?
            restartProcess();
        }
        return sb.toString();
    }

    private static int getPidOfProcess(Process p) {
        int pid = -1;

        try {
            if (p.getClass().getName().equals("java.lang.UNIXProcess")) {
                Field f = p.getClass().getDeclaredField("pid");
                f.setAccessible(true);
                pid = f.getInt(p);
                f.setAccessible(false);
            }
        } catch (Exception e) {
            pid = -1;
        }
        return pid;
    }
//</editor-fold>

}
