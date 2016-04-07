/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.appian.appianbaxter.domainentity;

/**
 *
 * @author serdar
 */
public class CommandResult {

    private Command sentCommand;
    private boolean success;
    private String message;
    private Integer pid;
    
    public CommandResult() {
    
    }
    
    public CommandResult(Command command, 
            String result, Integer pid) {
        this.success = success;
        this.sentCommand = command;
        this.message = result;
        this.pid = pid;
    }

    public Command getSentCommand() {
        return sentCommand;
    }

    public void setSentCommand(Command sentCommand) {
        this.sentCommand = sentCommand;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getPid() {
        return pid;
    }

    public void setPid(Integer pid) {
        this.pid = pid;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    
}
