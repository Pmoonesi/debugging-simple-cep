package ir.sss.common;

import ir.sss.model.Event;
import ir.sss.model.Fact;
import ir.sss.model.RequestEvent;
import ir.sss.model.Signal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads {@link Fact objects from the given (CSV) file. 
 * 
 * @author <a href="mailto:duncan.doyle@redhat.com">Duncan Doyle</a>
 */
public class FactsLoader {

	private static final Logger LOGGER = LoggerFactory.getLogger(FactsLoader.class);

	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd:HHmmssSSS");
	
	public static List<Event> loadEvents(File eventsFile) {

		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(eventsFile));
		} catch (FileNotFoundException fnfe) {
			String message = "File not found.";
			LOGGER.error(message, fnfe);
			throw new IllegalArgumentException(message, fnfe);
		}
		return loadEvents(br);
		
	}
	
	public static List<Event> loadEvents(InputStream eventsInputStream) {
		BufferedReader br = new BufferedReader(new InputStreamReader(eventsInputStream));
		return loadEvents(br);
		
	}
	
	private static List<Event> loadEvents(BufferedReader reader) {
		List<Event> eventList = new ArrayList<Event>();
		try {
			String nextLine;
			while ((nextLine = reader.readLine()) != null) {
				if (!nextLine.startsWith("#") && !("").equals(nextLine)) {
					Event bagScanEvent = readEvent(nextLine);
					if (bagScanEvent != null) {
						eventList.add(bagScanEvent);
					}
				}
			}
		} catch (IOException ioe) {
			throw new RuntimeException("Got an IO exception while reading events.", ioe);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException ioe) {
					// Swallowing exception, not much we can do here.
					LOGGER.error("Unable to close reader.", ioe);
				}
			}
		}
		return eventList;
	}

	/**
	 * Layout of a RequestEvent line has to be {event-id}, {bag-tag uuid}, {location}, {date}.
	 * 
	 * @param line
	 *            the line to parse.
	 * @return the {@link Fact}
	 */
	private static Event readEvent(String line) {
		String[] eventData = line.split("\\|");
		if (!(eventData.length == 7 || eventData.length == 2)) {
			LOGGER.error("Unable to parse string: " + line);
		}

		Event event = null;



		try {
			if (line.startsWith("SIG")) {
				event = new Signal(Signal.Type.GC, DATE_FORMAT.parse(eventData[1].trim()));
			} else {
				event = new RequestEvent(Integer.parseInt(eventData[0].trim()), Integer.parseInt(eventData[1].trim()), eventData[2].trim(), eventData[3].trim(), eventData[4].trim(), eventData[5].trim(), DATE_FORMAT.parse(eventData[6].trim()));
			}
		}  catch (ParseException pe) {
			LOGGER.error("Error parsing line: " + line, pe);
		}

		return event;

	}

}
