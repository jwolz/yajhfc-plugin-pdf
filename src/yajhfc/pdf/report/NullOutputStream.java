/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2012 Jonas Wolz <info@yajhfc.de>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package yajhfc.pdf.report;

import java.io.OutputStream;

/**
 * An output stream that does nothing (i.e. /dev/null equivalent)
 * @author jonas
 *
 */
public class NullOutputStream extends OutputStream {

    @Override
    public void write(int b) {
        // NOP
    }

    @Override
    public void write(byte[] b) {
        // NOP
    }

    @Override
    public void write(byte[] b, int off, int len) {
        // NOP
    }
}
