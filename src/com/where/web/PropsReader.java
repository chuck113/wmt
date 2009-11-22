package com.where.web;

import org.apache.log4j.Logger;

import java.util.Properties;
import java.net.URL;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import com.google.common.collect.ImmutableMap;
import com.where.domain.alg.BranchIterator;

/**
 * @author Charles Kubicek
 */
public class PropsReader {
    private static final Logger LOG = Logger.getLogger(PropsReader.class);
    public static final String PROPS_FILE_NAME = "wmt.properties";
    private static final Properties PROPS = new Properties();

    private static final String BRANCH_SYNCHRONIZER_FACTORY_CLASS_PROP_NAME = "branchSynchronizerFactoryClass";

    static {
        URL resource = PropsReader.class.getClassLoader().getResource(PROPS_FILE_NAME);
        System.out.println("PropsReader.static intializer will load from " + resource);
        if (resource == null) {
            LOG.warn("Did not read properties from " + PROPS_FILE_NAME + ", using defaults");
            PROPS.putAll(ImmutableMap.of(BRANCH_SYNCHRONIZER_FACTORY_CLASS_PROP_NAME, DefaultBranchIteratorSynchronizerFactoryImpl.class.getName()));
        } else {
            try {
                PROPS.load(resource.openStream());
                LOG.info("loaded classes from " + resource);
            } catch (IOException e) {
                LOG.warn("Did not read properties from " + PROPS_FILE_NAME + ", using defaults");
                PROPS.putAll(ImmutableMap.of(BRANCH_SYNCHRONIZER_FACTORY_CLASS_PROP_NAME, DefaultBranchIteratorSynchronizerFactoryImpl.class.getName()));
            }
        }
    }

    public static BranchIteratorSynchronizerFactory buildBranchIteratorSynchronizerFactoryInstance() {
        try {
            return (BranchIteratorSynchronizerFactory) Class.forName(PROPS.getProperty(BRANCH_SYNCHRONIZER_FACTORY_CLASS_PROP_NAME)).newInstance();
        } catch (ClassNotFoundException e) {
            LOG.fatal("Didn't find " + BRANCH_SYNCHRONIZER_FACTORY_CLASS_PROP_NAME);
            throw new RuntimeException("Didn't find " + BRANCH_SYNCHRONIZER_FACTORY_CLASS_PROP_NAME);
        } catch (IllegalAccessException e) {
            LOG.fatal("Couldn't build syncer class: " + e.getMessage());
            throw new RuntimeException("Couldn't build syncer class: " + e.getMessage(), e);
        } catch (InstantiationException e) {
            LOG.fatal("Couldn't build syncer class: " + e.getMessage());
            throw new RuntimeException("Couldn't build syncer class: " + e.getMessage(), e);
        }
    }
}
