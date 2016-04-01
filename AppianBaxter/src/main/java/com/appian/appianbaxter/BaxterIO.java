/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An object that manages IO to/from baxter
 *
 * @author serdar
 */
public class BaxterIO {

    private final boolean USE_TIMEOUT_READ = true;
    private final ProcessBuilder pb;

    private final static int READ_BUFFER = 1024;
    private final static int READ_TIMEOUT = 50000;
    private final static int PROCESS_CLOSE_TIMEOUT = 10000;
    private final static int READ_CLOSE_TIMEOUT = 10000;

    private Process process;

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

    private void initNewProcess() {
        try {
            this.process = pb.start();
        } catch (IOException ex) {
            throw new RuntimeException("Failed to start the process");
        }

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

    public CommandResult sendCommand(String command) {
        if (command == null || command.isEmpty()) {
            return new CommandResult(null, null);
        }

        try {
            writer.write(command + "\n");
            writer.flush();
        } catch (IOException ex) {
            throw new RuntimeException("Failed to write to process");
        }

        Command commandObject = new Command();
        commandObject.setCommand(command);
        return new CommandResult(commandObject,
                redirectOutput == Redirect.INHERIT ? null : readResult());
    }

    public CommandResult sendCommand(Command command) {
        return sendCommand(command == null ? null : command.getCommand());
    }

    private String readResult() {
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
            sb.append("\n---Read timed out---");
        } catch (IOException e) {
            sb.append("IOException occurred: ").append(e.getMessage());
            //TODO: restart process?
            restartProcess();
        }
        return sb.toString();
    }

    private boolean restartProcess() {
        try {
            process.destroyForcibly().waitFor(
                    PROCESS_CLOSE_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ex) {
            //TODO: what to do here?
            return false;
        }

        initNewProcess();
        return true;
    }

}
