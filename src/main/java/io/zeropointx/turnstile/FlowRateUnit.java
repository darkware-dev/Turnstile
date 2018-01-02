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

package io.zeropointx.turnstile;

import java.time.temporal.ChronoUnit;

/**
 * A {@link FlowRateUnit} is an abstraction of a {@link ChronoUnit} which leverages Java's built-in {@code enum}
 * functionality to create a slightly more efficient map of unit names to functional unit instances.
 * <p>
 * I suppose the translation here is that this is a combination of a weird hack and possibly pointless optimization
 * made to achieve a simple lookup table for parsing. Meh. Wouldn't need to do this if Java supported reasonable
 * parsing for {@link ChronoUnit}s.
 *
 * @author jeff@darkware.org
 * @since 2017-04-30
 */
enum FlowRateUnit
{
    /** Milliseconds. */
    MS(ChronoUnit.MILLIS),
    /** Seconds. */
    S(ChronoUnit.SECONDS),
    /** Minutes. */
    M(ChronoUnit.MINUTES),
    /** Hours. */
    H(ChronoUnit.HOURS),
    /** Days. */
    D(ChronoUnit.DAYS),
    ;

    /** The actual {@link ChronoUnit} which is represented. */
    private final ChronoUnit realUnit;

    /**
     * Create a new {@link FlowRateUnit}.
     *
     * @param realUnit The {@link ChronoUnit} it represents.
     */
    FlowRateUnit(ChronoUnit realUnit)
    {
        this.realUnit = realUnit;
    }

    /**
     * Fetch the real {@link ChronoUnit} represented by this unit.
     *
     * @return A {@link ChronoUnit} instance.
     */
    public final ChronoUnit getChronoUnit()
    {
        return this.realUnit;
    }
}
