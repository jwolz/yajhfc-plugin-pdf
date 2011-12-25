/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2011 Jonas Wolz
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
package yajhfc.file.pdf;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Logger;

import yajhfc.PaperSize;
import yajhfc.Utils;
import yajhfc.file.FileConverter;
import yajhfc.file.FileFormat;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * @author jonas
 *
 */
public class ITextImageFileConverter implements FileConverter {
    private static final Logger log = Logger.getLogger(ITextImageFileConverter.class.getName());
    
    /* (non-Javadoc)
     * @see yajhfc.file.FileConverter#convertToHylaFormat(java.io.File, java.io.OutputStream, yajhfc.PaperSize, yajhfc.file.FileFormat)
     */
    @Override
    public void convertToHylaFormat(File inFile, OutputStream destination,
            PaperSize paperSize, FileFormat desiredFormat)
            throws ConversionException, IOException {
        try {
            log.fine("Converting " + inFile + " to PDF using itext...");
            final Rectangle pageSize = PageSize.getRectangle(paperSize.name());
            Document document = new Document(pageSize);  
            PdfWriter writer = PdfWriter.getInstance(document, destination);  
            document.addCreator(Utils.AppShortName + " " + Utils.AppVersion);
            document.addSubject(inFile.getPath());
            writer.setStrictImageSequence(true);  
            document.open();  
            Image img = Image.getInstance(inFile.getPath());  
            ITextTIFFFileConverter.scaleImageToFit(document, img, pageSize);
            document.newPage();
            document.add(img);  
            document.close();
        } catch (BadElementException e) {
            throw new ConversionException("BadElementException from iText received", e);
        } catch (DocumentException e) {
            throw new ConversionException("DocumentException from iText received", e);
        } catch (NoClassDefFoundError e) {
            throw new ConversionException("iText not found", e);
        }
    }

    
    /* (non-Javadoc)
     * @see yajhfc.file.FileConverter#isOverridable()
     */
    @Override
    public boolean isOverridable() {
        return true;
    }

}
