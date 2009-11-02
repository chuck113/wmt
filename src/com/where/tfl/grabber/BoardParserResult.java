package com.where.tfl.grabber;

import com.where.dao.hsqldb.TimeInfo;
import com.where.domain.BranchStop;

import java.util.*;

/**
 * @author Charles Kubicek
*/
public class BoardParserResult {
    private final BoardParserResultCode resultCode;
    private Map<String, Map<String, List<String>>> boardDataWithPlatformNames;

    public BoardParserResult(BoardParserResultCode resultCode, Map<String, Map<String, List<String>>> boardData) {
        this.resultCode = resultCode;
        this.boardDataWithPlatformNames = Collections.unmodifiableMap(boardData);
    }

    public BoardParserResultCode getResultCode() {
        return resultCode;
    }

    public Map<String, List<String>> getBoardData() {
        Map<String, List<String>> res = new HashMap<String, List<String>>();

        for(String key : boardDataWithPlatformNames.keySet()){
            Map<String, List<String>> platforms = boardDataWithPlatformNames.get(key);
            List<String> directionRes = new ArrayList<String>();
            for(String platformKey : platforms.keySet()){
                directionRes.addAll(platforms.get(platformKey));
            }

            res.put(key, directionRes);
        }

        return res;
    }

    public Map<String, Map<String, List<String>>> getBoardDataWithPlatformNames() {
        return boardDataWithPlatformNames;
    }

    public static enum BoardParserResultCode {
        OK, UNAVAILABLE, PARSE_EXCEPTION;
    }
}
