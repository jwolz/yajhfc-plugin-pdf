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
package yajhfc.file.textextract.pdf;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import yajhfc.file.FileConverter.ConversionException;
import yajhfc.file.FileFormat;
import yajhfc.file.textextract.HylaToTextConverter;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

public class ITextPDFToTextConverter extends HylaToTextConverter {
    
    @Override
    public String getDescription() {
        return "iText";
    }

    @Override
    public FileFormat[] getAllowedInputFormats() {
        return new FileFormat[] { FileFormat.PDF };
    }

    @Override
    protected CharSequence[] convertToText(File[] input) throws IOException,
            ConversionException {
        try {
            List<CharSequence> result = new ArrayList<CharSequence>();
            for (File pdf : input) {
                PdfReader pdfR = new PdfReader(pdf.getPath());
                for (int pg = 1; pg<=pdfR.getNumberOfPages(); pg++) {
                    result.add(PdfTextExtractor.getTextFromPage(pdfR, pg));
                }
                pdfR.close();
            }
            return result.toArray(new CharSequence[result.size()]);
        } catch (NoClassDefFoundError e) {
            throw new ConversionException("iText not found", e);
        }
    }

//    public static void main(String[] args) throws IOException, ConversionException {
//        List<FormattedFile> files = new ArrayList<FormattedFile>();
//        for (int i=0; i<args.length; i++) {
//            files.add(new FormattedFile(args[i]));
//        }
//        List<String> out = new ArrayList<String>();
//        System.out.println("================================================================================");
//        System.out.println("pdftotext");
//        System.out.println("================================================================================");
//        out.clear();
//        new FaxnumberExtractor(new PDFToTextConverter()).extractFromMultipleFiles(files, out);
//        System.out.println(out);
//        System.out.println("================================================================================");
//        System.out.println("ps2ascii");
//        System.out.println("================================================================================");
//        out.clear();
//        new FaxnumberExtractor(new PSToAsciiConverter()).extractFromMultipleFiles(files, out);
//        System.out.println(out);
//        System.out.println("================================================================================");
//        System.out.println("pstotext");
//        System.out.println("================================================================================");
//        out.clear();
//        new FaxnumberExtractor(new PSToTextConverter()).extractFromMultipleFiles(files, out);
//        System.out.println(out);
//        System.out.println("================================================================================");
//        System.out.println("iText");
//        System.out.println("================================================================================");
//        out.clear();
//        new FaxnumberExtractor(new ITextPDFToTextConverter()).extractFromMultipleFiles(files, out);
//        System.out.println(out);
//    }
}
