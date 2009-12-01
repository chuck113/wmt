package com.where.web;

import java.util.Date;

/**
 * @author Charles Kubicek
*/
class LineParseResult {
    private long recordedAt = 0;
    private String result = null;
    private boolean parseInProgress = false;

    public void update(long recordedAt, String result, boolean parseInProgress) {
        this.recordedAt = recordedAt;
        this.result = result;
        this.parseInProgress = parseInProgress;
    }

    public long getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(long recordedAt) {
        this.recordedAt = recordedAt;
    }

    public CharSequence getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public boolean isParseInProgress() {
        return parseInProgress;
    }

    public void setParseInProgress(boolean parseInProgress) {
        this.parseInProgress = parseInProgress;
    }

    public boolean isValid() {
        System.out.println("AbstractLinesResource$LastResult.isValid checking if " + new Date(recordedAt + WmtProperties.DATA_VALIDITY_PERIOD_MS) + " > " + new Date());
        return (recordedAt + WmtProperties.DATA_VALIDITY_PERIOD_MS) > System.currentTimeMillis();
    }
}
