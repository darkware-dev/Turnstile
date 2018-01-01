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

import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * The {@link SystemTimeProvider} is a {@link TimeProvider} that exclusively provides the current system time,
 * accurate to the millisecond.
 *
 * @author jeff@mind-trick.net
 * @since 2018-01-01
 */
public class SystemTimeProvider implements TimeProvider
{
    @Override
    public long getTimestamp()
    {
        return System.currentTimeMillis();
    }

    @Override
    public ZonedDateTime now(final ZoneId zone)
    {
        return ZonedDateTime.now(zone);
    }
}
