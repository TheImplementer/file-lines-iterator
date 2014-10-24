package com.github.theimplementer.filelinesiterator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static com.github.theimplementer.filelinesiterator.Line.line;

public class FileLinesIterator implements Iterator<Line> {

    private static final char LINE_FEED = '\n';
    private static final char CARRIAGE_RETURN = '\r';
    private static final int BUFFER_LENGTH = 8192;

    private final FileInputStream inputStream;

    private byte[] buffer;
    private int positionInBuffer;
    private int bufferSize;
    private int lineLength;
    private StringBuilder stringBuilder;

    public FileLinesIterator(File input) throws IOException {
        this.buffer = new byte[BUFFER_LENGTH];
        this.inputStream = new FileInputStream(input);
        this.bufferSize = inputStream.read(buffer);
        this.positionInBuffer = 0;
        this.stringBuilder = new StringBuilder();
    }

    @Override
    public boolean hasNext() {
        refillBufferIfNeeded();
        if (bufferSize == -1) {
            closeInputStream();
            return false;
        }
        return true;
    }

    @Override
    public Line next() {
        if (bufferSize == -1) {
            closeInputStream();
            throw new NoSuchElementException();
        }

        lineLength = 0;
        stringBuilder.setLength(0);

        while (true) {
            if (!refillBufferIfNeeded()) break;
            final char nextChar = (char) buffer[positionInBuffer];
            if (!isLineDelimiter(nextChar)) {
                stringBuilder.append(nextChar);
            } else if (isCarriageReturnFollowedByLineFeed(nextChar)) {
                incrementCounters();
            }
            incrementCounters();
            if (isLineDelimiter(nextChar)) break;

        }
        if (lineLength == 0) {
            closeInputStream();
            throw new NoSuchElementException();
        }
        return line(stringBuilder.toString(), lineLength);
    }

    private void incrementCounters() {
        lineLength++;
        positionInBuffer++;
    }

    private boolean isCarriageReturnFollowedByLineFeed(char nextChar) {
        if (nextChar == CARRIAGE_RETURN && positionInBuffer + 1 < bufferSize) {
            final char followingCharacter = (char) buffer[positionInBuffer + 1];
            return followingCharacter == LINE_FEED;
        }
        return false;
    }

    private boolean refillBufferIfNeeded() {
        if (positionInBuffer < bufferSize) return true;
        try {
            bufferSize = inputStream.read(buffer);
            positionInBuffer = 0;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return bufferSize > 0;
    }

    private boolean isLineDelimiter(char aChar) {
        return aChar == LINE_FEED || aChar == CARRIAGE_RETURN;
    }

    private void closeInputStream() {
        try {
            inputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
