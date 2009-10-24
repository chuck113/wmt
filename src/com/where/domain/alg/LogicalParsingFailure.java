package com.where.domain.alg;

/**
     * Our weak exception that informs us that somthing has gone wrong with the parsing, will
 * happen when all the stops on a branch are unavailable
 */
public interface LogicalParsingFailure {
    String getReason();
    boolean shouldGiveUp();
    boolean shouldStartNextBranch();
    InstructionAfterFailure getInstructions();


    /**
     * If parsing returns LogicalParsingFailure then tell the algrithim what is should do next
     * <p/>
     * FIXME it shoudn't be upto other methods to decide if the algorithm should stop or not,
     * instead the algorithm should decide based on the type of error
     */
    public enum InstructionAfterFailure {
        CONTINUE, START_NEXT_BRANCH, GIVEUP
    }
}
