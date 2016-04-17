
package com.appian.appianbaxter.domainentity;

/**
 * A class encapsulating sensor readings
 *
 * @author serdar
 */
public class SensorReading {
    private SensorType sensorType;
    private double reading;
    
    public SensorReading(SensorType sensorType) {
        this.sensorType = sensorType;
    }

    public SensorType getSensorType() {
        return sensorType;
    }

    public void setSensorType(SensorType sensorType) {
        this.sensorType = sensorType;
    }

    public double getReading() {
        return reading;
    }

    public void setReading(double reading) {
        this.reading = reading;
    }
    
    
}
