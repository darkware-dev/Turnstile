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

import org.assertj.core.data.Offset;
import org.junit.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author jeff@darkware.org
 * @since 2017-05-01
 */
public class RateControlledMeterTests
{
    @Test
    public void simple_demo() throws InterruptedException
    {
        final RateControlledMeter meter = new RateControlledMeter(new FlowRate("5/100ms"));

        LocalDateTime start = LocalDateTime.now();
        meter.start();

        // Send 20 events
        for (int i = 1; i <= 20; i++)
        {
            meter.delay(i);
        }
        LocalDateTime end = LocalDateTime.now();
        Duration elapsed = Duration.between(start, end);

        assertThat(elapsed.toMillis()).isCloseTo(410L, Offset.offset(20L));
    }

    @Test
    public void simple_rate() throws InterruptedException
    {
        final RateControlledMeter meter = new RateControlledMeter(new FlowRate("3/300ms"));

        assertThat(meter.getDelayFor(1)).isEqualTo(100L);
        assertThat(meter.getDelayFor(2)).isEqualTo(200L);
        assertThat(meter.getDelayFor(3)).isEqualTo(300L);
    }

    @Test
    public void submilli_rate() throws InterruptedException
    {
        final RateControlledMeter meter = new RateControlledMeter(new FlowRate("400/ms"));

        assertThat(meter.getDelayFor(100)).isEqualTo(0L);
        assertThat(meter.getDelayFor(300)).isEqualTo(1L);
    }


}
