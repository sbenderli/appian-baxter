/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.appian.appianbaxter;

import com.appian.appianbaxter.domainentity.Command;
import com.appian.appianbaxter.domainentity.CommandResult;
import com.appian.appianbaxter.domainentity.Status;
import java.io.BufferedReader;
import java.io.BufferedWriter;
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

    private final Process process;
    
    private final BufferedReader errorReader;
    private final BufferedReader reader;
    private final BufferedWriter writer;

    private final Redirect redirectInput;
    private final Redirect redirectOutput;
    
    
    public BaxterIO(ProcessBuilder pb) throws IOException {
        this.process = pb.start();
        this.reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));
        this.writer = new BufferedWriter(
                new OutputStreamWriter(process.getOutputStream()));
        this.errorReader = new BufferedReader(
                new InputStreamReader(process.getErrorStream()));
        
        
        redirectInput = pb.redirectInput();
        redirectOutput = pb.redirectOutput();
    }
    
    public CommandResult sendCommand(String command) throws IOException {
        if (command == null || command.isEmpty()) {
            return new CommandResult(null,null);
        }
        writer.write(command+"\n");
        writer.flush(); 
        
        Command commandObject = new Command();
        commandObject.setCommand(command);
        return new CommandResult(commandObject, getResult());
    }
    
    
    public CommandResult sendCommand(Command command) throws IOException {
        if (command == null) {
            return new CommandResult(null,null);
        }
        writer.write(command.getCommand()+"\n");
        writer.flush();
                
        //If we're redirecting our output to the main console, we can't collect
        //a result, so skip getting the result in that case and return null
        return new CommandResult(command, 
            redirectOutput == Redirect.INHERIT ? null : getResult());
    }
    
    public String getResult() throws IOException {
        String line;
        StringBuilder sb = new StringBuilder();
        do {
            line = reader.readLine();
            sb.append(line).append("\n");
            System.out.println("Stdout: " + line);
        } while(reader.ready() && line != null && !line.trim().equals("--EOF--"));
        return sb.toString();
    }

    public String getErrors() throws IOException {
        String line;
        StringBuilder sb = new StringBuilder();
        do {
            if (!errorReader.ready()) break;
            line = errorReader.readLine();
            sb.append(line).append("\n");
            System.out.println("Stdout: " + line);
        } while(errorReader.ready() && line != null && !line.trim().equals("--EOF--"));
        return sb.toString();
    }
    
}
