/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.appian.appianbaxter.domainentity;

/**
 * An object representing the status returned by baxter
 *
 * @author serdar
 */
public class Status {

    private boolean ready;
    private boolean enabled;
    private boolean stopped;
    private int estopButton;
    private int estopSource;

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isStopped() {
        return stopped;
    }

    public void setStopped(boolean stopped) {
        this.stopped = stopped;
    }

    public int getEstopButton() {
        return estopButton;
    }

    public void setEstopButton(int estopButton) {
        this.estopButton = estopButton;
    }

    public int getEstopSource() {
        return estopSource;
    }

    public void setEstopSource(int estopSource) {
        this.estopSource = estopSource;
    }

    /**
     * Gets a status object from the string returned by this command:
     *
     * "rosrun baxter_tools enable_robot.py -s"
     *
     * Example return string: ready: False\nenabled: False\nstopped:
     * False\nerror: False \nestop_button: 0\nestop_source: 0\n
     *
     * @param statusString
     * @return
     */
    public static Status getStatusFromString(String statusString) {
        if (statusString == null) {
            return null;
        }
        
        Status status = new Status();
        String[] tokens = statusString.split("\n");
        for (String token : tokens) {
            if (token.contains("ready")) {
                status.setReady(token.contains("True"));
            }
            else if (token.contains("enabled")) {
                status.setEnabled(token.contains("True"));
            }
            else if (token.contains("stopped")) {
                status.setStopped(token.contains("True"));
            }
            else if (token.contains("estop_button")) {
                status.setEstopButton(
                        Integer.parseInt(token.split(":")[1].trim()));
            }
            else if (token.contains("estop_source")) {
                status.setEstopSource(
                        Integer.parseInt(token.split(":")[1].trim()));
            }
        }
        return status;
    }

}
