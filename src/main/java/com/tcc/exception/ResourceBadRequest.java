package com.tcc.exception;

public class ResourceBadRequest extends RuntimeException{
    public ResourceBadRequest(String message){
        super(message);
    }
}
