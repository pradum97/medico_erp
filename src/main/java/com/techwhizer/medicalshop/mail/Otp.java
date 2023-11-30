package com.techwhizer.medicalshop.mail;

import java.util.Random;

public class Otp {
    public static String generateOtp() {
        String numbers = "0123456789";
        Random random = new Random();
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            otp.append(numbers.charAt(random.nextInt(numbers.length())));
        }
        return otp.toString();
    }
}
