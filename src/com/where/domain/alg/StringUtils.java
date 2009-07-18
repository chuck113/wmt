package com.where.domain.alg;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * */
class StringUtils {

    public static String[] trimAll(String[] strings) {
        List<String> res = new ArrayList<String>(0);
        for(String st : strings){
         if (st != null && st.length() > 0) {
            res.add(st.trim());
            }
        }

        return res.toArray(new String[res.size()]);

    }

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
