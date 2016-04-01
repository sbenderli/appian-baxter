/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.appian.appianbaxter;

import com.appian.appianbaxter.domainentity.Command;
import com.appian.appianbaxter.domainentity.CommandResult;
import com.appian.appianbaxter.domainentity.Status;
import com.appian.appianbaxter.util.TimeoutInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.ProcessBuilder.Redirect;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An object that manages IO to/from baxter
 *
 * @author serdar
 */
public class BaxterIO {

    private final static int READ_BUFFER = 1024;
    private final static int READ_TIMEOUT = 10000;
    private final static int CLOSE_TIMEOUT = 10000;

    private final Process process;

    private final BufferedReader reader;
    private final BufferedWriter writer;

    private final Redirect redirectInput;
    private final Redirect redirectOutput;

    public BaxterIO(ProcessBuilder pb) throws IOException {
        this.process = pb.start();
        this.reader = new BufferedReader(
                new InputStreamReader(new TimeoutInputStream(
                        process.getInputStream(), READ_BUFFER,
                        READ_TIMEOUT, CLOSE_TIMEOUT)));
        this.writer = new BufferedWriter(
                new OutputStreamWriter(process.getOutputStream()));

        redirectInput = pb.redirectInput();
        redirectOutput = pb.redirectOutput();
    }

    public CommandResult sendCommand(String command) throws IOException {
        if (command == null || command.isEmpty()) {
            return new CommandResult(null, null);
        }
        writer.write(command + "\n");
        writer.flush();

        Command commandObject = new Command();
        commandObject.setCommand(command);
        return new CommandResult(commandObject,
                redirectOutput == Redirect.INHERIT ? null : getResult());
    }

    public CommandResult sendCommand(Command command) throws IOException {
        return sendCommand(command == null ? null : command.getCommand());
    }

    public String getResult() {
        String line;
        StringBuilder sb = new StringBuilder();

        try {
            do {
                line = reader.readLine();
                sb.append(line).append("\n");
                System.out.println("Stdout: " + line);
            } while (reader.ready() && line != null);
        } catch (IOException e) {
            //Do something
            sb.append("IO Exception Occurred").append("\n");
        }
        return sb.toString();
    }

}
