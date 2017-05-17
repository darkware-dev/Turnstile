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
public class RateControlledMeter extends Meter
{
    /** The rate that events can flow through the meter. */
    private final FlowRate rate;
    private final double millisPerEvent;

    /**
     * Create a new {@link RateControlledMeter} that limits events based on the given rate.
     *
     * @param rate The {@link FlowRate} to restrict events to.
     */
    public RateControlledMeter(final FlowRate rate)
    {
        super();

        this.rate = rate;
        this.millisPerEvent = this.rate.getDuration().toMillis() / this.rate.getVolume();

        this.reset();
    }

    /**
     * Create a new {@link RateControlledMeter} that limits events based on the given rate.
     *
     * @param rateDescription A {@link String} describing a rate, in the format accepted by {@link FlowRate}.
     * @see FlowRate#FlowRate(CharSequence)
     */
    public RateControlledMeter(final CharSequence rateDescription)
    {
        this(new FlowRate(rateDescription));
    }

    @Override
    protected long getDelayFor(final long eventCount)
    {
        long totalMillis = this.getPreviousElapsedTime() + this.getElapsedTime();
        long targetMillis = Math.round(eventCount * this.millisPerEvent);

        return targetMillis - totalMillis;
    }
}
