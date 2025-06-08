package com.maximorero14.payment.service;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.springframework.stereotype.Service;

@Service
public class UtilsService {
    public String getStackTraceAsString(Throwable throwable) {
        if(throwable == null || throwable.getStackTrace().length == 0){
            return "";
        }
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        throwable.printStackTrace(printWriter);
        return stringWriter.toString().replaceAll(System.lineSeparator(), " ");
    }
}
