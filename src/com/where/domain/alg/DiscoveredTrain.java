package com.where.domain.alg;

import com.where.domain.Point;
import com.where.domain.Direction;
import com.where.domain.BranchStop;

/**
 * @author Charles Kubicek
 */
public class DiscoveredTrain extends Point{
    private final BranchStop furthestStation;

    public DiscoveredTrain(Point point, BranchStop furthestStation) {
        super(point.getLat(), point.getLng(), point.getDirection(), point.getDescription());
        this.furthestStation = furthestStation;
    }

    public DiscoveredTrain(double x, double y, Direction direction, String description, BranchStop furthestStation) {
        super(x, y, direction, description);
        this.furthestStation = furthestStation;
    }

//    public Point getPoint() {
//        return point;
//    }

    public BranchStop getFurthestStation() {
        return furthestStation;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }
}
