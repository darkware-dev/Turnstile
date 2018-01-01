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
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.*;

/**
 * @author jeff@mind-trick.net
 * @since 2018-01-01
 */
public abstract class BaseTimeProviderTests<T extends TimeProvider>
{
    private static TimeProvider originalDefaultProvider;

    protected T provider;

    @BeforeClass
    public static void protectCurrentDefault()
    {
        BaseTimeProviderTests.originalDefaultProvider = DefaultTimeProvider.getDefault();
    }

    @AfterClass
    public static void restoreOriginalDefault()
    {
        DefaultTimeProvider.setDefaultProvider(BaseTimeProviderTests.originalDefaultProvider);
    }

    @Before
    public void baseSetup()
    {
        this.provider = this.createProvider();
    }

    protected abstract T createProvider();

    @Test
    public void checkZDTmatchesTimestamp()
    {
        final ZoneId tz = ZoneId.systemDefault();
        final long ts = this.provider.getTimestamp();
        final ZonedDateTime zdt = this.provider.now(tz);

        assertThat(zdt.toInstant().toEpochMilli()).isCloseTo(ts, Offset.offset(10L));
    }

    @Test
    public void checkZDTusesDefaultTZ()
    {
        final ZonedDateTime zdtDefault = this.provider.now();

        assertThat(zdtDefault.getZone()).isEqualTo(ZoneId.systemDefault());
    }

    @Test
    public void checkNowDefaultCloseToDefaultNow()
    {
        assertThat(this.provider.now().toInstant().toEpochMilli()).isCloseTo(this.provider.now(ZoneId.systemDefault()).toInstant().toEpochMilli(), Offset.offset(10L));
    }
}
