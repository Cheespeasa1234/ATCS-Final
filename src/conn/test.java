package conn;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;

public class test {
    public static void main(String[] args) throws NoSuchAlgorithmException {
        KeyPair kp = Security.generateKeyPair();
        System.out.println(kp.getPublic().toString());

        String msg = "Cock and ball torture (CBT) is a sexual activity involving the application of pain or constriction to the male genitals. This may involve directly painful activities, such as genital piercing, wax play, genital spanking, squeezing, ball-busting, genital flogging, urethral play, tickle torture, erotic electrostimulation, kneeing or kicking. The recipient of such activities may receive direct physical pleasure via masochism, or emotional pleasure through erotic humiliation, or knowledge that the play is pleasing to a sadistic dominant. Many of these practices carry significant health risks.";
        try {
            String encrypted = Security.encrypt(msg, kp.getPublic());
            System.out.println("Encrypted: " + encrypted);
            String decrypted = Security.decrypt(encrypted, kp.getPrivate());
            System.out.println("Decrypted: " + decrypted);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
