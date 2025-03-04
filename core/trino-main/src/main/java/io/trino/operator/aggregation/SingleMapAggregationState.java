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
package io.trino.operator.aggregation;

import io.trino.spi.block.Block;
import io.trino.spi.block.MapBlockBuilder;
import io.trino.spi.block.SingleMapBlock;
import io.trino.spi.function.AccumulatorState;
import io.trino.spi.type.Type;

import java.lang.invoke.MethodHandle;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

public class SingleMapAggregationState
        extends AbstractMapAggregationState
{
    private SingleMapBlock tempSerializedState;

    public SingleMapAggregationState(
            Type keyType,
            MethodHandle keyReadFlat,
            MethodHandle keyWriteFlat,
            MethodHandle hashFlat,
            MethodHandle distinctFlatBlock,
            MethodHandle keyHashBlock,
            Type valueType,
            MethodHandle valueReadFlat,
            MethodHandle valueWriteFlat)
    {
        super(
                keyType,
                keyReadFlat,
                keyWriteFlat,
                hashFlat,
                distinctFlatBlock,
                keyHashBlock,
                valueType,
                valueReadFlat,
                valueWriteFlat,
                false);
    }

    private SingleMapAggregationState(SingleMapAggregationState state)
    {
        super(state);
        checkArgument(state.tempSerializedState == null, "state.tempSerializedState is not null");
        tempSerializedState = null;
    }

    @Override
    public void add(Block keyBlock, int keyPosition, Block valueBlock, int valuePosition)
    {
        add(0, keyBlock, keyPosition, valueBlock, valuePosition);
    }

    @Override
    public void writeAll(MapBlockBuilder out)
    {
        serialize(0, out);
    }

    @Override
    public AccumulatorState copy()
    {
        return new SingleMapAggregationState(this);
    }

    void setTempSerializedState(SingleMapBlock tempSerializedState)
    {
        this.tempSerializedState = tempSerializedState;
    }

    SingleMapBlock removeTempSerializedState()
    {
        SingleMapBlock block = tempSerializedState;
        checkState(block != null, "tempDeserializeBlock is null");
        tempSerializedState = null;
        return block;
    }
}
