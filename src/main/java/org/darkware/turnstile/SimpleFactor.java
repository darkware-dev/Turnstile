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

import java.util.Optional;

/**
 * @author jeff@darkware.org
 * @since 2017-04-30
 */
public enum SimpleFactor
{
    /** Unit (just one... to help support cases where you need a factor but don't want one. */
    U(1L),
    /** A thousand. */
    K(1000L),
    /** A million. */
    M(1_000_000L),
    ;

    /**
     * Parse the supplied text as a {@link SimpleFactor}.
     *
     * @param text The text to parse.
     * @return A {@link SimpleFactor} matching the supplied text, or {@link SimpleFactor#U} if the
     * text is {@code null} or zero-length.
     */
    public static SimpleFactor parse(final String text)
    {
        if (text == null || text.length() < 1) return SimpleFactor.U;

        return Optional.ofNullable(SimpleFactor.valueOf(text.toUpperCase()))
                .orElseThrow(() -> new IllegalArgumentException("Unidentified multiplier: " + text));
    }

    /** The multiplier for this factor. */
    private final long multiplier;

    SimpleFactor(final long multiplier)
    {
        this.multiplier = multiplier;
    }

    /**
     * Fetch the multiplier for this factor.
     *
     * @return The multiplier as a {@code long}.
     */
    public long getMultiplier()
    {
        return this.multiplier;
    }

    /**
     * Apply the factor to the supplied value.
     *
     * @param baseValue The base value to multiply.
     * @return A new value, multiplied by the associated multiplier.
     */
    public double apply(final double baseValue)
    {
        return baseValue * (double)this.multiplier;
    }

    /**
     * Apply the factor to the supplied value.
     *
     * @param baseValue The base value to multiply.
     * @return A new value, multiplied by the associated multiplier.
     */
    public long apply(final long baseValue)
    {
        return baseValue * this.multiplier;
    }
}
