package com.where.web;

import org.restlet.resource.ResourceException;
import org.restlet.resource.Get;
import com.where.domain.Point;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Iterators;

import java.util.Set;
import java.util.ListIterator;
import java.util.List;
import java.util.Iterator;

/**
 * @author Charles Kubicek
 */
public class RootResource extends WmtResource {

    @Override
    protected void doInit() throws ResourceException {
        super.doInit();
    }

    /**
     * Returns a full representation for a given variant.
     */
    @Get
    //("json") //would be json but it means the text is downloaded as a file by browsers
    public String toJson() {
        return makeJsonLineLinks(WmtProperties.LINES_TO_ITERATE);
    }

     private String makeJsonLineLinks(List<String> lines) {
        Iterator<String> serverIterator = Iterators.cycle(PropsReader.getServers());
        StringBuffer buf = new StringBuffer("{\"lines\": { \"linesArray\" : [\n");
        for (ListIterator<String> iter = lines.listIterator(); iter.hasNext();) {
            String line = iter.next();
            buf.append("  { \"name\" : \"" + line + "\", \"url\" : \""+serverIterator.next()+"/"+WmtProperties.WEB_APP_CONTEXT+WmtRestApplication.LINE_RESOURCE_NAME+"/" + line + "\"}");
            if(iter.hasNext()){
                buf.append(",\n");
            }else{
                buf.append("\n");
            }
        }
        buf.append("]}}");
        return buf.toString();
    }
}
