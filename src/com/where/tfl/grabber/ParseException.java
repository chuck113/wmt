package com.where.tfl.grabber;

/**
 * */
public class ParseException extends Exception{
  public ParseException() {
  }

  public ParseException(String message) {
    super(message);
  }

  public ParseException(String message, Throwable cause) {
    super(message, cause);
  }
}
