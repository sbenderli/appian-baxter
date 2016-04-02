
package com.appian.appianbaxter;

import com.appian.appianbaxter.domainentity.Command;
import com.appian.appianbaxter.domainentity.CommandResult;
import com.appian.appianbaxter.util.TimeoutInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStreamWriter;
import java.lang.ProcessBuilder.Redirect;
import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import sun.misc.Signal;

/**
 * An object that manages IO to/from baxter
 *
 * @author serdar
 */
public class BaxterIO {

    private Command lastSentCommand;
    private final boolean USE_TIMEOUT_READ = true;
    private final ProcessBuilder pb;

    private final static int READ_BUFFER = 1024;
    private final static int READ_TIMEOUT = 50000;
    private final static int PROCESS_CLOSE_TIMEOUT = 10000;
    private final static int READ_CLOSE_TIMEOUT = 10000;

    private Process process;
    private long pid;

    private BufferedReader reader;
    private BufferedWriter writer;

    private final Redirect redirectInput;
    private final Redirect redirectOutput;

    public BaxterIO(ProcessBuilder pb) {
        this.pb = pb;
        redirectInput = pb.redirectInput();
        redirectOutput = pb.redirectOutput();

        initNewProcess();
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
        this.pid = getPidOfProcess(this.process);
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

        try {
            writer.write(command.getCommand() + "\n");
            writer.flush();
        } catch (IOException ex) {
            throw new RuntimeException("Failed to write to process");
        }

        return new CommandResult(command,
                redirectOutput == Redirect.INHERIT
                || !command.isWaitForResult() ? null : readResult());
    }

    public String readResult() {
        String line;
        StringBuilder sb = new StringBuilder();

        try {
            do {
                line = reader.readLine();
                sb.append(line).append("\n");
                System.out.println("Stdout: " + line);
            } while (reader.ready() && line != null);
        } catch (InterruptedIOException e) {
            //Do something
            sb.append("---Read timed out---");
        } catch (IOException e) {
            sb.append("IOException occurred: ").append(e.getMessage());
            //TODO: restart process?
            restartProcess();
        }
        return sb.toString();
    }
    
    public boolean killRunningProcess() {
        try {
            Runtime.getRuntime().exec("kill -SIGINT " + Long.toString(this.pid));
        } catch (IOException ex) {
            return false;
        }
        return true;
    }

    public boolean restartProcess() {
        boolean status = killRunningProcess();
        try {
            process.destroyForcibly().waitFor(
                    PROCESS_CLOSE_TIMEOUT, TimeUnit.MILLISECONDS);
            //0 means successfully terminated
            status &= process.exitValue() == 0;
        } catch (InterruptedException ex) {
            //TODO: what to do here?
            return false;
        }

        initNewProcess();
        return status;
    }

    public static long getPidOfProcess(Process p) {
        long pid = -1;

        try {
            if (p.getClass().getName().equals("java.lang.UNIXProcess")) {
                Field f = p.getClass().getDeclaredField("pid");
                f.setAccessible(true);
                pid = f.getLong(p);
                f.setAccessible(false);
            }
        } catch (Exception e) {
            pid = -1;
        }
        return pid;
    }

}
