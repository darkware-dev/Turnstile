/*==============================================================================
 =
 = Copyright 2018: darkware.org
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

package io.zeropointx.turnstile;

import io.zeropointx.time.DefaultTimeProvider;
import io.zeropointx.time.ManualTimeProvider;
import io.zeropointx.time.TimeProvider;
import org.assertj.core.data.Offset;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;

/**
 * @author jeff@darkware.org
 * @since 2017-05-17
 */
public class TurnstileObserverTests
{
    private Turnstile turnstile;
    private Meter meter;
    private ManualTimeProvider timeProvider;
    private TimeProvider originalProvider;

    @Before
    public void setup()
    {
        this.timeProvider = new ManualTimeProvider();
        this.timeProvider.setSystemTimestamp();
        this.originalProvider = DefaultTimeProvider.getDefault();
        DefaultTimeProvider.setDefaultProvider(this.timeProvider);

        this.meter = new NoopMeter();
        this.turnstile = new Turnstile(this.meter);
    }

    @After
    public void reset()
    {
        DefaultTimeProvider.setDefaultProvider(this.originalProvider);
    }

    protected void feedEvents(final int count, final String rate)
    {
        final Turnstile feeder = new Turnstile(new RateControlledMeter(rate));
        feeder.start();

        try
        {
            for (int i = 0; i < count; i++)
            {
                this.turnstile.pass();
                feeder.pass();
            }
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException("Error while feeding testing Turnstile.");
        }
    }

    protected void fakeEvents(final int firstSeq, final int count, final long timeGap, final TimeProvider provider)
    {
        final long now = provider.getTimestamp();
        final long firstEvent = now - (timeGap * count);

        for (int i = 0; i < count; i++)
        {
            final long systemTime = firstEvent + (i * timeGap);
            System.err.println("Recording fake event @ " + systemTime);
            this.turnstile.getObserver().recordEvent(firstSeq + i, firstEvent + (i * timeGap));
        }
    }

    @Test
    public void simple()
    {
        TurnstileObserver observer = this.turnstile.getObserver();

        this.turnstile.start();
        this.fakeEvents(1, 20, 20, DefaultTimeProvider.getDefault());
        observer.logObservedWindow();

        assertThat(observer.calculateEventRate().getVolumePerSecond()).isEqualTo(50, Offset.offset(5.0));
    }

    @Test
    public void countWindow()
    {
        TurnstileObserver observer = this.turnstile.getObserver();
        this.timeProvider.setSystemTimestamp();

        this.turnstile.start();
        this.fakeEvents(1, 20, 50, timeProvider);
        observer.logObservedWindow();

        timeProvider.adjust(50, TimeUnit.MILLISECONDS);
        assertThat(observer.calculateEventRate(10).getVolumePerSecond()).isEqualTo(20, Offset.offset(5.0));
    }

    @Test
    public void timeWindow()
    {
        TurnstileObserver observer = this.turnstile.getObserver();

        this.turnstile.start();
        this.fakeEvents(1, 20, 20, DefaultTimeProvider.getDefault());
        final double rate = observer.calculateEventRate(Duration.of(500, ChronoUnit.MILLIS)).getVolumePerSecond();

        assertThat(rate).isEqualTo(50, Offset.offset(10.0));
    }

    @Test
    public void manyFrequentEvents()
    {
        TurnstileObserver observer = this.turnstile.getObserver();

        final long now = System.currentTimeMillis();
        long id = 1;
        observer.recordEvent(id++, now - 3);
        observer.recordEvent(id++, now - 2);
        observer.recordEvent(id++, now - 2);
        observer.recordEvent(id++, now - 2);
        observer.recordEvent(id++, now - 1);
        observer.recordEvent(id++, now - 1);
        observer.recordEvent(id++, now - 1);

        assertThat(observer.calculateEventRate().getVolumePerSecond()).isEqualTo(3000, Offset.offset(100.0));
    }

    @Test
    public void singleEvent()
    {
        TurnstileObserver observer = this.turnstile.getObserver();

        long id = 1;
        observer.recordEvent(id++, this.timeProvider.getTimestamp());
        this.timeProvider.adjust(10, TimeUnit.MILLISECONDS);
        double rate = observer.calculateEventRate().getVolumePerSecond();

        assertThat(rate).isEqualTo(100, Offset.offset(0.5));
    }

    @Test
    public void windowPruning()
    {
        TurnstileObserver observer = this.turnstile.getObserver();

        this.turnstile.start();
        this.feedEvents(60, "100/s");

        assertThat(observer.getEventWindow()).hasSize(TurnstileObserver.IDEAL_SIZE);
    }

    @Test
    public void observingSkippedEvents()
    {
        TurnstileObserver observer = this.turnstile.getObserver();

        observer.observe(1);
        assertThat(observer.getEventWindow().size()).isEqualTo(1);

        observer.observe(10);
        assertThat(observer.getEventWindow().size()).isEqualTo(10);
    }

}
