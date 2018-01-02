/*==============================================================================
 =
 = Copyright 2017: darkware.org
 =
 =    Licensed under the Apache License, Version 2.0 (the "License");
 =    you may not use this file except in compliance with the License.
 =    You may obtain a copy of the License at
 =
 =        http://www.apache.org/licenses/LICENSE-2.0
 =
 =    Unless required by applicable law or agreed to in writing, software
 =    distributed under the License is distributed on an "AS IS" BASIS,
 =    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 =    See the License for the specific language governing permissions and
 =    limitations under the License.
 =
 =============================================================================*/

package org.darkware.turnstile;

import com.google.common.base.Preconditions;
import io.zeropointx.time.DefaultTimeProvider;
import io.zeropointx.time.TimeProvider;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * A {@link TurnstileObserver} receives event notifications from a {@link Turnstile} and provides methods
 * for calculating statistics or querying information about the passage of events.
 *
 * @author jeff@darkware.org
 * @since 2017-05-17
 */
public class TurnstileObserver
{
    /** The ideal size of the event window */
    protected final static int IDEAL_SIZE = 40;

    /** A {@link Deque} containing a constrained window of recent events seen by this observer. */
    private final Deque<EventRecord> lastEvents;

    /** An object that supplies views of the current effective time. */
    private final TimeProvider timeProvider;

    /**
     * Create a new {@link TurnstileObserver}. The window of observed events is initially empty and time is provided
     * by {@link DefaultTimeProvider}.
     */
    public TurnstileObserver()
    {
        this(DefaultTimeProvider.getDefault());
    }

    public TurnstileObserver(final TimeProvider timeProvider)
    {
        super();

        this.lastEvents = new ConcurrentLinkedDeque<>();
        this.timeProvider = timeProvider;
    }

    /**
     * Mark the passage of a given event, as identified by a {@link Turnstile}.
     *
     * @param sequenceNumber The sequence number of the event within the {@link Turnstile}.
     */
    protected void observe(final long sequenceNumber)
    {
        synchronized (this.lastEvents)
        {
            final long now = this.timeProvider.getTimestamp();

            // If this isn't the first event...
            if (!this.lastEvents.isEmpty())
            {
                final EventRecord lastEvent = this.lastEvents.getFirst();

                // Check if we've missed one or more events
                final long missingEvents = (sequenceNumber - 1) - lastEvent.getSequenceNumber();
                if (missingEvents > 0)
                {
                    // Populate them with fake data
                    long elapsedTime = Math.max(0, now - lastEvent.eventTime);
                    for (long i = lastEvent.getSequenceNumber(); i < sequenceNumber-1; i++)
                    {
                        this.recordEvent(i, lastEvent.getEventTime() + (elapsedTime * (i / missingEvents + 1)));
                    }
                }
            }

            this.recordEvent(sequenceNumber, now);
            this.pruneWindow();
        }
    }

    /**
     * Prunes the window to an ideal size.
     */
    protected void pruneWindow()
    {
        while (this.lastEvents.size() > TurnstileObserver.IDEAL_SIZE)
        {
            this.lastEvents.pollLast();
        }
    }

    /**
     * Calculate an event rate based upon a specific size of window.
     *
     * @param window The number of events in the window.
     * @return The {@link FlowRate} over the described window.
     */
    public FlowRate calculateEventRate(final int window)
    {
        final int windowSize = Math.max(window, this.lastEvents.size());

        final long windowStart = this.lastEvents.stream()
                                                .limit(windowSize)
                                                .mapToLong(EventRecord::getEventTime)
                                                .min()
                                                .orElse(new Long(this.timeProvider.getTimestamp()));

        long elapsedMillis = this.timeProvider.getTimestamp() - windowStart;
        Duration elapsedTime = Duration.of(elapsedMillis, ChronoUnit.MILLIS);

        return new FlowRate(windowSize, elapsedTime);
    }

