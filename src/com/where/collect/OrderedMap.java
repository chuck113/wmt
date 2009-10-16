package com.where.collect;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedHashMap;

/**
 * @author Charles Kubicek
 */
public class OrderedMap<V> {

    public OrderedMap(List<V> inputList){
        this.indexMapping = new HashMap<V, Integer>();
        this.inputList = inputList;

        for (int i=0; i<inputList.size(); i++) {
            indexMapping.put(inputList.get(i), i);
        }
    }

    public V get(int i){
        return inputList.get(i);
    }

    private final Map<V, Integer> indexMapping;
    private final List<V> inputList;

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
