package com.manga.collectionBend.exceptions;

public class FileExistsException extends RuntimeException{
    public FileExistsException(String message){
//        this super() gets all methods of parent class into child class by constructor
        super(message);
    }
}
