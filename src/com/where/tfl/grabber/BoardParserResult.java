package com.where.tfl.grabber;

import com.where.dao.hsqldb.TimeInfo;

import java.util.List;
import java.util.Map;
import java.util.Collections;

/**
 * @author Charles Kubicek
*/
public class BoardParserResult {
    private final BoardParserResultCode resultCode;
    private final Map<String, List<String>> boardData;

    public BoardParserResult(BoardParserResultCode resultCode, Map<String, List<String>> boardData) {
        this.resultCode = resultCode;
        this.boardData = Collections.unmodifiableMap(boardData);
    }

    public BoardParserResultCode getResultCode() {
        return resultCode;
    }

    public Map<String, List<String>> getBoardData() {
        return boardData;
    }

    public static enum BoardParserResultCode {
        OK, UNAVAILABLE, PARSE_EXCEPTION;
    }
}
