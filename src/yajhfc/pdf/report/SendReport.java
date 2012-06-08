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

import static yajhfc.pdf.i18n.Msgs._;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import yajhfc.Utils;
import yajhfc.file.FormattedFile;
import yajhfc.file.MultiFileConvFormat;
import yajhfc.file.MultiFileConverter;
import yajhfc.model.FmtItem;
import yajhfc.model.servconn.FaxDocument;
import yajhfc.model.servconn.FaxJob;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * @author jonas
 *
 */
public class SendReport<T extends FmtItem> {
        
    protected float marginTop    = 10;
    protected float marginLeft   = 10;
    protected float marginRight  = 10;
    protected float marginBottom = 10;
    
    protected float headerFontSize = 18;
    protected float normalFontSize = 10;
    
    protected final List<T> columns = new ArrayList<T>();
    
    protected yajhfc.PaperSize paperSize = yajhfc.PaperSize.A4;
    
    protected int startPage = 1;
    /**
     * End page. 0 means all pages
     */
    protected int endPage = 0; 
    /**
     * Number of thumbnails per page. 0 means unlimited (all on one page)
     */
    protected int thumbnailsPerPage = 0;
    
    /**
     * Print a header
     * @param cb
     * @param x
     * @param y
     * @param width
     * @param nPage 
     * @param numPages
     * @return
     * @throws IOException 
     * @throws DocumentException 
     */
    private float printHeader(PdfContentByte cb, float x, float y, float width, int nPage, int numPages) throws DocumentException, IOException {
        BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
        cb.beginText();
        cb.setFontAndSize(bf, headerFontSize);
        
        cb.setTextMatrix(x, y-headerFontSize);
        cb.showText(_("Fax send report"));
        
        bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
        cb.setFontAndSize(bf, normalFontSize);
        String pageText = MessageFormat.format(_("Page {0} of {1}"), nPage, numPages);
        float textWidth = cb.getEffectiveStringWidth(pageText, false);
        cb.setTextMatrix(x+width-textWidth, y-headerFontSize);
        cb.showText(pageText);
 
        cb.endText();
        
        return y - headerFontSize*1.5f;
    }
    
    private float printRows(PdfContentByte cb, float x, float y, float width, Row[] rows) throws DocumentException, IOException {
        y -= 2;
        cb.setLineWidth(0f);
        cb.moveTo(x, y);
        cb.lineTo(x+width, y);
        cb.stroke();
        y -= 2;
        
        cb.beginText();
        BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
        cb.setFontAndSize(bf, normalFontSize);
        
        float maxWidth = 0;
        for (Row row : rows) {
            row.width = cb.getEffectiveStringWidth(row.description, false);
            if (row.width > maxWidth)
                maxWidth = row.width;
        }
        for (Row row : rows) {
            cb.setTextMatrix(x + maxWidth - row.width, y-10);
            cb.showText(row.description + ": " + row.value);
            y -= 14;
        }
        cb.endText();
        
        y -= 2;
        cb.setLineWidth(0f);
        cb.moveTo(x, y);
        cb.lineTo(x+width, y);
        cb.stroke();
        y -= 2;
        
        
        return y;
    }
    
    /**
     * 
     * @param cb
     * @param writer
     * @param x
     * @param y
     * @param width
     * @param height
     * @param pdfFile
     * @param beginPage first page to output
     * @param maxPages the maximum number of pages to output or 0 for all pages
     * @return The last page processed
     * @throws IOException
     */
    private int printNup(PdfContentByte cb, PdfWriter writer, PdfReader reader, float x, float y, float width, float height, int beginPage, int maxPages) throws IOException {
        
        Rectangle pgSize = reader.getPageSize(1);
        int numPages = reader.getNumberOfPages() - beginPage + 1;
        if (maxPages > 0 && numPages > maxPages)
            numPages = maxPages;
            
        int numCols = numPages;
        int numRows = 1;
        
        float factor;
        
        while (true) {
            factor = Math.min(
                    width  / (pgSize.getWidth()  * numRows),
                    height / (pgSize.getHeight() * numCols)
                    );
        
            float scaledHeight = pgSize.getHeight() * factor;
            if (height / scaledHeight > (numRows + 1)) {
                numRows += 1;
                numCols  = (numPages + numRows - 1) / numRows; // set the number of rows to numPages / numRows, rounded up 
            } else {
                break;
            }
        }
        
        cb.setLineWidth(0);
        float cellWidth  = width  / numCols;
        float cellHeight = height / numRows;
        int iPage = 0;
        for (int row=0; row<numRows; row++) {
            for (int col = 0; col<numCols; col++) {
                iPage = row * numCols + col + beginPage;
                if (iPage >= (beginPage + numPages)) {
                    return iPage-1;
                }
                PdfImportedPage page = writer.getImportedPage(reader, iPage);
                Rectangle bbox = page.getBoundingBox();
                
                factor = Math.min(
                        cellWidth / bbox.getWidth(),
                        cellHeight / bbox.getHeight()
                        );
                
                float drawWidth  = bbox.getWidth() * factor;
                float drawHeight = bbox.getHeight() * factor;
                float drawX      = x + col * cellWidth  + (cellWidth  - drawWidth)  / 2;
                float drawY      = y - row * cellHeight - (cellHeight + drawHeight) / 2;
                
                cb.addTemplate(page, factor, 0, 0, factor, drawX, drawY);
                
                cb.rectangle(drawX, drawY, drawWidth, drawHeight);
                cb.stroke();
            }
        }
        
        return iPage;
    }
    
