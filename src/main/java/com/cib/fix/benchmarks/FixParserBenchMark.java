package com.cib.fix.benchmarks;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import com.cib.fix.parser.FixMessageParser;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
public class FixParserBenchMark {

    @State(Scope.Thread)
    public static class BenchmarkState {
        byte[] smallFixMsg;    // 4 fields
        byte[] mediumFixMsg;   // 15 fields
        byte[] largeFixMsg;    // 50 fields
        byte[] hugeFixMsg;     // 200 fields

        @Setup
        public void setup() {
            // Small message (typical order)
            smallFixMsg = "8=FIX.4.4\u000135=D\u000155=ABC\u000110=123\u0001".getBytes();

            // Medium message (typical market data)
            mediumFixMsg = ("8=FIX.4.4\u00019=122\u000135=X\u000134=8\u000149=BUYSIDE\u0001" +
                          "56=SELLSIDE\u000152=20200101-10:00:00\u000155=IBM\u0001" +
                          "54=1\u000138=1000\u000140=2\u000144=150.25\u000159=0\u000110=222\u0001").getBytes();

            // Large message (complex order)
            StringBuilder large = new StringBuilder();
            for (int i = 1; i <= 50; i++) {
                large.append(i).append("=FIELD").append(i).append("\u0001");
            }
            largeFixMsg = large.toString().getBytes();

            // Huge message (worst case)
            StringBuilder huge = new StringBuilder();
            for (int i = 1; i <= 200; i++) {
                huge.append(i).append("=FIELD").append(i).append("\u0001");
            }
            hugeFixMsg = huge.toString().getBytes();

        }
    }

    // Throughput Tests
    @Benchmark
    public void parseSmallMessage(BenchmarkState state, Blackhole bh) {
    	FixMessageParser.parse(state.smallFixMsg, (tag, msg, start, end) -> {
            bh.consume(tag);
            bh.consume(msg);
            bh.consume(start);
            bh.consume(end);
        });
    }

    @Benchmark
    public void parseMediumMessage(BenchmarkState state, Blackhole bh) {
    	FixMessageParser.parse(state.mediumFixMsg, (tag, msg, start, end) -> {
            bh.consume(tag);
            bh.consume(msg);
            bh.consume(start);
            bh.consume(end);
        });
    }

    @Benchmark
    public void parseLargeMessage(BenchmarkState state, Blackhole bh) {
    	FixMessageParser.parse(state.largeFixMsg, (tag, msg, start, end) -> {
            bh.consume(tag);
            bh.consume(msg);
            bh.consume(start);
            bh.consume(end);
        });
    }

    // Memory Allocation Tests
    @Benchmark
    @Measurement(iterations = 10, time = 1)
    @Fork(value = 1, jvmArgs = {"-Xmx256m", "-XX:+PrintGCDetails"})
    public void allocationTestSmallMessage(BenchmarkState state) {
    	FixMessageParser.parse(state.smallFixMsg, (tag, msg, start, end) -> {
            // Intentional allocation
            String value = new String(msg, start, end - start);
        });
    }

    @Benchmark
    @Measurement(iterations = 10, time = 1)
    @Fork(value = 1, jvmArgs = {"-Xmx256m", "-XX:+PrintGCDetails"})
    public void allocationTestHugeMessage(BenchmarkState state) {
    	FixMessageParser.parse(state.hugeFixMsg, (tag, msg, start, end) -> {
            // Intentional allocation
            String value = new String(msg, start, end - start);
        });
    }

    // Memory Efficiency Tests
    @Benchmark
    @Fork(value = 1, jvmArgs = {"-Xmx256m", "-XX:+UnlockDiagnosticVMOptions", 
                                "-XX:+PrintCompilation", "-XX:+PrintAssembly"})
    public void memoryEfficiencyTest(BenchmarkState state) {
    	FixMessageParser.FixMap map = FixMessageParser.parseMessage(state.mediumFixMsg);
        byte[] value = map.get(55); // IBM
        if (value.length != 3) throw new AssertionError();
    }


    // Throughput vs. Memory Tradeoff
    @Benchmark
    @Fork(value = 1, jvmArgs = {"-Xmx256m", "-XX:+UseSerialGC"})
    public void throughputMemoryTradeoff(BenchmarkState state) {
    	FixMessageParser.parse(state.largeFixMsg, (tag, msg, start, end) -> {
            // Simulate real processing with some allocations
            if (tag == 55) {
                String symbol = new String(msg, start, end - start);
            }
        });
    }
}