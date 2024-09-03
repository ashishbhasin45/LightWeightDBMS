package Assignment1.Helpers;

import java.util.Random;

public class CaptchaGenerator {
    public static boolean checkCaptcha(String captcha, String user_captcha)
    {
        return captcha.equals(user_captcha);
    }

    /**
     * Generates a CAPTCHA of given length
     * @param n number of alphanumeric characters for captcha
     * @return captcha
     */
    public static String generateCaptcha(int n)
    {
        Random rand = new Random(62);
        String chrs = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

        // Generate n characters from above set and
        // add these characters to captcha.
        StringBuilder captcha = new StringBuilder();
        while (n-- > 0){
            int index = (int)(Math.random()*62);
            captcha.append(chrs.charAt(index));
        }

        return captcha.toString();
    }
}
