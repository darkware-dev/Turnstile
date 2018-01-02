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

import com.google.common.collect.Sets;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author jeff@darkware.org
 * @since 2017-01-16
 */
@RunWith(Parameterized.class)
public class BaseObjectTests
{
    private final EqualityObjectSet<?> objectSet;

    public BaseObjectTests(final EqualityObjectSet objectSet)
    {
        super();

        this.objectSet = objectSet;
    }

    @Parameterized.Parameters
    public static Set<EqualityObjectSet<?>> getTestExamples()
    {
        return Sets.newHashSet(
                new EqualityObjectSet<>(FlowRate.class,
                                        new FlowRate(10, Duration.of(4, ChronoUnit.SECONDS)),
                                        new FlowRate(10, Duration.of(4, ChronoUnit.SECONDS)),
                                        new FlowRate(5, Duration.of(6, ChronoUnit.SECONDS)))
        );
    }

    public boolean hasOption(final BaseObjectOption option)
    {
        return this.objectSet.options.contains(option);
    }

    @Test
    public void equals_self()
    {
        assertThat(this.objectSet.base.equals(this.objectSet.base)).isTrue();
    }

    @Test
    public void equals_null()
    {
        assertThat(this.objectSet.base.equals(null)).isFalse();
    }

    @Test
    public void equals_similar()
    {
        if (this.objectSet.options.contains(BaseObjectOption.SIMILAR_ISNT_EQUAL))
        {
            assertThat(this.objectSet.base.equals(this.objectSet.equal)).isFalse();
        }
        else
        {
            assertThat(this.objectSet.base.equals(this.objectSet.equal)).isTrue();
        }
    }

    @Test
    public void equals_otherObject()
    {
        assertThat(this.objectSet.base.equals(new Object())).isFalse();
    }

    @Test
    public void hashcode_vsSimilar()
    {
        if (this.hasOption(BaseObjectOption.SIMILAR_HAS_DIFFERENT_HASHCODES))
        {
            assertThat(this.objectSet.base.hashCode()).isNotEqualTo(this.objectSet.equal.hashCode());
        }
        else
        {
            assertThat(this.objectSet.base.hashCode()).isEqualTo(this.objectSet.equal.hashCode());
        }
    }

    @Test
    public void hashcode_vsDifferent()
    {
        assertThat(this.objectSet.base.hashCode()).isNotEqualTo(this.objectSet.different.hashCode());
    }

    public enum BaseObjectOption
    {
        SIMILAR_ISNT_EQUAL,
        SIMILAR_HAS_DIFFERENT_HASHCODES,
        CLONE_ISNT_EQUAL
    }

    public static final class EqualityObjectSet<T>
    {
        protected final Class<? super T> targetClass;
        protected final T base;
        protected final T equal;
        protected final T different;
        protected final EnumSet<BaseObjectOption> options;

        public EqualityObjectSet(final Class<? super T> targetClass,
                                 final T base,
                                 final T equal,
                                 final T different,
                                 final BaseObjectOption... options)
        {
            super();

            this.targetClass = targetClass;
            this.base = base;
            this.equal = equal;
            this.different = different;

            this.options = Sets.newEnumSet(Arrays.asList(options), BaseObjectOption.class);
        }
    }

}
