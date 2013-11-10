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
import java.util.Iterator;

import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.RandomAccessFileOrArray;
import com.itextpdf.text.pdf.codec.TiffImage;

/**
 * @author jonas
 *
 */
public class ITextTiffImageReader implements TIFFImageReader {
    protected RandomAccessFileOrArray ra;

    public ITextTiffImageReader(File tiff) throws IOException {
        this.ra = new RandomAccessFileOrArray(tiff.getPath());
    }
    
    @Override
    public void close() {
        try {
            ra.close();
        } catch (IOException e) {
            // NOP
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Iterable#iterator()
     */
    @Override
    public Iterator<Image> iterator() {
        return new ImgIterator();
    }

    protected class ImgIterator implements Iterator<Image> {
        protected final int numberOfPages;
        protected int page = 1;
        
        protected ImgIterator() {
            super();
            this.numberOfPages = TiffImage.getNumberOfPages(ra);
        }
        
        @Override
        public boolean hasNext() {
            return (page <= numberOfPages);
        }
        @Override
        public Image next() {
            return TiffImage.getTiffImage(ra, page++);
        }
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
