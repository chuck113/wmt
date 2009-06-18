package com.where.tfl.grabber;

import com.where.dao.*;
import com.where.dao.hibernate.Branch;
import com.where.dao.hibernate.BranchStop;
import com.where.dao.hibernate.TflStationCode;

import java.net.URL;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * */
public class TFLScraper {
  private Logger LOG = Logger.getLogger(TFLScraper.class);
  private final TagSoupParser parser;
  private final Loader loader;


  public TFLScraper() {
    this.parser = new TagSoupParser();
    this.loader = Loader.instance();
  }

  public Map<String,List<TimeInfo>> get(BranchStop branchStop) throws ParseException{
    Branch branch = loader.getBranchStopsToBranches().get(branchStop);
    TflStationCode tflStationCode = branchStop.getStationCode();

    URL url = null;

    try {
      url = new URL("http://www.tfl.gov.uk/tfl/livetravelnews/departureboards/tube/default.asp?LineCode="+branch.getLine()+"&StationCode=" + tflStationCode.getCode() + "&Go=Go&switch=on");
      LOG.info("parsing: " + url);
      return this.parser.parse(url);
    } catch (MalformedURLException e) {
      LOG.warn("didn't get url: '"+url+"'", e);
      throw new ParseException("didn't get url: '"+url+"'", e);
    }
  }

  private void getBranchFromStop(BranchStop branchStop){

  }
}
