
package com.appian.appianbaxter;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import javax.imageio.ImageIO;

/**
 *
 * @author serdar
 */
public class FileUtils {

    public static List<File> getImageFiles(File directory) {
        return Arrays.asList(directory.listFiles(new ImageFilter()));
    }

    public static File getLastImage(File directory) {
        List<File> allFiles = FileUtils.getImageFiles(directory);
        File lastImage = null;
        for (File file : allFiles) {
            if (lastImage == null
                    || file.lastModified() > lastImage.lastModified()) {
                lastImage = file;
            }
        }
        return lastImage;
    }
    
    public static File makeDirectory(String path) {
        File directory = new File(path);
        try {
            directory.mkdirs();
        } catch(SecurityException ex) {
            return null;
        }
        return directory;
    }
    
    public static byte[] getImageDataToSend(File image) throws IOException {
        BufferedImage bufferedImage = ImageIO.read(image);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "jpeg", baos);
        return baos.toByteArray();
    }
}

class ImageFilter implements FilenameFilter {

    @Override
    //return true if find a file named "a",change this name according to your file name
    public boolean accept(final File dir, final String name) {
        return name.endsWith(".jpg") 
                | name.endsWith(".jpeg") 
                | name.endsWith(".png");

    }
}
