/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.appian.appianbaxter.domainentity;

/**
 * An object that represents a command to be sent to baxter
 *
 * @author serdar
 */
public class Command {
    
    private String command;
    private boolean waitForResult = true;
    
    //Read timeout in seconds
    private int readTimeout = 10; //10sec default readtimeout
    
    public Command() {
    }
     
    public Command(String command) {
        this.command = command;
    }
    
    public Command(String command, boolean waitForResult) {
        this.command = command;
        this.waitForResult = waitForResult;
    }
    
    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public boolean isWaitForResult() {
        return waitForResult;
    }

    public void setWaitForResult(boolean waitForResult) {
        this.waitForResult = waitForResult;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }
    
    
    
}
