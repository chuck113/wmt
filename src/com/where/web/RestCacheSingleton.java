package com.where.web;

import java.util.LinkedHashMap;

/**
 * @author Charles Kubicek
 */
public class RestCacheSingleton {

    private final LinkedHashMap CACHE = new LinkedHashMap();
    private static final RestCacheSingleton INSTANCE = new RestCacheSingleton();

    private RestCacheSingleton(){

    }

    public static RestCacheSingleton instance(){
        return INSTANCE;
    }

    public void put(String key, Object value){
       CACHE.put(key, value);
    }

    public <T> T get(String key){
       return (T) CACHE.get(key); 
    }
}
