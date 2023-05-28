/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.parser;

import java.io.IOException;

@SuppressWarnings({ "PMD.MethodNamingConventions", "PMD.CyclomaticComplexity" })
public class SimpleCharStream {

    /**
     * Whether parser is static.
     */
    @SuppressWarnings("checkstyle:constantname")
    public static final boolean staticFlag = false;

    int bufsize;

    int available;

    int tokenBegin;

    /**
     * Position in buffer.
     */
    public int bufpos = -1;

    protected int[] bufline;

    protected int[] bufcolumn;

    protected int column = 0;

    protected int line = 1;

    protected boolean prevCharIsCR = false;

    protected boolean prevCharIsLF = false;

    protected Provider inputStream;

    private boolean isStringProvider;

    protected char[] buffer;

    protected int maxNextCharInd = 0;

    protected int inBuf = 0;

    protected int tabSize = 1;

    protected boolean trackLineColumn = true;

    protected int totalCharsRead = 0;

    protected int absoluteTokenBegin = 0;

    public void setTabSize(int i) {
        tabSize = i;
    }

    public int getTabSize() {
        return tabSize;
    }

    public final int getAbsoluteTokenBegin() {
        return absoluteTokenBegin;
    }

    protected void ExpandBuff(boolean wrapAround) throws IOException {
        char[] newbuffer = new char[bufsize + 2048];
        int[] newbufline = new int[bufsize + 2048];
        int[] newbufcolumn = new int[bufsize + 2048];
        try {
            if (wrapAround) {
                System.arraycopy(buffer, tokenBegin, newbuffer, 0, bufsize - tokenBegin);
                System.arraycopy(buffer, 0, newbuffer, bufsize - tokenBegin, bufpos);
                buffer = newbuffer;
                System.arraycopy(bufline, tokenBegin, newbufline, 0, bufsize - tokenBegin);
                System.arraycopy(bufline, 0, newbufline, bufsize - tokenBegin, bufpos);
                bufline = newbufline;
                System.arraycopy(bufcolumn, tokenBegin, newbufcolumn, 0, bufsize - tokenBegin);
                System.arraycopy(bufcolumn, 0, newbufcolumn, bufsize - tokenBegin, bufpos);
                bufcolumn = newbufcolumn;
                maxNextCharInd = bufpos += bufsize - tokenBegin;
            } else {
                System.arraycopy(buffer, tokenBegin, newbuffer, 0, bufsize - tokenBegin);
                buffer = newbuffer;
                System.arraycopy(bufline, tokenBegin, newbufline, 0, bufsize - tokenBegin);
                bufline = newbufline;
                System.arraycopy(bufcolumn, tokenBegin, newbufcolumn, 0, bufsize - tokenBegin);
                bufcolumn = newbufcolumn;
                maxNextCharInd = bufpos -= tokenBegin;
            }
        } catch (Throwable t) {
            throw new IOException("Errow expanding the buffer.", t);
        }
        bufsize += 2048;
        available = bufsize;
        tokenBegin = 0;
    }

    protected void FillBuff() throws IOException {
        if (!isStringProvider && maxNextCharInd == available) {
            if (available == bufsize) {
                if (tokenBegin > 2048) {
                    bufpos = maxNextCharInd = 0;
                    available = tokenBegin;
                } else if (tokenBegin < 0) {
                    bufpos = maxNextCharInd = 0;
                } else {
                    ExpandBuff(false);
                }
            } else if (available > tokenBegin) {
                available = bufsize;
            } else if ((tokenBegin - available) < 2048) {
                ExpandBuff(true);
            } else {
                available = tokenBegin;
            }
        }
        int i;
        try {
            if (inputStream instanceof StringProvider) {
                i = ((StringProvider) inputStream)._string.length();
                if (maxNextCharInd == i) {
                    throw new IOException();
                }
                maxNextCharInd = i;
            } else {
                if ((i = inputStream.read(buffer, maxNextCharInd, available - maxNextCharInd)) == -1) {
                    inputStream.close();
                    throw new IOException();
                } else {
                    maxNextCharInd += i;
                }
            }
            return;
        } catch (IOException e) {
            --bufpos;
            backup(0);
            if (tokenBegin == -1) {
                tokenBegin = bufpos;
            }
            throw e;
        }
    }

