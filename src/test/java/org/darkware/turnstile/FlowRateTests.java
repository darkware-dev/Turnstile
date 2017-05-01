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
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.*;

/**
 * @author jeff@darkware.org
 * @since 2017-04-30
 */
public class FlowRateTests
{
    @Test
    public void simple_create()
    {
        final FlowRate rate = new FlowRate(1, Duration.of(1, ChronoUnit.SECONDS));

        assertThat(rate).isNotNull();
        assertThat(rate.getVolume()).isEqualTo(1.0);
        assertThat(rate.getDuration()).isEqualTo(Duration.of(1, ChronoUnit.SECONDS));
    }

    @Test
    public void parse_superCorrect()
    {
        final FlowRate rate = new FlowRate("5U/20s");

        assertThat(rate.getVolume()).isEqualTo(5.0);
        assertThat(rate.getDuration()).isEqualTo(Duration.of(20, ChronoUnit.SECONDS));
    }

    @Test
    public void parse_noFactor()
    {
        final FlowRate rate = new FlowRate("5/20s");

        assertThat(rate.getVolume()).isEqualTo(5.0);
        assertThat(rate.getDuration()).isEqualTo(Duration.of(20, ChronoUnit.SECONDS));
    }

    @Test
    public void parse_floatVolume()
    {
        final FlowRate rate = new FlowRate("5.2/20s");

        assertThat(rate.getVolume()).isEqualTo(5.2, Offset.offset(0.01));
        assertThat(rate.getDuration()).isEqualTo(Duration.of(20, ChronoUnit.SECONDS));
    }

    @Test
    public void parse_floatVolumeWithFactor()
    {
        final FlowRate rate = new FlowRate("5.2M/20s");

        assertThat(rate.getVolume()).isEqualTo(5_200_000.0, Offset.offset(0.2));
        assertThat(rate.getDuration()).isEqualTo(Duration.of(20, ChronoUnit.SECONDS));
    }

    @Test
    public void parse_withFactor()
    {
        final FlowRate rate = new FlowRate("5K/20s");

        assertThat(rate.getVolume()).isEqualTo(5000.0);
        assertThat(rate.getDuration()).isEqualTo(Duration.of(20, ChronoUnit.SECONDS));
    }

    @Test
    public void parse_withLowercaseFactor()
    {
        final FlowRate rate = new FlowRate("5k/20s");

        assertThat(rate.getVolume()).isEqualTo(5000.0);
        assertThat(rate.getDuration()).isEqualTo(Duration.of(20, ChronoUnit.SECONDS));
    }

    @Test
    public void parse_unitOnly()
    {
        final FlowRate rate = new FlowRate("5U/s");

        assertThat(rate.getVolume()).isEqualTo(5.0);
        assertThat(rate.getDuration()).isEqualTo(Duration.of(1, ChronoUnit.SECONDS));
    }

    @Test
    public void parseFail_badFactor()
    {
        assertThatThrownBy(() -> new FlowRate("5Z/1s")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void parseFail_badUnit()
    {
        assertThatThrownBy(() -> new FlowRate("5/1z")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void parseFail_badCount()
    {
        assertThatThrownBy(() -> new FlowRate("A/1s")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void parseFail_badDurationCount()
    {
        assertThatThrownBy(() -> new FlowRate("1/1.2s")).isInstanceOf(IllegalArgumentException.class);
    }
}
