package com.where.domain.alg;

import com.where.dao.hsqldb.TimeInfo;
import com.where.domain.Direction;
import com.where.domain.BranchStop;

import java.util.List;

/**
     * Data found on a board
 */
public class BoardData {
    final List<String> timeInfo;
    final Direction concreteDirection;
    final BranchStop foundStop;
    final LogicalParsingFailure error;

    public BoardData(BranchStop foundStop, List<String> timeInfo, Direction concreteDirection, LogicalParsingFailure error) {
        this.foundStop = foundStop;
        this.timeInfo = timeInfo;
        this.concreteDirection = concreteDirection;
        this.error = error;
    }

    public BoardData(LogicalParsingFailure error) {
        this(null, null, null, error);
    }


    public BoardData(BranchStop foundStop, List<String> timeInfo, Direction concreteDirection) {
        this(foundStop, timeInfo, concreteDirection, BranchParseFailures.NO_ERROR);
    }

    public boolean hasError() {
        return error != null;
    }

    public String dump() {
        StringBuilder builder = new StringBuilder();
        builder.append("direction: " + concreteDirection.getName() + "\n");
        builder.append("foundStop: " + foundStop.getStation().getName() + "\n");
        builder.append("error: " + error.getReason() + "\n");

        for (String info : timeInfo) {
            builder.append("info: " + info + "\n");
        }
        return builder.toString() + "\n";
    }
}
