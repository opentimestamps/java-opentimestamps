package com.eternitywall.ots;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.Assert.assertThat;

public class TestOtsCli {

    @Test
    public void testCommandLineHandlesUpgradeCommandWithWrongFileName() {
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        Logger logger = (Logger) LoggerFactory.getLogger(OtsCli.class);
        logger.addAppender(listAppender);

        OtsCli.main(new String[]{"upgrade", "some_non_existent_file.name"});

        assertThat("Upgrade with non existing file should log 'No valid file' error",
                listAppender.list, hasLogMessage("No valid file"));
    }

    private static HasLoggingEventMessage hasLogMessage(String message) {
        return new HasLoggingEventMessage(message);
    }

    private static class HasLoggingEventMessage extends BaseMatcher<List<ILoggingEvent>> {

        private final String message;

        private HasLoggingEventMessage(String message) {
            this.message = message;
        }

        @Override
        public boolean matches(Object o) {
            if (o instanceof List) {
                for (Object event : (List<?>) o) {
                    if (event instanceof ILoggingEvent
                            && ((ILoggingEvent) event).getFormattedMessage().equals(message)) {
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("to have log message '" + message + "'");
        }
    }
}
