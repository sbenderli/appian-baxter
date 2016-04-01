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
    
    public CommandResult(Command command, String result) {
        this.sentCommand = command;
        this.result = result;
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

}
