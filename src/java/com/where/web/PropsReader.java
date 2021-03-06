package com.where.web;

import org.apache.log4j.Logger;
import org.apache.commons.collections.iterators.IteratorEnumeration;
import org.apache.commons.collections.iterators.EnumerationIterator;
import org.apache.commons.collections.EnumerationUtils;

import java.util.*;
import java.net.URL;
import java.io.IOException;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.google.common.collect.Lists;

/**
 * @author Charles Kubicek
 */
public class PropsReader {
    private static final Logger LOG = Logger.getLogger(PropsReader.class);
    public static final String PROPS_FILE_NAME = "wmt.properties";
    private static final Properties PROPS = new Properties();
    private static final String LINE_SYNCHRONIZER_FACTORY_CLASS_PROP_NAME = "branchSynchronizerFactoryClass";

    static {
        URL resource = PropsReader.class.getClassLoader().getResource(PROPS_FILE_NAME);
        LOG.info("PropsReader.static intializer will load from " + resource);
        if (resource == null) {
            LOG.warn("Did not read properties from " + PROPS_FILE_NAME + ", using defaults");
            PROPS.putAll(ImmutableMap.of(LINE_SYNCHRONIZER_FACTORY_CLASS_PROP_NAME, DefaultLineIteratorSynchronizerFactoryImpl.class.getName()));
        } else {
            try {
                PROPS.load(resource.openStream());
                LOG.info("loaded classes from " + resource);
            } catch (IOException e) {
                LOG.warn("Did not read properties from " + PROPS_FILE_NAME + ", using defaults");
                PROPS.putAll(ImmutableMap.of(LINE_SYNCHRONIZER_FACTORY_CLASS_PROP_NAME, DefaultLineIteratorSynchronizerFactoryImpl.class.getName()));
            }
        }
    }

    public static List<String> getServers(){
        String serverProp = PROPS.getProperty("servers");
        return Lists.newArrayList(new EnumerationIterator(new StringTokenizer(serverProp, ",")));
    }

    public static void main(String[] args) {
        System.out.println("PropsReader.main "+getServers());
    }

    public static LineIteratorSynchronizerFactory buildLineIteratorSynchronizerFactoryInstance() {
        try {
            return (LineIteratorSynchronizerFactory) Class.forName(PROPS.getProperty(LINE_SYNCHRONIZER_FACTORY_CLASS_PROP_NAME)).newInstance();
        } catch (ClassNotFoundException e) {
            LOG.fatal("Didn't find " + LINE_SYNCHRONIZER_FACTORY_CLASS_PROP_NAME);
            throw new RuntimeException("Didn't find " + LINE_SYNCHRONIZER_FACTORY_CLASS_PROP_NAME);
        } catch (IllegalAccessException e) {
            LOG.fatal("Couldn't build syncer class: " + e.getMessage());
            throw new RuntimeException("Couldn't build syncer class: " + e.getMessage(), e);
        } catch (InstantiationException e) {
            LOG.fatal("Couldn't build syncer class: " + e.getMessage());
            throw new RuntimeException("Couldn't build syncer class: " + e.getMessage(), e);
        }
    }
}
