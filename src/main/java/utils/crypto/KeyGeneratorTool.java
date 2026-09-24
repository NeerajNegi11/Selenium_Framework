package utils.crypto;

import javax.crypto.KeyGenerator;
import java.util.Base64;

public class KeyGeneratorTool {
    public static void main(String[] args) throws Exception {
        KeyGenerator kg = KeyGenerator.getInstance("AES");
        kg.init(256);
        String key = Base64.getEncoder().encodeToString(kg.generateKey().getEncoded());
        System.out.println("Your FRAMEWORK_SECRET_KEY (store it safely, never commit it):");
        System.out.println(key);
    }
}
