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
import yajhfc.pdf.PDFOptions;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.RandomAccessFileOrArray;
import com.itextpdf.text.pdf.codec.TiffImage;

public class ITextTIFFFileConverter implements FileConverter {
    private static final Logger log = Logger.getLogger(ITextTIFFFileConverter.class.getName());
    
    protected final PDFOptions options;
    
    @Override
    public void convertToHylaFormat(File inFile, OutputStream destination,
            PaperSize paperSize, FileFormat desiredFormat)
    throws ConversionException, IOException {
        try {
            log.fine("Converting " + inFile + " to PDF using itext");
            
            final Rectangle pageSize = PageSize.getRectangle(paperSize.name());
            Document document = new Document(pageSize, 0, 0, 0, 0);
            PdfWriter writer = PdfWriter.getInstance(document, destination);
            document.addCreator(Utils.AppShortName + " " + Utils.AppVersion);
            document.addSubject(inFile.getPath());
            document.open();
            PdfContentByte cb = writer.getDirectContent();
            RandomAccessFileOrArray ra = new RandomAccessFileOrArray(inFile.getPath());
            int pages = TiffImage.getNumberOfPages(ra);
            log.fine("TIFF has " + pages + " pages");
            for (int pg = 0; pg < pages; ++pg) {
                Image img = TiffImage.getTiffImage(ra, pg + 1);
                if (img != null) {
                    log.fine("Writing page " + (pg + 1));
                    if (options.TIFFfitToPaperSize) {
                        if (options.TIFFchopLongPage && checkCrop(document, cb, img, pageSize)) {
                            continue;
                        } else {
                            scaleImageToFit(document, img, pageSize, !options.TIFFassumePortrait);
                        }
                    } else {
                        img.setAbsolutePosition(0, 0);
                        img.scalePercent(7200f / img.getDpiX(), 7200f / img.getDpiY());
                        document.setPageSize(new Rectangle(img.getScaledWidth(), img.getScaledHeight()));
                    }
                    document.newPage();
                    cb.addImage(img);
                    //document.newPage();
                }
            }
            ra.close();
            document.close();
        } catch (DocumentException e) {
            throw new ConversionException("DocumentException from iText received", e);
        } catch (NoClassDefFoundError e) {
            throw new ConversionException("iText not found", e);
        }
    }

    private boolean checkCrop(Document document, PdfContentByte cb, Image img, final Rectangle pageSize) throws DocumentException {
        float dpiFactor;
        if (img.getDpiX() > 0 && img.getDpiY() > 0)
            dpiFactor = (float)img.getDpiX() / (float)img.getDpiY();
        else
            dpiFactor = 1f;
        
        float imgWidth = img.getWidth();
        float imgHeight = img.getHeight() * dpiFactor;
        
        if (imgHeight > imgWidth * options.TIFFchopThreshold) {
            document.setPageSize(pageSize);
            
            float chunkHeight = (imgWidth * options.TIFFchopFactor);
            float docWidth = document.right()-document.left();
            float docHeight = document.top()-document.bottom();
            float scaleFactor = Math.min(docWidth/imgWidth, docHeight/chunkHeight);

            float tplWidth  = imgWidth * scaleFactor;
            float tplHeight = chunkHeight * scaleFactor;
            float scaledHeight = imgHeight * scaleFactor;
            img.scaleAbsolute(tplWidth, scaledHeight);     
            
            int count = (int)Math.ceil(imgHeight / chunkHeight);
            for (int i=1; i<=count; i++) {
                document.newPage();
                
                PdfTemplate tpl = cb.createTemplate(tplWidth, tplHeight);
                img.setAbsolutePosition(0, i * tplHeight - scaledHeight);
                tpl.addImage(img);
                
                cb.addTemplate(tpl, 
                        (document.left() + document.right() - tplWidth) * 0.5f, 
                        document.top() - tplHeight);
            }
            return true;
        } else {
            return false;
        }
    }
    
    
    /**
     * Scales the given image to fit the given page size and positions it vertically on top and horizontally centered.
     * If rotate==true switches the document's page size between landscape and portrait to make the image as large as possible.
     * @param document
     * @param img
     * @param pageSize
     */
    public static void scaleImageToFit(Document document, Image img, final Rectangle pageSize, boolean rotate) {
        float dpiFactor;
        if (img.getDpiX() > 0 && img.getDpiY() > 0)
            dpiFactor = (float)img.getDpiX() / (float)img.getDpiY();
        else
            dpiFactor = 1f;
        
        float imgWidth = img.getWidth();
        float imgHeight = img.getHeight() * dpiFactor;
        
        if (rotate && imgWidth > imgHeight) {
            document.setPageSize(pageSize.rotate());
        } else {
            document.setPageSize(pageSize);
        }                        
        float docWidth = document.right()-document.left();
        float docHeight = document.top()-document.bottom();
        float scaleFactor = Math.min(docWidth/imgWidth, docHeight/imgHeight);
        
        img.scaleAbsolute(imgWidth * scaleFactor, imgHeight * scaleFactor);
        img.setAbsolutePosition(
                (document.left() + document.right() - img.getScaledWidth()) * 0.5f,
                 document.top() - img.getScaledHeight()
                );
    }

    @Override
    public boolean isOverridable() {
        return true;
    }

    public ITextTIFFFileConverter(PDFOptions options) {
        super();
        this.options = options;
    }
}
