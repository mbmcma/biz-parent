package cn.waynechu.springcloud.common.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

/**
 * @author zhuwei
 * @date 2019/6/19 16:39
 */
@Slf4j
@UtilityClass
public class Base64Util {

    /**
     * 网络图片转化为Base64字符串
     * <pre>
     * 前端在返回字符串前添加 [data:image/jpg;base64,] 即可转化为相应图片
     *
     * 支持的图片类型:
     *      data:image/gif;base64,       base64编码的gif图片数据
     *      data:image/png;base64,       base64编码的png图片数据
     *      data:image/jpeg;base64,      base64编码的jpeg图片数据
     *      data:image/x-icon;base64,    base64编码的icon图片数据
     *
     * Base64图片在线转化:
     *      http://tool.chinaz.com/tools/imgtobase/
     * </pre>
     *
     * @param netImageUrl 图片地址
     * @return Base64字符串
     */
    public static String netImageToBase64(String netImageUrl) {
        ByteArrayOutputStream data = new ByteArrayOutputStream();
        try {
            URL url = new URL(netImageUrl);
            byte[] by = new byte[1024];

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            InputStream is = conn.getInputStream();

            int len = -1;
            while ((len = is.read(by)) != -1) {
                data.write(by, 0, len);
            }
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 对字节数组Base64编码
        Base64.Encoder encoder = Base64.getEncoder();
        return encoder.encodeToString(data.toByteArray());
    }

    public static void main(String[] args) {
        String imageUrl = "https://upload.jianshu.io/users/upload_avatars/2378056/9a011f6ce0c4.jpg";
        String imageStr = netImageToBase64(imageUrl);
        System.out.println("data:image/jpg;base64," + imageStr);
    }
}