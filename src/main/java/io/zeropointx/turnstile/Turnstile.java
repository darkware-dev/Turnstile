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

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.StampedLock;

/**
 * A {@link Turnstile} is a rate limiting device that checks and potentially blocks thread execution in order
 * to restrict the flow of events to match a desired goal.
 *
 * @author jeff@darkware.org
 * @since 2017-05-01
 */
public class Turnstile
{
    protected final static Logger log = LoggerFactory.getLogger("Turnstile");

    private final StampedLock block;
    private Long blockId;

    private final Meter meter;
    private final TurnstileObserver observer;
    private final AtomicLong eventCount;

    /**
     * Create a new {@link Turnstile} with the configured {@link Meter}.
     *
     * @param meter An object which controls the policy over how often events pass.
     */
    public Turnstile(final Meter meter)
    {
        super();

        this.block = new StampedLock();
        this.observer = new TurnstileObserver();

        this.meter = meter;

        this.eventCount = new AtomicLong(0L);
        this.reset();
    }

    /**
     * Reset this {@link Turnstile}, returning it to a freshly initialized state. All counts for events and
     * previously active time is zeroed.
     */
    public void reset()
    {
        this.eventCount.set(0L);
        this.meter.reset();
        this.unblock();
    }

    /**
     * Fetch the number of events that have entered the {@link Turnstile}. This includes any event that might
     * currently be controlled by the restriction policy of a {@link Meter}.
     *
     * @return The number of events as a {@code long}.
     */
    public long getEventsSeen()
    {
        return this.eventCount.get();
    }

    /**
     * Fetch the observer which is recording metrics for this {@link Turnstile}.
     *
     * @return A {@link TurnstileObserver} attached to this turnstile.
     */
    public TurnstileObserver getObserver()
    {
        return this.observer;
    }

    /**
     * Start the {@link Turnstile}. Policies based on the passage of time will start to account for this in deciding
     * when to allow events to pass.
     */
    public void start()
    {
        Preconditions.checkState(this.meter.isPaused(), "Cannot start a turnstile that is already started.");

        this.meter.start();
    }

    /**
     * Pause the {@link Turnstile}. For {@link Meter}s which care about the passage of time, this may result in events
     * being blocked. Other M
     */
    public void pause()
    {
        Preconditions.checkState(!this.meter.isPaused(), "Cannot pause a turnstile that is already paused.");

        this.meter.pause();
    }

    /**
     * Clears or resets any global block on the {@link Turnstile}. This allows events to pass through. The rate or
     * number of events is still ultimately controlled by the configured {@link Meter}.
     */
    public void unblock()
    {
        synchronized (this.block)
        {
            if (this.isBlocked())
            {
                this.block.unlockWrite(this.blockId);
                this.blockId = null;
            }
        }
    }

    /**
     * Blocks the {@link Turnstile}, preventing the passage of all events. Events under action by a configured
     * {@link Meter} will handle restrictions from the {@link Meter} first, then be restricted by the block. This means
     * that events cleared by a {@link Meter} while the {@link Turnstile} is blocked will pass almost immediately
     * when the block is lifted.
     */
    public void block()
    {
        synchronized (this.block)
        {
            if (!this.isBlocked())
            {
                this.blockId = this.block.writeLock();
            }
        }
    }

    /**
     * Check to see if this {@link Turnstile} is currently blocked.
     *
     * @return {@code true} if no events will be allowed to pass the {@link Turnstile}, {@code false} if events
     * are allowed to pass normally.
     */
    public boolean isBlocked()
    {
        return this.blockId != null;
    }

    /**
     * Have the current thread attempt to pass the turnstile. If allowing the thread to
     * continue would exceed the configured rate, the thread will be blocked until the
     * rate falls within the desired limits.
     *
     * @throws InterruptedException If the thread is interrupted while being restricted.
     */
    public void pass() throws InterruptedException
    {
        synchronized (this.eventCount)
        {
            this.meter.delay(this.eventCount.incrementAndGet());

            final long checkBlock = this.block.readLock();
            this.observer.observe(this.eventCount.get());
            this.block.unlockRead(checkBlock);
        }
    }
}
