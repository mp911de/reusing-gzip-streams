Reusable GZIP Streams
======================

This repository demonstrates the use of a pooled GZIP OutputStream. The initial 
motivation were performance measurements of the GZIP'ing inside of [logstash-gelf](https://github.com/mp911de/logstash-gelf).

Using new instances of `GZIPOutputStream` is costly and allocates a bunch of objects (byte buffer, `Deflater`) and more.

A reusable `GZIPOutputStream` differs from the JDK-provided class in some points:

1. Do not write the GZIP header upon instance creation but expose a `writeHeader()` method
2. Expose a `reset()` method to reset the deflater state
3. Do not close the stream to keep it reusable

A design pattern of Java streams is that the target requires being set upon construction. That's a limitation
for reusing but can be mitigated by constructing the pooled `OutputStream` instance with a custom `OutputStream`
that allows you to switch the target.

Running a benchmark with the unpooled and pooled streams speaks for itself. The reduced garbage per compression run also
affects the deviation in a positive way.

```
Benchmark                                  Mode  Cnt       Score       Error  Units
GelfMessageAssemblerPerf.compressPooled    avgt    5   18164,796 ±  5717,793  ns/op
GelfMessageAssemblerPerf.compressUnpooled  avgt    5  184431,045 ± 46292,939  ns/op
```

Running the code
-----------------

The code requires Maven and contains the JMH benchmark execution. Run 

```
$ mvn clean verify
```

to build the project and the JMH benchmark.