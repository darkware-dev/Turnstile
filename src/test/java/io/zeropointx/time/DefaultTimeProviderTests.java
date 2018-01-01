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

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author jeff@mind-trick.net
 * @since 2018-01-01
 */
public class DefaultTimeProviderTests
{
    @Test
    public void pointlessConstructor()
    {
        // Just make sure this doesn't toss an exception
        new DefaultTimeProvider();
    }

    @Test
    public void getAndSet()
    {
        TimeProvider provider = new ManualTimeProvider();

        assertThat(DefaultTimeProvider.getDefault()).isNotSameAs(provider);
        DefaultTimeProvider.setDefaultProvider(provider);
        assertThat(DefaultTimeProvider.getDefault()).isSameAs(provider);
    }

    @Test
    public void tempTimeProvider()
    {
        final TimeProvider provider = new ManualTimeProvider();
        final TimeProvider originalProvider = DefaultTimeProvider.getDefault();
        final List<TimeProvider> tempProviderHolder = new ArrayList<>();

        DefaultTimeProvider.useTemporaryProvider(provider, p -> {
            tempProviderHolder.add(DefaultTimeProvider.getDefault());
        });

        assertThat(DefaultTimeProvider.getDefault()).isSameAs(originalProvider);
        assertThat(tempProviderHolder.get(0)).isSameAs(provider);
    }
}
