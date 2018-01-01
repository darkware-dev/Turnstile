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

import org.assertj.core.data.Offset;
import org.junit.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author jeff@mind-trick.net
 * @since 2018-01-01
 */
public class SystemTimeProviderTests extends BaseTimeProviderTests<SystemTimeProvider>
{
    @Test
    public void systemTime()
    {
        final long now = System.currentTimeMillis();

        assertThat(this.provider.getTimestamp()).isCloseTo(now, Offset.offset(10L));
    }

    @Test
    public void systemZDT()
    {
        final ZoneId tz = ZoneId.systemDefault();
        final ZonedDateTime now = ZonedDateTime.now(tz);

        final ZonedDateTime providerNow = this.provider.now(tz);

        assertThat(now.toInstant().toEpochMilli()).isCloseTo(providerNow.toInstant().toEpochMilli(), Offset.offset(10L));
    }

    @Override
    protected SystemTimeProvider createProvider()
    {
        return new SystemTimeProvider();
    }
}
