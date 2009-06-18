/**
 * */

import au.id.jericho.lib.html.*;
import java.util.*;
import java.io.*;
import java.net.*;

public class FindSpecificTags {
	public static void main(String[] args) throws Exception {
		String sourceUrlString="data/test.html";
		if (args.length==0)
		  System.err.println("Using default argument of \""+sourceUrlString+'"');
		else
			sourceUrlString=args[0];
		if (sourceUrlString.indexOf(':')==-1) sourceUrlString="file:"+sourceUrlString;
		MicrosoftTagTypes.register();
		MasonTagTypes.register();
		Source source=new Source(new URL(sourceUrlString));
		System.out.println("\n*******************************************************************************\n");

		System.out.println("XML Declarations:");
		displaySegments(source.findAllTags(StartTagType.XML_DECLARATION));

		System.out.println("XML Processing instructions:");
		displaySegments(source.findAllTags(StartTagType.XML_PROCESSING_INSTRUCTION));

		PHPTagTypes.register(); // register PHPTagTypes after searching for XML processing instructions, otherwise PHP short tags override them.
		StartTagType.XML_DECLARATION.deregister(); // deregister XML declarations so they are recognised as PHP short tags, consistent with the real PHP parser.
		source=new Source(source); // have to create a new Source object after changing tag type registrations otherwise cache might contain tags found with previous configuration.
		System.out.println("##################### PHP tag types now added to register #####################\n");

		System.out.println("H2 Elements:");
		displaySegments(source.findAllElements(Tag.H2));

		System.out.println("Document Type Declarations:");
		displaySegments(source.findAllTags(StartTagType.DOCTYPE_DECLARATION));


		System.out.println("CDATA sections:");
		displaySegments(source.findAllTags(StartTagType.CDATA_SECTION));

		System.out.println("Common server tags: (eg ASP, JSP, PSP, ASP-style PHP or Mason substitution tag)");
		displaySegments(source.findAllTags(StartTagType.SERVER_COMMON));

		System.out.println("Tags starting with <%=");
		displaySegments(source.findAllStartTags("%="));

		System.out.println("Tags starting with <%=var");
		displaySegments(source.findAllStartTags("%=var"));

		System.out.println("HTML Comments:");
		displaySegments(source.findAllTags(StartTagType.COMMENT));

		System.out.println("Elements in namespace \"o\" (generated by MS-Word):");
		displaySegments(source.findAllElements("o:"));

		System.out.println("Tags starting with <![ (commonly generated by MS-Word):");
		displaySegments(source.findAllStartTags("!["));

		// Note: The end of a PHP tag can not be reliably found without the use of a PHP parser,
		// meaning any PHP tag found by this library is not guaranteed to have the correct end position.
		System.out.println("Standard PHP tags:");
		displaySegments(source.findAllTags(PHPTagTypes.PHP_STANDARD));

		System.out.println("Short PHP tags:");
		displaySegments(source.findAllTags(PHPTagTypes.PHP_SHORT));

		System.out.println("Mason Component Calls:");
		displaySegments(source.findAllTags(MasonTagTypes.MASON_COMPONENT_CALL));

		System.out.println("Mason Components Called With Content:");
		displaySegments(source.findAllElements(MasonTagTypes.MASON_COMPONENT_CALLED_WITH_CONTENT));

		System.out.println("Mason Named Blocks:");
		displaySegments(source.findAllElements(MasonTagTypes.MASON_NAMED_BLOCK));

		System.out.println("Unregistered start tags:");
		displaySegments(source.findAllTags(StartTagType.UNREGISTERED));

		System.out.println("Unregistered end tags:");
		displaySegments(source.findAllTags(EndTagType.UNREGISTERED));

		System.out.println(source.getCacheDebugInfo());
  }

	private static void displaySegments(List segments) {
		for (Iterator i=segments.iterator(); i.hasNext();) {
			Segment segment=(Segment)i.next();
			System.out.println("-------------------------------------------------------------------------------");
			System.out.println(segment.getDebugInfo());
			System.out.println(segment);
		}
		System.out.println("\n*******************************************************************************\n");
	}
}
