package com.clientserver;

public class GetResponse {
    public final String name;
    public final java.util.Optional<String> email;
    public GetResponse(String name, java.util.Optional<String> email) {
        this.name = name;
        this.email = email;
    }



}
