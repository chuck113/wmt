package com.where.domain.alg;

import com.where.domain.Direction;
import com.where.domain.BranchStop;

import java.util.List;
import java.util.Map;
import java.util.Collections;

/**
  * Data found at a station
 */
public class StationArrivalData {
    private final Map<String, List<String>> platformInfo;
    final Direction concreteDirection;
    final BranchStop stop;
    final LogicalParsingFailure error;

    public StationArrivalData(BranchStop stop, Map<String, List<String>> platformInfo, Direction concreteDirection, LogicalParsingFailure error) {
        this.stop = stop;
        this.platformInfo = platformInfo;
        this.concreteDirection = concreteDirection;
        this.error = error;
    }

    public StationArrivalData(LogicalParsingFailure error) {
        this(null, null, null, error);
    }

    public Map<String, List<String>> getPlatformInfo(){
        return platformInfo;
    }

    public StationArrivalData(BranchStop stop, Map<String, List<String>> platformInfo, Direction concreteDirection) {
        this(stop, platformInfo, concreteDirection, BranchIterationFailures.NO_ERROR);
    }

    public boolean hasError() {
        return error != null;
    }

    public String dump() {
        StringBuilder builder = new StringBuilder();
        builder.append("direction: " + concreteDirection.getName() + "\n");
        builder.append("stop: " + stop.getStation().getName() + "\n");
        builder.append("error: " + error.getReason() + "\n");

        for(String dir : platformInfo.keySet()){
            List<String> dirInfo = platformInfo.get(dir);
            for (String info : dirInfo) {
                builder.append("dir = '"+info+"', info: " + info + "\n");
            }
        }
        return builder.toString() + "\n";
    }
}
