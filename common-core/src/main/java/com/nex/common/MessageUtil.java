package com.nex.common;

public class MessageUtil {

    public static String getMessage(int status) {
        String message;

        if (status == 400) {
            message = "Bad Request";
        } else if (status == 401) {
            message = "Unauthorized";
        } else if (status == 403) {
            message = "Forbidden";
        } else if (status == 404) {
            message = "Not Found";
        } else if (status == 405) {
            message = "Method Not Allowed";
        } else if (status == 502) {
            message = "Bad Gateway";
        } else if (status == 503) {
            message = "Service Unavailable";
        } else if (status == 504) {
            message = "Gateway Timeout";
        } else {
            message = "Internal Server Error";
        }
        return message;
    }
}
