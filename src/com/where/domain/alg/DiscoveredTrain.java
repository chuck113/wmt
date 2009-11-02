package com.where.domain.alg;

import com.where.domain.Point;
import com.where.domain.Direction;
import com.where.domain.BranchStop;

/**
 * @author Charles Kubicek
 */
public class DiscoveredTrain extends Point{
    private final BranchStop furthestStation;
    private final boolean isInbetweenStations;

    public DiscoveredTrain(Point point, BranchStop furthestStation, boolean inbetweenStations) {
        super(point.getLat(), point.getLng(), point.getDirection(), point.getDescription());
        this.furthestStation = furthestStation;
        isInbetweenStations = inbetweenStations;
    }

    public DiscoveredTrain(double x, double y, Direction direction, String description, BranchStop furthestStation, boolean inbetweenStations) {
        super(x, y, direction, description);
        this.furthestStation = furthestStation;
        isInbetweenStations = inbetweenStations;
    }

//    public Point getPoint() {
//        return point;
//    }

    public boolean isInbetweenStations(){
        return isInbetweenStations;    
    }

    public BranchStop getFurthestStation() {
        return furthestStation;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }
}
