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

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * A {@link TimeProvider} is an abstraction for a number of common methods used to determine the current date or
 * time withing Java. While this interface is useful in itself for simplifying the process of finding the current
 * date or time in environments crossing timezones, the more important capability is abstracting the capability to
 * tie the view of the current time to some external controller. This allows another object to act as a
 * {@link TimeProvider} for a given object and modify its view of time. This can be used to "freeze" time within
 * some cross-object operation or to freely manipulate time for testing.
 *
 * @author jeff@mind-trick.net
 * @since 2018-01-01
 */
public interface TimeProvider
{
    /**
     * Fetch the current time as the count of milliseconds since the UNIX Epoch. This matches the conventions
     * used for {@link System#currentTimeMillis()}.
     *
     * @return The current provider timestamp.
     */
    long getTimestamp();

    /**
     * Fetch the current date and time in the provided timezone specifier. The current time is based on the value
     * supplied by {@link #getTimestamp()}.
     *
     * @param zone The timezone to use for resolving the date and time.
     * @return The current date and time as a {@link ZonedDateTime}.
     * @see #getTimestamp()
     */
    default ZonedDateTime now(final ZoneId zone)
    {
        return Instant.ofEpochMilli(this.getTimestamp()).atZone(zone);
    }

    /**
     * Fetch the current date and time in the system default timezone.
     * <p>
     * <em>Beware:</em> This method uses {@link ZoneId#systemDefault()} to resolve the default timezone, and this
     * is often configured on an operating-system level. The system timezone can vary between hosts in ways that are
     * not immediately visible. Take care when relying on this to provide uniform results across multiple hosts. The
     * most common failing is for tests to pass on local development hosts (which are often configured to use the
     * local timezone) but fail on production hosts (which may be configured to use UTC).
     *
     * @return The current date and time as a {@link ZonedDateTime}.
     * @see #getTimestamp()
     * @see #now(ZoneId)
     */
    default ZonedDateTime now()
    {
        return this.now(ZoneId.systemDefault());
    }

}
