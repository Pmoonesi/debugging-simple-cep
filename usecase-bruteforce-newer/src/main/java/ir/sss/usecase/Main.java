package ir.sss.usecase;

import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.sun.management.HotSpotDiagnosticMXBean;
import org.drools.core.time.impl.PseudoClockScheduler;
import ir.sss.model.Event;
import ir.sss.common.FactsLoader;
import org.drools.time.SessionClock;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    private static final String EVENTS_FILE_NAME = "logs.csv";

    private static final HotSpotDiagnosticMXBean hotspotMBean =
            ManagementFactory.getPlatformMXBean(HotSpotDiagnosticMXBean.class);

    /**
     * Dumps the heap to the specified file.
     *
     * @param filePath the path where the heap dump should be written
     * @param live true to dump only live objects; false to dump all objects
     */
    public static void dumpHeap(String filePath, boolean live) {
        try {
            hotspotMBean.dumpHeap(filePath, live);
            System.out.println("Heap dump created at: " + filePath);
        } catch (IOException e) {
            System.err.println("Failed to create heap dump: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        LOGGER.info("Initialize KIE.");
        KieServices kieServices = KieServices.Factory.get();
        KieContainer kieContainer = kieServices.getKieClasspathContainer();
        LOGGER.info("Creating KieSession.");
        KieSession kieSession = kieContainer.newKieSession();

//        decorateKieSession(kieSession);

        PseudoClockScheduler clock = kieSession.getSessionClock();
        kieSession.insert(clock);

        List<Event> events;
        try(InputStream eventFileInputStream = Main.class.getClassLoader().getResourceAsStream(EVENTS_FILE_NAME)) {
            events = FactsLoader.loadEvents(eventFileInputStream);
        } catch (IOException ioe) {
            throw new RuntimeException("I/O problem loading event file. Not much we can do in this lab.", ioe);

        }
        events.stream().forEach(event -> { insertAndFire(kieSession, event);});
        LOGGER.info("Finished Executing.");

        LOGGER.info("Getting Heap Dump.");
        dumpHeap("dump.hprof", true);
    }

    /**
     * Inserts an event into the session, advances the clock and fires the rules.
     *
     * @param kieSession
     * @param event
     */
    private static void insertAndFire(KieSession kieSession, Event event) {
        PseudoClockScheduler clock = kieSession.getSessionClock();
        kieSession.insert(event);
        long deltaTime = event.getTimestamp().getTime() - clock.getCurrentTime();
        if (deltaTime > 0) {
            clock.advanceTime(deltaTime, TimeUnit.MILLISECONDS);
        }
        kieSession.fireAllRules();
    }

    private static void decorateKieSession(KieSession kieSession) {
        kieSession.addEventListener(new LoggingRuleRuntimeEventListener());
    }

}
