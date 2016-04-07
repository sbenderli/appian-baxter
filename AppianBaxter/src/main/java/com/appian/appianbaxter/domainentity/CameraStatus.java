
package com.appian.appianbaxter.domainentity;

/**
 *
 * @author serdar
 */
public class CameraStatus {
    private boolean isLeftCamOpen;
    private boolean isRightCamOpen;
    private boolean isHeadCamOpen;
    
    public CameraStatus() {
        
    }
    
    public boolean isCameraOpen(Camera camera) {
        switch (camera) {
            case LEFT:
                return isLeftCamOpen;
            case RIGHT:
                return isRightCamOpen;
            case HEAD:
                return isHeadCamOpen;
            default:
                throw new AssertionError(camera.name());
        }
    }
    
    public static CameraStatus getStatusFromString(String statusString) {
        CameraStatus status = new CameraStatus();
        
        String[] tokens = statusString.split("\n");
        for(String token : tokens) {
            if (token.contains(Camera.LEFT.toString())) {
                status.isLeftCamOpen = token.contains("open");
            } else if (token.contains(Camera.RIGHT.toString())) {
                status.isRightCamOpen = token.contains("open");
            } else if (token.contains(Camera.HEAD.toString())) {
                status.isHeadCamOpen = token.contains("open");
            }
        }
        
        return status;
    }

    public boolean isIsLeftCamOpen() {
        return isLeftCamOpen;
    }

    public void setIsLeftCamOpen(boolean isLeftCamOpen) {
        this.isLeftCamOpen = isLeftCamOpen;
    }

    public boolean isIsRightCamOpen() {
        return isRightCamOpen;
    }

    public void setIsRightCamOpen(boolean isRightCamOpen) {
        this.isRightCamOpen = isRightCamOpen;
    }

    public boolean isIsHeadCamOpen() {
        return isHeadCamOpen;
    }

    public void setIsHeadCamOpen(boolean isHeadCamOpen) {
        this.isHeadCamOpen = isHeadCamOpen;
    }
    
    
}

