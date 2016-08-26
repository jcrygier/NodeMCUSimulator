package com.crygier.nodemcu.emu.http;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.HttpClientBuilder;
import org.luaj.vm2.LuaClosure;
import org.luaj.vm2.LuaValue;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class HttpRequest {

    private String method;
    private String url;
    private String extraHeaders;
    private String body;

    public HttpRequest(String method, String url, String extraHeaders, String body) {
        this.method = method;
        this.url = url;
        this.extraHeaders = extraHeaders;
        this.body = body;
    }

    private RequestBuilder getHttpRequestObj(String method, String uri) {
        return RequestBuilder.create(method).setUri(uri);
    }

    public void execute(LuaClosure callback) {
        RequestBuilder httpRequest = this.getHttpRequestObj(this.method, this.url);
        HttpClient client = HttpClientBuilder.create().setHttpProcessor(new HttpProcessor()).build();

        try {
            if (body != null && !body.isEmpty() && !body.equals("nil")) {
                this.setBody(httpRequest, this.body);
            }

            if (extraHeaders != null && !extraHeaders.isEmpty() && !extraHeaders.equals("nil")) {
                httpRequest = this.addHeaders(httpRequest, this.extraHeaders);
            }

            httpRequest = this.addOverrideHeaders(httpRequest);
            NMCUHttpResponse response = this.getResponse(client, httpRequest);
            callback.call(LuaValue.valueOf(response.getStatusCode()), LuaValue.valueOf(response.getBody()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private RequestBuilder addOverrideHeaders(RequestBuilder httpRequestBase) {
        httpRequestBase.addHeader("User-Agent", "ESP8266");
        httpRequestBase.addHeader("Connection", "close");
        httpRequestBase.addHeader("HOST", "requestb.in");

        return httpRequestBase;
    }

    private RequestBuilder addHeaders(RequestBuilder httpRequest, String extraHeaders) throws Exception {
        String extraHeaderArr[] = extraHeaders.split("\\r?\\n");
        for (String extraHeader : extraHeaderArr) {
            String[] header = extraHeader.split(":", 2);
            if (header.length != 2) {
                throw new Exception("Invalid header");
            }
            httpRequest.addHeader(header[0], header[1]);
        }
        return httpRequest;
    }

    private void setBody(RequestBuilder httpRequest, String body) throws Exception {
        HttpEntity entity = EntityBuilder.create().setText(body).build();
        httpRequest.setEntity(entity);
    }

    private NMCUHttpResponse getResponse(HttpClient client, RequestBuilder httpRequest) throws Exception {
        HttpClientContext context = HttpClientContext.create();
        HttpResponse response = client.execute(httpRequest.build(), context);
        System.out.println(context.getRequest().toString());

        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

        StringBuilder result = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            result.append(line);
            result.append(System.getProperty("line.separator"));
        }
        return new NMCUHttpResponse(response.getStatusLine().getStatusCode(), result.toString());
    }
}