    /**
     * Start.
     * @return the character read
     * @throws IOException
     */
    public char BeginToken() throws IOException {
        tokenBegin = -1;
        char c = readChar();
        tokenBegin = bufpos;
        absoluteTokenBegin = totalCharsRead;
        return c;
    }

    protected void UpdateLineColumn(char c) {
        column++;
        if (prevCharIsLF) {
            prevCharIsLF = false;
            line += column = 1;
        } else if (prevCharIsCR) {
            prevCharIsCR = false;
            if (c == '\n') {
                prevCharIsLF = true;
            } else {
                line += column = 1;
            }
        }
        switch(c) {
            case '\r':
                prevCharIsCR = true;
                break;
            case '\n':
                prevCharIsLF = true;
                break;
            case '\t':
                column--;
                column += tabSize - (column % tabSize);
                break;
            default:
                break;
        }
        bufline[bufpos] = line;
        bufcolumn[bufpos] = column;
    }

    private char readChar(int pos) {
        if (this.inputStream instanceof StringProvider) {
            return ((StringProvider) inputStream)._string.charAt(pos);
        } else {
            return buffer[pos];
        }
    }

    /**
     * Read a character.
     * @return the character read
     * @throws IOException
     */
    public char readChar() throws IOException {
        if (inBuf > 0) {
            --inBuf;
            if (++bufpos == bufsize) {
                bufpos = 0;
            }
            totalCharsRead++;
            return readChar(bufpos);
        }
        if (++bufpos >= maxNextCharInd) {
            FillBuff();
        }
        totalCharsRead++;
        char c = readChar(bufpos);
        UpdateLineColumn(c);
        return c;
    }

    /**
     * @return the column
     * @deprecated @see #getEndColumn
     */
    @Deprecated
    public int getColumn() {
        return getBufferColumnValue();
    }

    /**
     * @return the line
     * @deprecated @see #getEndLine
     */
    @Deprecated
    public int getLine() {
        return getBufLineIndex();
    }

    /**
     * @return get token end column number.
     */
    public int getEndColumn() {
        return getBufferColumnValue();
    }

    /**
     * @return get token end line number.
     */
    public int getEndLine() {
        return getBufLineIndex();
    }

    /**
     * @return get token beginning column number.
     */
    public int getBeginColumn() {
        return bufcolumn[tokenBegin];
    }

    /**
     * @return get token beginning line number.
     */
    public int getBeginLine() {
        return bufline[tokenBegin];
    }

    /**
     * Backup a number of characters.
     * @param amount
     */
    public void backup(int amount) {
        inBuf += amount;
        totalCharsRead -= amount;
        if ((bufpos -= amount) < 0) {
            bufpos += bufsize;
        }
    }

    /**
     * Constructor
     * @param dstream
     * @param startline
     * @param startcolumn
     * @param buffersize
     */
    public SimpleCharStream(Provider dstream, int startline, int startcolumn, int buffersize) {
        inputStream = dstream;
        isStringProvider = dstream instanceof StringProvider;
        line = startline;
        column = startcolumn - 1;
        if (isStringProvider) {
            int bs = ((StringProvider) inputStream)._string.length();
            available = bufsize = bs;
            bufline = new int[bs];
            bufcolumn = new int[bs];
        } else {
            available = bufsize = buffersize;
            buffer = new char[buffersize];
            bufline = new int[buffersize];
            bufcolumn = new int[buffersize];
        }
    }

    /**
     * Constructor
     * @param dstream
     * @param startline
     * @param startcolumn
     */
    public SimpleCharStream(Provider dstream, int startline, int startcolumn) {
        this(dstream, startline, startcolumn, 4096);
    }

    /**
     * Constructor
     * @param dstream
     */
    public SimpleCharStream(Provider dstream) {
        this(dstream, 1, 1, 4096);
    }

    /**
     * Reinitialise.
     * @param dstream
     * @param startline
     * @param startcolumn
     * @param buffersize
     */
    public void ReInit(Provider dstream, int startline, int startcolumn, int buffersize) {
        inputStream = dstream;
        isStringProvider = dstream instanceof StringProvider;
        line = startline;
        column = startcolumn - 1;
        if (isStringProvider) {
            int bs = ((StringProvider) inputStream)._string.length();
            available = bufsize = bs;
            bufline = new int[bs];
            bufcolumn = new int[bs];
        } else {
            if (buffer == null || buffersize != buffer.length) {
                available = bufsize = buffersize;
                buffer = new char[buffersize];
                bufline = new int[buffersize];
                bufcolumn = new int[buffersize];
            }
        }
        prevCharIsLF = prevCharIsCR = false;
        tokenBegin = inBuf = maxNextCharInd = 0;
        bufpos = -1;
    }

