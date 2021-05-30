# fastmapper
A method to convert map to bean, based on Cglib.

## benchmark
```text
# JMH version: 1.23
# VM version: JDK 11.0.2, OpenJDK 64-Bit Server VM, 11.0.2+9
# VM invoker: C:\Java\jdk-11.0.2\bin\java.exe
# VM options: -Dfile.encoding=UTF-8 -Duser.country=CN -Duser.language=zh -Duser.variant
# Warmup: 2 iterations, 10 s each
# Measurement: 2 iterations, 10 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: fastmapper.FastMapperBenchmark.testBeanUtil4LargeObject

...

Benchmark                                        Mode  Cnt         Score        Error  Units
FastMapperBenchmark.testBeanUtil4LargeObject    thrpt    4     45764.781 ±   4024.908  ops/s
FastMapperBenchmark.testBeanUtil4SmallObject    thrpt    4    807440.887 ±  21860.054  ops/s
FastMapperBenchmark.testFastMapper4LargeObject  thrpt    4   1687758.612 ± 138851.962  ops/s
FastMapperBenchmark.testFastMapper4SmallObject  thrpt    4  18603801.498 ± 773943.098  ops/s
```
