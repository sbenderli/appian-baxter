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
    
    private String commandName;

    public String getCommandName() {
        return commandName;
    }

    public void setCommandName(String commandName) {
        this.commandName = commandName;
    }
    
    
}