    /**
     * Reinitialise.
     * @param dstream
     * @param startline
     * @param startcolumn
     */
    public void ReInit(Provider dstream, int startline, int startcolumn) {
        ReInit(dstream, startline, startcolumn, 4096);
    }

    /**
     * Reinitialise.
     * @param dstream
     */
    public void ReInit(Provider dstream) {
        ReInit(dstream, 1, 1, 4096);
    }

    /**
     * @return get token literal value.
     */
    public String GetImage() {
        if (isStringProvider) {
            String data = ((StringProvider) inputStream)._string;
            if (bufpos >= tokenBegin) {
                return data.substring(tokenBegin, bufpos + 1);
            } else {
                return data.substring(tokenBegin, bufsize) + data.substring(0, bufpos + 1);
            }
        } else {
            if (bufpos >= tokenBegin) {
                return new String(buffer, tokenBegin, bufpos - tokenBegin + 1);
            } else {
                return new String(buffer, tokenBegin, bufsize - tokenBegin) + new String(buffer, 0, bufpos + 1);
            }
        }
    }

    /**
     * @param len
     * @return get the suffix.
     */
    public char[] GetSuffix(int len) {
        char[] ret = new char[len];
        if (isStringProvider) {
            String str = ((StringProvider) inputStream)._string;
            if ((bufpos + 1) >= len) {
                str.getChars(bufpos - len + 1, bufpos - len + 1 + len, ret, 0);
            } else {
                str.getChars(bufsize - (len - bufpos - 1), bufsize - (len - bufpos - 1) + len - bufpos - 1, ret, 0);
                str.getChars(0, bufpos + 1, ret, len - bufpos - 1);
            }
        } else {
            if ((bufpos + 1) >= len) {
                System.arraycopy(buffer, bufpos - len + 1, ret, 0, len);
            } else {
                System.arraycopy(buffer, bufsize - (len - bufpos - 1), ret, 0, len - bufpos - 1);
                System.arraycopy(buffer, 0, ret, len - bufpos - 1, bufpos + 1);
            }
        }
        return ret;
    }

    /**
     * Reset buffer when finished.
     */
    public void Done() {
        buffer = null;
        bufline = null;
        bufcolumn = null;
    }

    /**
     * Method to adjust line and column numbers for the start of a token.
     * @param newLine
     * @param newCol
     */
    public void adjustBeginLineColumn(int newLine, int newCol) {
        int nl = newLine;
        int start = tokenBegin;
        int len;
        if (bufpos >= tokenBegin) {
            len = bufpos - tokenBegin + inBuf + 1;
        } else {
            len = bufsize - tokenBegin + bufpos + 1 + inBuf;
        }
        int i = 0;
        int j = 0;
        int k = 0;
        int nextColDiff = 0;
        int columnDiff = 0;
        while (i < len && bufline[j = start % bufsize] == bufline[k = ++start % bufsize]) {
            bufline[j] = nl;
            nextColDiff = columnDiff + bufcolumn[k] - bufcolumn[j];
            bufcolumn[j] = newCol + columnDiff;
            columnDiff = nextColDiff;
            i++;
        }
        if (i < len) {
            bufline[j] = nl++;
            bufcolumn[j] = newCol + columnDiff;
            while (i++ < len) {
                if (bufline[j = start % bufsize] != bufline[++start % bufsize]) {
                    bufline[j] = nl++;
                } else {
                    bufline[j] = nl;
                }
            }
        }
        line = bufline[j];
        column = bufcolumn[j];
    }

    boolean getTrackLineColumn() {
        return trackLineColumn;
    }

    void setTrackLineColumn(boolean tlc) {
        trackLineColumn = tlc;
    }

    private int getBufferColumnValue() {
        return bufcolumn[bufpos];
    }

    private int getBufLineIndex() {
        return bufline[bufpos];
    }
}
/* JavaCC - OriginalChecksum=47e65cd0a1ed785f7a51c9e0c60893c9 (do not edit this line) */