    /**
     * Calculate an event rate based upon a time-bounded window.
     *
     * @param timeWindow A {@link Duration} of time from the current time to constrain the window.
     * @return The {@link FlowRate} over the described window.
     */
    public FlowRate calculateEventRate(final Duration timeWindow)
    {
        final long now = this.timeProvider.getTimestamp();
        final long windowBoundary = now - timeWindow.toMillis();

        final long events = this.lastEvents.stream()
                                           .filter(e -> e.getEventTime() > windowBoundary)
                                           .map(EventRecord::getEventTime)
                                           .count();

        return new FlowRate(events, timeWindow);
    }

    /**
     * Calculate the event rate with an adaptive set of events. An attempt is made to find a small set of
     * events which characterize the recent flow rate. The algorithm attempts to find three events or events
     * over at least 20ms, whichever is larger.
     *
     * @return A {@link FlowRate} over a very recent set of events.
     */
    public FlowRate calculateEventRate()
    {
        Preconditions.checkState(!this.lastEvents.isEmpty(), "Cannot calculate rate. No events observed.");

        final long now = this.timeProvider.getTimestamp();
        final long minimumBoundary = now - 20;
        final long lastRecordTime = this.lastEvents.getFirst().getEventTime();

        // Edge case: Only one event
        if (this.lastEvents.size() == 1)
        {
            return new FlowRate(1, Duration.of(now - lastRecordTime, ChronoUnit.MILLIS));
        }

        long recentWindowStartTime = lastRecordTime-1;
        long recordCount = 0;

        for (EventRecord record : this.lastEvents)
        {
            recentWindowStartTime = record.getEventTime();
            recordCount++;

            // Update the earliest record that fits within the criteria
            // - At least 20ms of elapsed time
            // - At least 3 recorded events
            if (recordCount > 3 && recentWindowStartTime < minimumBoundary) break;
        }

        return new FlowRate(recordCount-1, Duration.of(lastRecordTime - recentWindowStartTime, ChronoUnit.MILLIS));
    }

    /**
     * Write log messages for the current event window.
     */
    public void logObservedWindow()
    {
        Turnstile.log.debug("Observer event window: ({} events)", this.lastEvents.size());
        long origin = this.timeProvider.getTimestamp();
        for (EventRecord record : this.lastEvents)
        {
            final long offset = origin - record.getEventTime();
            Turnstile.log.debug("Sequence:{} @ {} (+{}ms)", record.getSequenceNumber(), record.getEventTime(), offset);
            origin = record.getEventTime();
        }
    }

    /**
     * Fetch the full window of events saved in this observer. The window is of limited size.
     *
     * @return The window as a {@link Deque}.
     */
    protected Deque<EventRecord> getEventWindow()
    {
        return this.lastEvents;
    }

    /**
     * Store a new event. This allows for either real (via {@link #observe(long)} or simulated events
     * (manufactured during a sequence number gap detection).
     *
     * @param sequenceNumber The sequence number of the event.
     * @param systemTime The time the event was observed.
     * @return The newly recorded {@link EventRecord}.
     */
    protected EventRecord recordEvent(final long sequenceNumber, final long systemTime)
    {
        EventRecord created = new EventRecord(sequenceNumber, systemTime);
        this.lastEvents.push(created);

        return created;
    }

    /**
     * An {@link EventRecord} is packaged tuple of the event sequence record and the observed time of
     * an event traversing a {@link Turnstile}.
     */
    protected static class EventRecord
    {
        /** The time the event was seen. */
        private final long eventTime;
        /** The sequential number of the event. */
        private final long sequenceNumber;

        /**
         * Create a new {@link EventRecord}.
         *
         * @param sequenceNumber The sequence number of the event.
         * @param eventTime The time the event was observed.
         */
        public EventRecord(final long sequenceNumber, final long eventTime)
        {
            super();

            this.sequenceNumber = sequenceNumber;
            this.eventTime = eventTime;
        }

        /**
         * Fetch the time the event was seen.
         *
         * @return The time as milliseconds past the Epoch.
         */
        public long getEventTime()
        {
            return eventTime;
        }

        /**
         * Fetch the sequence number noted for this event.
         *
         * @return The sequence number as a positive {@code long}.
         */
        public long getSequenceNumber()
        {
            return sequenceNumber;
        }
    }
}
