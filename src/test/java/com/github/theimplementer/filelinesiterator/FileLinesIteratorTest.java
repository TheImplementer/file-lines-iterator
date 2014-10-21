package com.github.theimplementer.filelinesiterator;

import org.junit.Test;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.github.theimplementer.filelinesiterator.Line.line;
import static java.nio.file.Files.createTempFile;
import static java.util.Spliterator.*;
import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.StreamSupport.stream;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class FileLinesIteratorTest {

    @Test
    public void theIteratorShouldHandleEmptyFilesCorrectly() throws IOException {
        final Path emptyFile = createTempFile("test", "");

        final FileLinesIterator fileLinesIterator = new FileLinesIterator(emptyFile.toFile());

        assertThat(fileLinesIterator.hasNext(), is(false));
    }

    @Test(expected = NoSuchElementException.class)
    public void shouldThrowAnExceptionWhenThereAreNoMoreElements() throws IOException {
        final Path emptyFile = createTempFile("test", "");

        final FileLinesIterator fileLinesIterator = new FileLinesIterator(emptyFile.toFile());
        fileLinesIterator.next();
    }

    @Test
    public void shouldReturnTheCorrectLineIfTheFileHasJustOneLine() throws IOException {
        final Path emptyFile = createTempFile("test", "");
        final PrintWriter printWriter = new PrintWriter(emptyFile.toFile());
        printWriter.print("line");
        printWriter.close();

        final FileLinesIterator fileLinesIterator = new FileLinesIterator(emptyFile.toFile());

        assertThat(fileLinesIterator.hasNext(), is(true));
        assertThat(fileLinesIterator.next(), is(line("line", 4)));
    }

    @Test
    public void shouldReturnTheCorrectLineIfTheLineHasADelimiter() throws IOException {
        final Path emptyFile = createTempFile("test", "");
        final PrintWriter printWriter = new PrintWriter(emptyFile.toFile());
        printWriter.print("line\n");
        printWriter.close();

        final FileLinesIterator fileLinesIterator = new FileLinesIterator(emptyFile.toFile());

        assertThat(fileLinesIterator.hasNext(), is(true));
        assertThat(fileLinesIterator.next(), is(line("line", 5)));
    }

    @Test
    public void shouldReturnTheCorrectLines() throws IOException {
        final Path emptyFile = createTempFile("test", "");
        final PrintWriter printWriter = new PrintWriter(emptyFile.toFile());
        printWriter.println("line 1");
        printWriter.println("line 2");
        printWriter.close();

        final FileLinesIterator fileLinesIterator = new FileLinesIterator(emptyFile.toFile());

        assertThat(fileLinesIterator.hasNext(), is(true));
        assertThat(fileLinesIterator.next(), is(line("line 1", 7)));
        assertThat(fileLinesIterator.hasNext(), is(true));
        assertThat(fileLinesIterator.next(), is(line("line 2", 7)));
    }

    @Test
    public void shouldHandleEmptyLinesCorrectly() throws IOException {
        final Path emptyFile = createTempFile("test", "");
        final PrintWriter printWriter = new PrintWriter(emptyFile.toFile());
        printWriter.println("line 1");
        printWriter.println("");
        printWriter.close();

        final FileLinesIterator fileLinesIterator = new FileLinesIterator(emptyFile.toFile());

        assertThat(fileLinesIterator.hasNext(), is(true));
        assertThat(fileLinesIterator.next(), is(line("line 1", 7)));
        assertThat(fileLinesIterator.hasNext(), is(true));
        assertThat(fileLinesIterator.next(), is(line("", 1)));
    }

    @Test
    public void shouldHandleCorrectlyLargeFiles() throws IOException {
        final Path emptyFile = createTempFile("test", "");
        final PrintWriter printWriter = new PrintWriter(emptyFile.toFile());
        for (int i = 0; i < 10000; i++) {
            printWriter.println("line " + i);
        }
        printWriter.close();

        final FileLinesIterator fileLinesIterator = new FileLinesIterator(emptyFile.toFile());

        final Spliterator<Line> lineSpliterator = spliteratorUnknownSize(fileLinesIterator, SORTED);
        final Stream<Line> lineStream = stream(lineSpliterator, false);
        assertThat(lineStream.count(), is(10000L));
    }

}