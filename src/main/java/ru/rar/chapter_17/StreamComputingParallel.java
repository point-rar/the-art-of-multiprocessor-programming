package ru.rar.chapter_17;

import org.apache.commons.lang3.time.StopWatch;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.concurrent.RecursiveTask;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class StreamComputingParallel {
    static final int THRESHOLD = 2000000;

    public static void main(String[] args) {
        StopWatch stopWatch = new StopWatch();
        List<String> text = readFile("src/main/resources/document.txt");

        stopWatch.start();
        Spliterator<String> spliterator = text
                .stream()
                .spliterator();
        Map<String, Long> result = (new RecursiveWordCountTask(spliterator)).compute();
        stopWatch.stop();
        displayOutput(result);
        System.out.println(stopWatch);
    }

    private static void displayOutput(Map<String, Long> result) {
        System.out.println("result = " + result);
    }

    static List<String> readFile(String fileName) {
        try {
            Pattern pattern = Pattern.compile("\\W|\\d|_");
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            return reader
                    .lines()
                    .map(String::toLowerCase)
                    .flatMap(pattern::splitAsStream)
                    .collect(Collectors.toList());
        } catch (FileNotFoundException ex) {
            System.out.println("ex = " + ex);
        }
        return null;
    }

    static class RecursiveWordCountTask extends RecursiveTask<Map<String, Long>> {

        Spliterator<String> rightSplit;

        RecursiveWordCountTask(Spliterator<String> aSpliterator) {
            rightSplit = aSpliterator;
        }

        protected Map<String, Long> compute() {
            Map<String, Long> result = new HashMap<>();
            Spliterator<String> leftSplit;
            if (rightSplit.estimateSize() > THRESHOLD
                    && (leftSplit = rightSplit.trySplit()) != null) {
                RecursiveWordCountTask left = new RecursiveWordCountTask(leftSplit);
                RecursiveWordCountTask right = new RecursiveWordCountTask(rightSplit);
                left.fork();
                right.compute().forEach(
                        (k, v) -> result.merge(k, v, Long::sum)
                );
                left.join().forEach(
                        (k, v) -> result.merge(k, v, Long::sum)
                );
            } else {
                rightSplit.forEachRemaining(
                        word -> result.merge(word, 1L, (x, y) -> x + y)
                );
            }
            return result;
        }
    }
}