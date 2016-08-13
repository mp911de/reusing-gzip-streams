/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package biz.paluch;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPOutputStream;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

/**
 * @author Mark Paluch
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class GelfMessageAssemblerPerf {

    @State(Scope.Thread)
    public static class Input {

        public byte[] bytes;
        public OutputStream devNull = new OutputStream() {

            @Override
            public void write(int b) throws IOException {

            }

            @Override
            public void write(byte[] b) throws IOException {
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
            }
        };

        @Setup
        public void setup(final Blackhole bh) {

            StringWriter writer = new StringWriter();

            new Exception().printStackTrace(new PrintWriter(writer));
            this.bytes = writer.getBuffer().toString().getBytes();
        }
    }

    @Benchmark
    public void compressUnpooled(Input input) throws IOException {

        GZIPOutputStream out = new GZIPOutputStream(input.devNull);
        out.write(input.bytes);
        out.finish();

    }

    @Benchmark
    public void compressPooled(Input input) throws IOException {

        ReusableGzipOutputStream out = GzipPool.forStream(input.devNull);
        out.writeHeader();
        out.write(input.bytes);
        out.finish();

		out.reset();

    }

    public static void main(String[] args) throws IOException {

        GelfMessageAssemblerPerf perf = new GelfMessageAssemblerPerf();
        Input input = new Input();
        input.setup(null);

        perf.compressUnpooled(input);
        perf.compressPooled(input);
    }
}
