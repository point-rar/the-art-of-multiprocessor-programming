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
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class StreamComputing {

    public static void main(String[] args) {
        List<String> text = readFile("src/main/resources/document.txt");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        Map<String, Long> map = text
                .stream()
                .collect(
                        Collectors.groupingBy(
                                Function.identity(),
                                Collectors.counting()
                        )
                );
        stopWatch.stop();
        displayOutput(map);
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
                    .toList();
        } catch (FileNotFoundException ex) {
            System.out.println("ex = " + ex);
        }
        return null;
    }
}