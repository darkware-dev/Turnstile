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

/**
 * A {@link Meter} is an abstraction of a policy for restricting the frequency that any event is allowed to occur.
 *
 * @author jeff@darkware.org
 * @since 2017-05-01
 */
public abstract class Meter
{
    /**
     * Reset this meter to a freshly-initialized state.
     */
    protected abstract void reset();

    /**
     * Check to see if this meter is currently paused.
     *
     * @return {@code true} if the meter is in a paused (or never started) state, {@code false} if the meter is
     * currently started and running.
     */
    protected abstract boolean isPaused();

    /**
     * Start measuring the passage of time in this meter. Any policies which change the rate of flow through the
     * meter based on how much time has passed will see side effects because of this.
     */
    protected abstract void start();

    /**
     * Stop measuring the passage of time in this meter. The amount of time that has passed before being paused is
     * still remembered and will be taken into account for policies based on the passage of time. They will operate
     * as if the paused period did not exist.
     */
    protected abstract void pause();

    /**
     * Calculate the delay to apply to the given serial event identifer. This would be the amount of time that
     * the event should be forced to delay if it were requested to the {@link #delay(long)} method.
     * <p>
     * While this method is used by the {@link #delay(long)} method, it has no side effects in itself and can be
     * called at any time. This is particularly useful for testing.
     *
     * @param eventCount The event count (or serial event ID if you want to think of it that way).
     * @return The number of milliseconds from the current time that the event should be delayed before being
     * allowed to pass.
     */
    protected abstract long getDelayFor(final long eventCount);

    /**
     * Delay the current thread until the {@link Meter}'s policy for passing events is met. This may or may not
     * actually block the thread, and side effects of the passing may not be noticeable by the thread which called
     * the method (eg: the {@link Meter} may enforce a mandatory delay on the <em>next</em> caller).
     *
     * @param eventCount The total number of events that have been recorded by this {@link Meter}.
     * @throws InterruptedException If the thread is interrupted while being blocked to enforce the {@link Meter}'s
     * policy.
     */
    protected void delay(final long eventCount) throws InterruptedException
    {
        final long delayMillis = this.getDelayFor(eventCount);

        if (delayMillis > 0)
        {
            Thread.sleep(delayMillis);
        }
    }
}
