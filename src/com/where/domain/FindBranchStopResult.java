package com.where.domain;

public class FindBranchStopResult {

    private enum State{
        OK,NOT_ON_BRANCH,UNKOWN
    }

    private final State state;
    private final BranchStop branchStop;

    private FindBranchStopResult(State state, BranchStop branchStop) {
        this.state = state;
        this.branchStop = branchStop;
    }

    public static FindBranchStopResult notOnBranch(){
        return new FindBranchStopResult(State.NOT_ON_BRANCH, null);
    }

    public static FindBranchStopResult unknown(){
        return new FindBranchStopResult(State.UNKOWN, null);
    }

    public static FindBranchStopResult result(BranchStop result){
        return new FindBranchStopResult(State.OK, result);
    }

    public boolean hasResult(){
        return State.OK == state;
    }

    public boolean isOnAnotherBranch(){
        return State.NOT_ON_BRANCH == state;
    }

    public BranchStop getResult() {
        return branchStop;
    }
}
