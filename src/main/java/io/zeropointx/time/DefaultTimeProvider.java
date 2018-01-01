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

/**
 * This is a simple facade to manage a system default {@link TimeProvider} for use by other objects. This is done
 * to simplify the sharing of {@link TimeProvider}s across objects for those who don't want to set up a full
 * dependency injection system.
 *
 * @author jeff@mind-trick.net
 * @since 2018-01-01
 */
public class DefaultTimeProvider
{
    private static TimeProvider defaultProvider = new SystemTimeProvider();

    /**
     * Fetch the current system default {@link TimeProvider}.
     *
     * @return The declared default {@link TimeProvider}.
     */
    public static TimeProvider getDefault()
    {
        return DefaultTimeProvider.defaultProvider;
    }

    /**
     * Set the {@link TimeProvider} to use for future requests for the default provider. Setting the provider
     * <em>will not</em> have any effect on any objects previously fetched.
     *
     * @param provider
     */
    public static void setDefaultProvider(final TimeProvider provider)
    {
        DefaultTimeProvider.defaultProvider = provider;
    }
}
