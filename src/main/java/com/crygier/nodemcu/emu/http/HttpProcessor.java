package com.crygier.nodemcu.emu.http;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;


/**
 * This processor doesn't do anything. Just like NodeMCU!
 */
public class HttpProcessor implements org.apache.http.protocol.HttpProcessor {
    @Override
    public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {

    }

    @Override
    public void process(HttpResponse response, HttpContext context) throws HttpException, IOException {

    }
}
