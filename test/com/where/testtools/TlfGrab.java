package com.where.testtools;

import tfl.grabber.RegexParserTest;
import com.where.tfl.grabber.RegexParser;
import org.apache.commons.io.IOUtils;

import java.net.URL;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * @author Charles Kubicek
 */
public class TlfGrab {
    public static void main(String[] args) {
        new TlfGrab().showFromFolder();
    }

    private void grabbit(){
        try {
            String url = "http://www.tfl.gov.uk/tfl/livetravelnews/departureboards/tube/default.asp?LineCode=bakerloo&StationCode=HSD";
            RegexParser parser = new RegexParser();
            parser.parse(new URL(url).openStream());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // victoria: 5395
    // jubilee: 5848
    private void showFromFolder(){
        try {
            RegexParser parser = new RegexParser();
            String folder = "C:\\data\\projects\\wheresmytube\\htmls\\jubilee-happy";
            File jub = new File(folder);
            for(File it:jub.listFiles()){
                parser.parse(new FileInputStream(it));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
