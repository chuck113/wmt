package com.where.domain.alg;

import org.apache.log4j.Logger;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

import com.where.domain.Direction;
import com.where.domain.BranchStop;
import com.where.collect.OrderedMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.ImmutableList;

/**
 * We will recive various directions along one line, ie on jub will be south then west,
 * so we have an abstract direction that represents both directions. We start with an abstract direction
 * and get the "concrete" direction we need to measure.
 * <p/>
 * e.g. one directions are south and east so when we are parsing direction one and we get some
 * results we use the abstract directions to get either south or east
 */
public enum AbstractDirection {
    ONE(new Direction[]{Direction.DirectionEnum.SOUTHBOUND, Direction.DirectionEnum.EASTBOUND}),
    TWO(new Direction[]{Direction.DirectionEnum.NORTHBOUND, Direction.DirectionEnum.WESTBOUND});

    private static Logger LOG = Logger.getLogger(AbstractDirection.class);
    private final Map<String, Direction> concreteDirections;

    AbstractDirection(Direction[] directions) {
        concreteDirections = new HashMap<String, Direction>(directions.length);

        for (Direction direction : directions) {
            concreteDirections.put(direction.getName(), direction);
        }
    }    

    /**
     * Given a list of directions found from parsing readouts, get a
     * proper direction.
     *
     * If this direction had an empty board but the other direction had a non-empty
     * board then this method will return null as no concrete direction was found
     *
     * @param foundDirections
     * @return
     */
    public Direction getConcreteDirection(List<String> foundDirections) {
        for (String foundDirection : foundDirections) {
            if (concreteDirections.containsKey(foundDirection)){
                return concreteDirections.get(foundDirection);
            } else if (concreteDirections.containsKey(StringUtils.stripSquareBrackets(foundDirection))){
                LOG.warn("found direction with square brackets: "+foundDirection);
                return concreteDirections.get(StringUtils.stripSquareBrackets(foundDirection));
            }
        }
        return null;
    }

    private String makeDirections(){
        StringBuilder builder = new StringBuilder();
        for (String s : concreteDirections.keySet()) {
            builder.append(s+", ");
        }
        return builder.toString();
    }

    public <T> List<T> makeIterable(List<T> list){
        if (this == AbstractDirection.ONE) { // iterate backwards
            return ImmutableList.copyOf(Iterables.reverse(list));
        } else {
            return ImmutableList.copyOf(list);
        }
    }
}
