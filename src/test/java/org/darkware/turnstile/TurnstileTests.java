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

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author jeff@darkware.org
 * @since 2017-05-01
 */
public class TurnstileTests
{
    private Meter testMeter = Mockito.mock(Meter.class);

    @Before
    public void setup()
    {
        Mockito.reset(this.testMeter);
    }

    @Test
    public void simple()
    {
        final Turnstile limiter = new Turnstile(new NoopMeter());

        assertThat(limiter.getEventsSeen()).isZero();
    }

    @Test
    public void start()
    {
        Mockito.when(this.testMeter.isPaused()).thenReturn(true);
        final Turnstile limiter = new Turnstile(this.testMeter);

        limiter.start();

        Mockito.verify(this.testMeter).start();
    }

    @Test
    public void pause()
    {
        final Turnstile limiter = new Turnstile(this.testMeter);

        limiter.pause();

        Mockito.verify(this.testMeter).pause();
    }

    @Test
    public void delay() throws InterruptedException
    {
        final Turnstile limiter = new Turnstile(this.testMeter);

        limiter.pass();

        Mockito.verify(this.testMeter).delay(1);
    }

    @Test
    public void block()
    {
        final Turnstile limiter = new Turnstile(this.testMeter);

        limiter.block();

        assertThat(limiter.isBlocked()).isTrue();

        limiter.unblock();

        assertThat(limiter.isBlocked()).isFalse();
    }

    @Test
    public void block_otherThread() throws BrokenBarrierException, InterruptedException
    {
        final Turnstile limiter = new Turnstile(this.testMeter);
        final CyclicBarrier syncLock = new CyclicBarrier(2);

        limiter.block();

        assertThat(limiter.isBlocked()).isTrue();

        Thread unblocker = new Thread(() ->
                                      {
                                          limiter.unblock();
                                          try
                                          {
                                              syncLock.await();
                                          }
                                          catch (Exception e)
                                          {
                                              e.printStackTrace();
                                          }
                                      });

        unblocker.start();
        syncLock.await();

        assertThat(limiter.isBlocked()).isFalse();
    }
}
