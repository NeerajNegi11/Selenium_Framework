package utils.crypto;

import java.io.Console;
import java.util.Scanner;

public class EncryptTool {
    public static void main(String[] args) {
        String plain;
        Console console = System.console();
        if (console != null) {
            plain = new String(console.readPassword("Value to encrypt: "));   // hidden input in real terminal
        } else {
            System.out.print("Value to encrypt: ");                          // IntelliJ Run console
            plain = new Scanner(System.in).nextLine();
        }
        System.out.println("Paste this into config.properties:");
        System.out.println("ENC(" + CryptoUtil.encrypt(plain) + ")");
    }
}