    public void createReport(FaxJob<T> job, File pdfFile) throws Exception {
        // Calculate rows for status table
        Row[] rows = new Row[columns.size()];
        for (int i=0; i<rows.length; i++) {
            T col = columns.get(i);
            String desc = col.getDescription();
            Object oVal = job.getData(col);
            String sVal;
            if (oVal == null) {
                sVal = "";
            } else if (col.getDataType() == Date.class) {
                sVal = col.getDisplayDateFormat().format(oVal);
            } else if (col.getDataType() == Boolean.class) {
                sVal = ((Boolean)oVal).booleanValue() ? _("yes") : _("no");
            } else {
                sVal = oVal.toString();
            }
            
            rows[i] = new Row(desc, sVal);
        }
        // Get input PDF
        Collection<FaxDocument> docs = job.getDocuments();
        List<FormattedFile> files = new ArrayList<FormattedFile>(docs.size());
        for (FaxDocument doc : docs) {
            files.add(doc.getDocument());
        }
        File tempPDF = File.createTempFile("sendreport", ".pdf");
        tempPDF.deleteOnExit();
        MultiFileConverter.convertMultipleFilesToSingleFile(files, tempPDF, MultiFileConvFormat.PDF, paperSize);
        
        // Create the output PDF
        Document document = new Document(PageSize.getRectangle(paperSize.name()), marginLeft, marginRight, marginTop, marginBottom);
        FileOutputStream outStream = new FileOutputStream(pdfFile);
        PdfWriter writer = PdfWriter.getInstance(document, outStream);
        document.addCreator(Utils.AppShortName + " " + Utils.AppVersion);
        document.addSubject("Fax send report for fax job " + job.getIDValue());
        document.open();
        PdfContentByte cb = writer.getDirectContent();
        PdfReader reader = new PdfReader(tempPDF.getPath());
        
        int firstPage = startPage;
        int lastPage = reader.getNumberOfPages();
        if (endPage > 0 && lastPage > endPage)
            lastPage = endPage;
        int totalOutPages;
        if (thumbnailsPerPage == 0) {
            totalOutPages = 1;
        } else {
            totalOutPages = (reader.getNumberOfPages() + thumbnailsPerPage - 1) / thumbnailsPerPage;
        }
        int outPage = 1;
        while (firstPage <= lastPage) {
            document.newPage();

            float x = marginLeft;
            float y = document.top();
            float width  = document.right() - document.left();

            y = printHeader(cb, x, y, width, outPage, totalOutPages);
            y = printRows(cb, x, y, width, rows);

            float height = y - document.bottom();

            firstPage = printNup(cb, writer, reader, x, y, width, height, firstPage, Math.min(thumbnailsPerPage, lastPage-firstPage+1)) + 1;
            outPage++;
        }
        
        // step 5
        reader.close();
        document.close();
        
        outStream.close();
        
        tempPDF.delete();
    }
    
    
    public float getMarginTop() {
        return marginTop;
    }

    public float getMarginLeft() {
        return marginLeft;
    }

    public float getMarginRight() {
        return marginRight;
    }

    public float getMarginBottom() {
        return marginBottom;
    }

    public float getHeaderFontSize() {
        return headerFontSize;
    }

    public float getNormalFontSize() {
        return normalFontSize;
    }

    public List<T> getColumns() {
        return columns;
    }

    public yajhfc.PaperSize getPaperSize() {
        return paperSize;
    }

    public int getStartPage() {
        return startPage;
    }

    public int getEndPage() {
        return endPage;
    }

    public int getThumbnailsPerPage() {
        return thumbnailsPerPage;
    }

    public void setMarginTop(float marginTop) {
        this.marginTop = marginTop;
    }

    public void setMarginLeft(float marginLeft) {
        this.marginLeft = marginLeft;
    }

    public void setMarginRight(float marginRight) {
        this.marginRight = marginRight;
    }

    public void setMarginBottom(float marginBottom) {
        this.marginBottom = marginBottom;
    }

    public void setHeaderFontSize(float headerFontSize) {
        this.headerFontSize = headerFontSize;
    }

    public void setNormalFontSize(float normalFontSize) {
        this.normalFontSize = normalFontSize;
    }

    public void setPaperSize(yajhfc.PaperSize paperSize) {
        this.paperSize = paperSize;
    }

    public void setStartPage(int startPage) {
        this.startPage = startPage;
    }

    public void setEndPage(int endPage) {
        this.endPage = endPage;
    }

    public void setThumbnailsPerPage(int thumbnailsPerPage) {
        this.thumbnailsPerPage = thumbnailsPerPage;
    }

    static class Row {
        public final String description;
        public final String value;
        public float width;
        
        
        public Row(String description, String value) {
            super();
            this.description = description;
            this.value = value;
        }
    }
}
