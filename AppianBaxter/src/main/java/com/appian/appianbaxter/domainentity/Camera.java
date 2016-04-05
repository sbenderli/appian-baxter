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
public enum Camera {
    LEFT("left_hand_camera"),
    RIGHT("right_hand_camera"),
    HEAD("head_camera");
    
    private final String name;       

    private Camera(String s) {
        name = s;
    }

    public boolean equalsName(String otherName) {
        return (otherName == null) ? false : name.equals(otherName);
    }

    @Override
    public String toString() {
       return this.name;
    }
}
