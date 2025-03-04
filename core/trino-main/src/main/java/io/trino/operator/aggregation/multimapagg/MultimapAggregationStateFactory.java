/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.trino.operator.aggregation.multimapagg;

import io.trino.spi.function.AccumulatorStateFactory;
import io.trino.spi.function.Convention;
import io.trino.spi.function.OperatorDependency;
import io.trino.spi.function.OperatorType;
import io.trino.spi.function.TypeParameter;
import io.trino.spi.type.Type;

import java.lang.invoke.MethodHandle;

import static io.trino.spi.function.InvocationConvention.InvocationArgumentConvention.BLOCK_POSITION_NOT_NULL;
import static io.trino.spi.function.InvocationConvention.InvocationArgumentConvention.FLAT;
import static io.trino.spi.function.InvocationConvention.InvocationReturnConvention.BLOCK_BUILDER;
import static io.trino.spi.function.InvocationConvention.InvocationReturnConvention.FAIL_ON_NULL;
import static io.trino.spi.function.InvocationConvention.InvocationReturnConvention.FLAT_RETURN;
import static java.util.Objects.requireNonNull;

public class MultimapAggregationStateFactory
        implements AccumulatorStateFactory<MultimapAggregationState>
{
    private final Type keyType;
    private final MethodHandle keyReadFlat;
    private final MethodHandle keyWriteFlat;
    private final MethodHandle keyHashFlat;
    private final MethodHandle keyDistinctFlatBlock;
    private final MethodHandle keyHashBlock;

    private final Type valueType;
    private final MethodHandle valueReadFlat;
    private final MethodHandle valueWriteFlat;

    public MultimapAggregationStateFactory(
            @TypeParameter("K") Type keyType,
            @OperatorDependency(
                    operator = OperatorType.READ_VALUE,
                    argumentTypes = "K",
                    convention = @Convention(arguments = FLAT, result = BLOCK_BUILDER)) MethodHandle keyReadFlat,
            @OperatorDependency(
                    operator = OperatorType.READ_VALUE,
                    argumentTypes = "K",
                    convention = @Convention(arguments = BLOCK_POSITION_NOT_NULL, result = FLAT_RETURN)) MethodHandle keyWriteFlat,
            @OperatorDependency(
                    operator = OperatorType.HASH_CODE,
                    argumentTypes = "K",
                    convention = @Convention(arguments = FLAT, result = FAIL_ON_NULL)) MethodHandle keyHashFlat,
            @OperatorDependency(
                    operator = OperatorType.IS_DISTINCT_FROM,
                    argumentTypes = {"K", "K"},
                    convention = @Convention(arguments = {FLAT, BLOCK_POSITION_NOT_NULL}, result = FAIL_ON_NULL)) MethodHandle keyDistinctFlatBlock,
            @OperatorDependency(
                    operator = OperatorType.HASH_CODE,
                    argumentTypes = "K",
                    convention = @Convention(arguments = BLOCK_POSITION_NOT_NULL, result = FAIL_ON_NULL)) MethodHandle keyHashBlock,
            @TypeParameter("V") Type valueType,
            @OperatorDependency(
                    operator = OperatorType.READ_VALUE,
                    argumentTypes = "V",
                    convention = @Convention(arguments = FLAT, result = BLOCK_BUILDER)) MethodHandle valueReadFlat,
            @OperatorDependency(
                    operator = OperatorType.READ_VALUE,
                    argumentTypes = "V",
                    convention = @Convention(arguments = BLOCK_POSITION_NOT_NULL, result = FLAT_RETURN)) MethodHandle valueWriteFlat)
    {
        this.keyType = requireNonNull(keyType, "keyType is null");
        this.keyReadFlat = requireNonNull(keyReadFlat, "keyReadFlat is null");
        this.keyWriteFlat = requireNonNull(keyWriteFlat, "keyWriteFlat is null");
        this.keyHashFlat = requireNonNull(keyHashFlat, "keyHashFlat is null");
        this.keyDistinctFlatBlock = requireNonNull(keyDistinctFlatBlock, "keyDistinctFlatBlock is null");
        this.keyHashBlock = requireNonNull(keyHashBlock, "keyHashBlock is null");

        this.valueType = requireNonNull(valueType, "valueType is null");
        this.valueReadFlat = requireNonNull(valueReadFlat, "valueReadFlat is null");
        this.valueWriteFlat = requireNonNull(valueWriteFlat, "valueWriteFlat is null");
    }

    @Override
    public MultimapAggregationState createSingleState()
    {
        return new SingleMultimapAggregationState(keyType, keyReadFlat, keyWriteFlat, keyHashFlat, keyDistinctFlatBlock, keyHashBlock, valueType, valueReadFlat, valueWriteFlat);
    }

    @Override
    public MultimapAggregationState createGroupedState()
    {
        return new GroupedMultimapAggregationState(keyType, keyReadFlat, keyWriteFlat, keyHashFlat, keyDistinctFlatBlock, keyHashBlock, valueType, valueReadFlat, valueWriteFlat);
    }
}
