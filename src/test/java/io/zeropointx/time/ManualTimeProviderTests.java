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

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author jeff@mind-trick.net
 * @since 2018-01-01
 */
public class ManualTimeProviderTests extends BaseTimeProviderTests<ManualTimeProvider>
{
    @Test
    public void defaultTimestamp()
    {
        final long now = System.currentTimeMillis();

        assertThat(this.provider.getTimestamp()).isCloseTo(now, Offset.offset(10L));
    }

    @Test
    public void systemTimestamp()
    {
        final long now = System.currentTimeMillis();
        this.provider.setSystemTimestamp();

        assertThat(this.provider.getTimestamp()).isCloseTo(now, Offset.offset(10L));
    }

    @Test
    public void setLong() throws InterruptedException
    {
        final long value = 3133742;
        this.provider.setTimestamp(value);

        assertThat(this.provider.getTimestamp()).isEqualTo(value);
        Thread.sleep(20);
        assertThat(this.provider.getTimestamp()).isEqualTo(value);
    }

    @Test
    public void setZDT() throws InterruptedException
    {
        final ZonedDateTime fortyDaysAgo = ZonedDateTime.now().minus(Duration.ofDays(40));

        this.provider.setTimestamp(fortyDaysAgo);

        assertThat(this.provider.getTimestamp()).isEqualTo(fortyDaysAgo.toInstant().toEpochMilli());
        Thread.sleep(50);
        assertThat(this.provider.getTimestamp()).isEqualTo(fortyDaysAgo.toInstant().toEpochMilli());
    }

    @Test
    public void adjust() throws InterruptedException
    {
        final ZonedDateTime fortyDaysAgo = ZonedDateTime.now().minus(Duration.ofDays(40));
        final long daysAdjust = 2;

        this.provider.setTimestamp(fortyDaysAgo);

        assertThat(this.provider.getTimestamp()).isEqualTo(fortyDaysAgo.toInstant().toEpochMilli());
        this.provider.adjust(-daysAdjust, TimeUnit.DAYS);
        Thread.sleep(24);
        assertThat(this.provider.getTimestamp()).isEqualTo(fortyDaysAgo.minusDays(daysAdjust).toInstant().toEpochMilli());
    }

    @Override
    protected ManualTimeProvider createProvider()
    {
        return new ManualTimeProvider();
    }
}
