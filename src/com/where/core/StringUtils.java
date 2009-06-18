package com.where.core;

/**
 * */
class StringUtils {

  public static String trim(String stationString) {
    if (stationString != null && stationString.length() > 0) {
      return stationString.trim();
    }

    return null;
  }

  public static int endIndex(String st, int start){
      return (st.length() - start);
  }
}
