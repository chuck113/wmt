package com.where.tools;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.List;

/**
 * @author Charles Kubicek
 */
public class StationParser {

    private static List<String> allStationsLines;

    public StationParser(String allStationsFile) {
        try {
            allStationsLines = IOUtils.readLines(new FileInputStream(allStationsFile));
        } catch (IOException e) {
            throw new IllegalArgumentException("error", e);
        }
    }

    public void doParse(String hammerSmithStationsFile) {
        try {


            BufferedReader in = new BufferedReader(new FileReader(new File(hammerSmithStationsFile)));
            String line = in.readLine();
            String currentBranch = line.substring(1, line.length());
            System.out.println(currentBranch);
            while ((line = in.readLine()) != null) {
                if (line.startsWith("#")) {
                    currentBranch = line.substring(1, line.length());
                     System.out.println(currentBranch);
                    continue;
                }

                int start = "  PGpoints.push(new GPoint(".length();
                int middlecomma = line.indexOf(",");
                int end = line.indexOf("))");
                String lat = line.substring(start, middlecomma);
                String lng = line.substring(middlecomma + 1, end);

                Double.parseDouble(lat);
                Double.parseDouble(lng);
                String name = getStationName(lat, lng);
                System.out.println(lat + "," + lng+","+name);

            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getStationName(String lat, String lng) {
        for (String line : allStationsLines) {
            if (line.contains(lat) && line.contains(lng)) {
                //  var point = new GPoint(-0.18814032297458586,51.517934944389836); var marker = createMarker(point, "Royal Oak", icon); myMap.addOverlay(marker);

                int start = line.indexOf("\"")+1;
                int end = line.lastIndexOf("\"");
                return line.substring(start, end);
            }
        }
        throw new IllegalStateException("no station for corrds '"+lat+","+lng+"'");
    }
 

    public static void main(String[] args) {
        String allStationFile = "C:\\data\\projects\\wheresmytube\\etc\\allstations.txt";
        String hammersmithFile = "C:\\data\\projects\\wheresmytube\\etc\\hammersmithBranchStops.txt";
        String cirlceFile = "C:\\data\\projects\\wheresmytube\\etc\\centralBranchStops.txt";
        String picadillyFile = "C:\\data\\projects\\wheresmytube\\etc\\picadillyBranchStops.txt";
        StationParser stationParser = new StationParser(allStationFile);

        //stationParser.doParse(hammersmithFile);
        //stationParser.doParse(cirlceFile);
        stationParser.doParse(picadillyFile);
    }
}
