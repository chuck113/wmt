package com.where.domain.alg;

/**
 * @author Charles Kubicek
*/
class BranchParseFailures {
    public static final LogicalParsingFailure NO_ERROR = new NoError();
    public static final LogicalParsingFailure HTTP_TIMEOUT_FAIULURE = new HttpTimeoutFailure();
    public static final LogicalParsingFailure END_OF_BRANCH_FAIULURE = new EndOfBranchFailure();

    private static abstract class AbstractLogicalParsingFailure implements LogicalParsingFailure{
        private final String reason;
        private final InstructionAfterFailure failure;

        protected AbstractLogicalParsingFailure(String reason, InstructionAfterFailure failure) {
            this.reason = reason;
            this.failure = failure;
        }

        public String getReason() {
            return reason;
        }

        public InstructionAfterFailure getInstructions() {
            return failure;
        }

        public boolean shouldGiveUp() {
            return failure == InstructionAfterFailure.GIVEUP;   
        }

        public boolean shouldStartNextBranch() {
            return failure == InstructionAfterFailure.START_NEXT_BRANCH;
        }
    }

    private static class EndOfBranchFailure extends AbstractLogicalParsingFailure {
        protected EndOfBranchFailure() {
            super("End of branch", InstructionAfterFailure.START_NEXT_BRANCH);
        }
    }

    private static class NoError extends AbstractLogicalParsingFailure {
        protected NoError() {
            super("No Error", InstructionAfterFailure.CONTINUE);
        }
    }

    private static class HttpTimeoutFailure extends AbstractLogicalParsingFailure {
        protected HttpTimeoutFailure() {
            super("HTTP timeout", InstructionAfterFailure.GIVEUP);
        }
    }
}
