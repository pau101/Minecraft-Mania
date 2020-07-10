package me.paulf.minecraftmania;

import com.google.common.base.CharMatcher;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class TempMain {
    public static void main(final String[] args) throws IOException {
        final Set<String> strings = Files.readAllLines(Paths.get("D:\\Text\\badwords.txt")).stream()
            .map(String::trim)
            .map(s -> s.toLowerCase(Locale.ROOT).replaceAll("[*-;]", ""))
            .filter(s -> s.length() > 2 && s.length() <= 4 && CharMatcher.javaLetter().matchesAllOf(s))
            .collect(Collectors.toCollection(TreeSet::new));
        strings.add("nig");
        System.out.println(strings.size());
        final Iterator<String> it = strings.iterator();
        while (it.hasNext()) {
            final String str = it.next();
            for (final String s : strings) {
                if (s.length() < str.length() && str.contains(s)) {
                    it.remove();
                    break;
                }
            }
        }
        System.out.println(strings.size());
        try (final PrintStream out = new PrintStream(Files.newOutputStream(Paths.get("src\\main\\resources\\assets\\minecraftmania\\texts\\word_blacklist.txt").toAbsolutePath()))) {
            for (final String s : strings) {
                out.println(s);
            }
        }
    }
}
