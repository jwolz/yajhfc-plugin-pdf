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

import java.io.FileOutputStream;
import java.util.Date;

import yajhfc.Utils;

import com.itextpdf.text.Document;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * @author jonas
 *
 */
public class SendReport {

    static void createReport(String fileName) throws Exception {
        final Rectangle pageSize = PageSize.getRectangle("A4");
        Document document = new Document(pageSize, 10, 10, 10, 10);
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(fileName));
        document.addCreator(Utils.AppShortName + " " + Utils.AppVersion);
        document.addSubject("Fax send report");
        document.open();
        PdfContentByte cb = writer.getDirectContent();
        
        BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA_BOLD, "Unicode", BaseFont.EMBEDDED);
        cb.beginText();
        cb.setFontAndSize(bf, 18);
        cb.showTextAligned(PdfContentByte.ALIGN_LEFT, "Fax send report", 10, 10+18, 0);
        
        PdfPTable table = new PdfPTable(2);
        table.addCell("Number:");
        table.addCell("0123456789 ДДД!");
        table.addCell("Time:");
        table.addCell(new Date().toString());
        table.addCell("Nüm Attempts:");
        table.addCell("1→2");
        //bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
        cb.endText();
        
        document.add(table);
        // step 5
        document.close();
        
        
    }
    
    /**
     * @param args
     */
    public static void main(String[] args) throws Exception{
        String file = "/tmp/report.pdf";
        createReport(file);
        Runtime.getRuntime().exec("evince " + file);
    }

}
