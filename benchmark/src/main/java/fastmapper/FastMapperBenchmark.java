/*
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package fastmapper;

import net.sf.cglib.core.DebuggingClassWriter;
import org.apache.commons.beanutils.BeanUtils;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * @author iefan
 * @version 1.0
 */
@Fork(2)
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 2, time = 10)
@Measurement(iterations = 2, time = 10)
@State(Scope.Benchmark)
public class FastMapperBenchmark {

    private static final Map<String, Object> smallMap = SmallObject.map();

    private static final Map<String, Object> largeMap = LargeObject.map();

    private static final FastMapper smallObjectMapper = FastMapper.create(SmallObject.class);

    private static final FastMapper largeObjectMapper = FastMapper.create(LargeObject.class);

    @Benchmark
    public void testFastMapper4SmallObject() {
        smallObjectMapper.populate(smallMap, new SmallObject());
    }

    @Benchmark
    public void testFastMapper4LargeObject() {
        largeObjectMapper.populate(largeMap, new LargeObject());
    }

    @Benchmark
    public void testBeanUtil4SmallObject() throws InvocationTargetException, IllegalAccessException {
        BeanUtils.populate(new SmallObject(), smallMap);
    }

    @Benchmark
    public void testBeanUtil4LargeObject() throws InvocationTargetException, IllegalAccessException {
        BeanUtils.populate(new LargeObject(), largeMap);
    }

    public static void main(String[] args) throws RunnerException {
        System.setProperty(DebuggingClassWriter.DEBUG_LOCATION_PROPERTY, "./cglib-classes");
        Options options = new OptionsBuilder()
                .include(FastMapperBenchmark.class.getSimpleName())
                .syncIterations(true)
                .build();
        new Runner(options).run();
    }

}
