package ltd.dreamcraft.xinxinautoreview.utils;


import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Base64;

public class MessageUtil {

    public static String bufferedImgToMsg(BufferedImage image) {
        return getImageMsg("base64://" + imageToBase64(image));
    }
    public static String getImageMsg(String file) {
        file = formatCQCode(file);
        return "[CQ:image,file=" + file + "]";
    }
    public static String formatCQCode(String str) {
        return str.replace("&", "&amp;").replace("[", "&#91;").replace("]", "&#93;").replace(",", "&#44;");
    }
    public static String imageToBase64(BufferedImage image) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        try {
            ImageIO.write(image, "png", os);
            return Base64.getEncoder().encodeToString(os.toByteArray());
        } catch (IOException var3) {
            throw new UncheckedIOException(var3);
        }
    }


}


