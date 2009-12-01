package com.where.collect;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedHashMap;
import java.io.Serializable;

/**
 * Data structure that backs a list by a map to allow for faster lookups-
 * not actually tested so not sure if it makes much difference...
 *
 * @author Charles Kubicek
 */
public class OrderedMap<V> implements Serializable {

    private final Map<V, Integer> indexMapping;
    private final List<V> inputList;

    public OrderedMap(List<V> inputList){
        this.indexMapping = new HashMap<V, Integer>();
        this.inputList = inputList;

        for (int i=0; i<inputList.size(); i++) {
            indexMapping.put(inputList.get(i), i);
        }
    }

    public V get(int i){
        if(i < 0){
            throw new IndexOutOfBoundsException("must not be negative");
        }
        return inputList.get(i);
    }

    public int indexOf(V value){
        if(indexMapping.containsKey(value)){
            return indexMapping.get(value);
        }else{
            return -1;
        }
    }

    public int size() {
        return inputList.size();
    }
}
