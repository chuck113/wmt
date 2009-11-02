package com.where.tfl.grabber;

import com.where.domain.BranchStop;

import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.HashMap;

/**
 * Contains the fromStation so that we can use previous results to
 * get station data from another station. If we did this without
 * giving the station results came from then things like 'at platform'
 * would be misinterprited. 
 *
 * @author Charles Kubicek
 */
public class BoardParserResultFromStation{
    
    private final BoardParserResult delegate;
    private final BranchStop fromStation;

    public BoardParserResultFromStation(BoardParserResult delegate, BranchStop fromStation) {
        this.delegate = delegate;
        this.fromStation = fromStation;
    }

//        private Map<String, Map<String, List<String>>> convert(Map<String, List<String>> input){
//        Map<String, Map<String, List<String>>> res = new HashMap<String, Map<String, List<String>>>();
//
//        for (String key : input.keySet()) {
//            List<String> list = input.get(key);
//            res.put(key, Collections.singletonMap("Platform 1", list));
//        }
//
//        return res;
//    }

    public BoardParserResultFromStation(BoardParserResult.BoardParserResultCode resultCode, Map<String, Map<String, List<String>>> boardData, BranchStop fromStation) {
        this.fromStation = fromStation;
        this.delegate = new BoardParserResult(resultCode, boardData);
    }

//    @Deprecated
//    public Map<String, List<String>> getBoardData() {
//        return delegate.getBoardData();
//    }

    public Map<String, Map<String, List<String>>> getBoardDataWithPlatforms() {
        return delegate.getBoardDataWithPlatformNames();
    }

    public BoardParserResult.BoardParserResultCode getResultCode() {
        return delegate.getResultCode();
    }

    public BranchStop getFromStation() {
        return fromStation;
    }
}
