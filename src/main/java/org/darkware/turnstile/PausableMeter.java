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

/**
 * @author jeff@darkware.org
 * @since 2017-05-01
 */
public abstract class PausableMeter extends Meter
{
    private long previousElapsed;
    private long lastStart;
    private boolean paused;

    @Override
    protected void reset()
    {
        this.previousElapsed = 0L;
        this.lastStart = -1L;
        this.paused = true;
    }

    @Override
    protected boolean isPaused()
    {
        return this.paused;
    }

    @Override
    protected void start()
    {
        this.lastStart = System.currentTimeMillis();
        this.paused = false;
    }

    @Override
    protected void pause()
    {
        this.previousElapsed += this.getElapsedTime();
        this.lastStart = -1;
        this.paused = true;
    }

    /**
     * Fetch the amount of time that has elapsed before the most recent pause (if any).
     *
     * @return The amount of time in milliseconds.
     */
    protected long getPreviousElapsedTime()
    {
        return this.previousElapsed;
    }

    /**
     * Fetch the amount of time that has passed since the most recent start.
     *
     * @return The amount of time in milliseconds.
     */
    protected long getElapsedTime()
    {
        if (this.isPaused()) return 0L;

        return System.currentTimeMillis() - this.lastStart;
    }
}
