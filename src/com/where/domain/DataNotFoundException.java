package com.where.domain;

/**
 * @author Charles Kubicek
 */
public class DataNotFoundException extends RuntimeException{

    public DataNotFoundException(String error) {
        super(error);
    }

}
