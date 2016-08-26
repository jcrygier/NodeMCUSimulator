package com.crygier.nodemcu.emu.http;

public class NMCUHttpResponse {

    private Integer statusCode;
    private String responseBody;

    public NMCUHttpResponse(Integer statusCode, String responseBody) {
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public String getBody() {
        return this.responseBody;
    }

    public Integer getStatusCode() {
        return this.statusCode;
    }
}
