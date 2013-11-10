/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2013 Jonas Wolz <info@yajhfc.de>
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
package yajhfc.file.tiff;

import java.io.File;
import java.io.IOException;

/**
 * @author jonas
 *
 */
public class TIFFImageReaderFactory {
    public static TIFFImageReaderFactory DEFAULT = new TIFFImageReaderFactory();
    
    public TIFFImageReader createReader(File tiff) throws IOException {
        return new ITextTiffImageReader(tiff);
    }

}
