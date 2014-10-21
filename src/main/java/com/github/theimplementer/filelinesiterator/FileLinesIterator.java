package com.github.theimplementer.filelinesiterator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static com.github.theimplementer.filelinesiterator.Line.line;

public class FileLinesIterator implements Iterator<Line> {

    private static final char LINE_FEED = '\n';
    private static final char CARRIAGE_RETURN = '\r';
    private static final int BUFFER_LENGTH = 8192;

    private final File input;

    private ByteBuffer buffer;
    private long filePosition;
    private int positionInBuffer;
    private int lineLength;

    public FileLinesIterator(File input) throws IOException {
        this.input = input;
        this.buffer = ByteBuffer.allocate(BUFFER_LENGTH);
        final FileInputStream fileInputStream = new FileInputStream(input);
        final FileChannel channel = fileInputStream.getChannel();
        channel.read(buffer, filePosition);
        this.buffer.flip();
        this.filePosition = 0;
        this.positionInBuffer = 0;
    }

    @Override
    public boolean hasNext() {
        refillBufferIfNeeded();
        return buffer.limit() != 0;
    }

    @Override
    public Line next() {
        if (buffer.limit() == 0) {
            throw new NoSuchElementException();
        }

        lineLength = 0;
        final StringBuilder stringBuilder = new StringBuilder();

        while (true) {
            if (!refillBufferIfNeeded()) break;
            final char nextChar = (char) buffer.get(positionInBuffer);
            if (!isLineDelimiter(nextChar)) {
                stringBuilder.append(nextChar);
            } else if (isCarriageReturnFollowedByLineFeed(nextChar)) {
                incrementCounters();
            }
            incrementCounters();
            if (isLineDelimiter(nextChar)) break;

        }
        if (lineLength == 0) {
            throw new NoSuchElementException();
        }
        return line(stringBuilder.toString(), lineLength);
    }

    private void incrementCounters() {
        lineLength++;
        positionInBuffer++;
        filePosition++;
    }

    private boolean isCarriageReturnFollowedByLineFeed(char nextChar) {
        if (nextChar == CARRIAGE_RETURN && positionInBuffer + 1 < buffer.limit()) {
            final int followingCharacter = (char) buffer.get(positionInBuffer + 1);
            return followingCharacter == LINE_FEED;
        }
        return false;
    }

    private boolean refillBufferIfNeeded() {
        if (positionInBuffer < buffer.limit()) return true;
        try {
            final FileInputStream fileInputStream = new FileInputStream(input);
            final FileChannel fileChannel = fileInputStream.getChannel();
            final int bytesRead = fileChannel.read(buffer, filePosition);
            if (bytesRead == -1) {
                buffer.limit(0);
            } else {
                buffer.limit(bytesRead);
                buffer.rewind();
                positionInBuffer = 0;
            }
            fileInputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return buffer.hasRemaining();
    }

    private boolean isLineDelimiter(char aChar) {
        return aChar == LINE_FEED || aChar == CARRIAGE_RETURN;
    }
}
