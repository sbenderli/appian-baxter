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
    private String result;
    private Integer pid;
    
    public CommandResult() {
    
    }
    
    public CommandResult(Command command, String result, Integer pid) {
        this.sentCommand = command;
        this.result = result;
        this.pid = pid;
    }

    public Command getSentCommand() {
        return sentCommand;
    }

    public void setSentCommand(Command sentCommand) {
        this.sentCommand = sentCommand;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public Integer getPid() {
        return pid;
    }

    public void setPid(Integer pid) {
        this.pid = pid;
    }
    
}
