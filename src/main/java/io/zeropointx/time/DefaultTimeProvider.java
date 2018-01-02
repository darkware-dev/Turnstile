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

import java.util.concurrent.locks.StampedLock;
import java.util.function.Consumer;

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
    private static final StampedLock lock = new StampedLock();

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
     * @param provider The {@link TimeProvider} to adopt as the system default.
     */
    public static void setDefaultProvider(final TimeProvider provider)
    {
        final long lockId = DefaultTimeProvider.lock.writeLock();
        try
        {
            DefaultTimeProvider.defaultProvider = provider;
        }
        finally
        {
            DefaultTimeProvider.lock.unlockWrite(lockId);
        }
    }

    /**
     * Use a temporary {@link TimeProvider} for the supplied action. This will set the system default to the supplied
     * provider for the duration of the action while blocking any attempts to change the system default. This is
     * primarily useful for testing, but some highly specialized actions might find use for it.
     * <p>
     * <em>Note:</em> During the time the action is running, other threads that retrieve the default provider may get
     * the temporary provider in use. This is halfway by design, as it will permit multi-threaded actions, however it
     * poses a risk to concurrent applications.
     * <p>
     * <em>Note:</em> Since attempts to change the provider while this action is running, there are risks to
     * application performance if other code attempts to change the provider during this method's execution. Other
     * attempts to change the default provider will result in the thread being blocked. As yet, there is no way to
     * detect and avoid this situation. Use this capability with care.
     *
     * @param provider The temporary {@link TimeProvider} to use.
     * @param action The action to perform, as a {@link Consumer}. The action takes the supplied {@link TimeProvider}
     * as its supplied parameter.
     */
    public static void useTemporaryProvider(final TimeProvider provider, Consumer<TimeProvider> action)
    {
        final long lockId = DefaultTimeProvider.lock.writeLock();
        TimeProvider original = null;
        try
        {
            original = DefaultTimeProvider.defaultProvider;
            DefaultTimeProvider.defaultProvider = provider;

            action.accept(provider);
        }
        finally
        {
            if (original != null) DefaultTimeProvider.defaultProvider = original;
            DefaultTimeProvider.lock.unlockWrite(lockId);
        }
    }
}
