package com.mahmoudyoussef.url_shortener.generator;

import org.springframework.stereotype.Component;

@Component
public class Base62Generator {

    private static final String BASE62 =
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private static final int MIN_LENGTH = 6;

    public String encode(long id) {

        StringBuilder sb = new StringBuilder();

        while (id > 0) {
            sb.append(BASE62.charAt((int) (id % 62)));
            id /= 62;
        }

        String encoded = sb.reverse().toString();


        return pad(encoded);
    }

    private String pad(String input) {
        StringBuilder sb = new StringBuilder(input);

        while (sb.length() < MIN_LENGTH) {
            sb.insert(0, 'a');
        }

        return sb.toString();
    }
}