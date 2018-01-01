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

package io.zeropointx.time;

import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;

/**
 * The {@link ManualTimeProvider} is a {@link TimeProvider} which adds additional methods for taking complete
 * control over the passage of time within the provider. Unlike the expected behavior with a {@link TimeProvider},
 * the presented time will not change unless modified through one of the setter methods. This provides an
 * explicitly stable presentation of time useful for complex operations based on time or testing complex or
 * otherwise fiddly methods based on the passage of time.
 * <p>
 * In the interest of friendliness and usability, the {@link #getTimestamp()} method will adopt the current system
 * time if no timestamp has been previously set (or if the timestamp is set to a value less than zero).
 *
 * @author jeff@mind-trick.net
 * @since 2018-01-01
 */
public class ManualTimeProvider implements TimeProvider
{
    private long timestamp = -1L;

    /**
     * Set the current timestamp for the provider. This timestamp will be used for all future calls to
     * {@link #getTimestamp()} until the timestamp is set to another value.
     *
     * @param timestamp The timestamp to use, as a count of milliseconds since the UNIX Epoch.
     */
    public void setTimestamp(final long timestamp)
    {
        this.timestamp = timestamp;
    }

    /**
     * Set the timestamp to match the current system time.
     *
     * @see #setTimestamp(long)
     */
    public void setSystemTimestamp()
    {
        this.setTimestamp(System.currentTimeMillis());
    }

    /**
     * Set the timestamp to match the provided date and time.
     *
     * @param dateTime The date and time to adopt.
     * @see #setTimestamp(long)
     */
    public void setTimestamp(final ZonedDateTime dateTime)
    {
        this.setTimestamp(dateTime.toInstant().toEpochMilli());
    }

    /**
     * Adjust the current timestamp by the supplied duration of units. The supplied duration will be applied to the
     * current value of the timestamp, meaning that if the current timestamp is not set, the current time will be
     * used as the base time.
     *
     * @param duration The number of units to adjust the timestamp by
     * @param unit The unit of the supplied duration
     * @see #setTimestamp(long)
     */
    public void adjust(final long duration, final TimeUnit unit)
    {
        this.setTimestamp(this.getTimestamp() + unit.toMillis(duration));
    }

    @Override
    public long getTimestamp()
    {
        if (this.timestamp < 0L)
        {
            this.setSystemTimestamp();
        }

        return this.timestamp;
    }
}
