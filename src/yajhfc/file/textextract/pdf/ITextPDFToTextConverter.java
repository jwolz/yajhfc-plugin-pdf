package yajhfc.file.textextract.pdf;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import yajhfc.file.FileConverter.ConversionException;
import yajhfc.file.FileFormat;
import yajhfc.file.FormattedFile;
import yajhfc.file.textextract.FaxnumberExtractor;
import yajhfc.file.textextract.HylaToTextConverter;
import yajhfc.file.textextract.PDFToTextConverter;
import yajhfc.file.textextract.PSToAsciiConverter;
import yajhfc.file.textextract.PSToTextConverter;

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
        List<CharSequence> result = new ArrayList<CharSequence>();
        for (File pdf : input) {
            PdfReader pdfR = new PdfReader(pdf.getPath());
            for (int pg = 1; pg<=pdfR.getNumberOfPages(); pg++) {
                result.add(PdfTextExtractor.getTextFromPage(pdfR, pg));
            }
            pdfR.close();
        }
        return result.toArray(new CharSequence[result.size()]);
    }

    public static void main(String[] args) throws IOException, ConversionException {
        List<FormattedFile> files = new ArrayList<FormattedFile>();
        for (int i=0; i<args.length; i++) {
            files.add(new FormattedFile(args[i]));
        }
        List<String> out = new ArrayList<String>();
        System.out.println("================================================================================");
        System.out.println("pdftotext");
        System.out.println("================================================================================");
        out.clear();
        new FaxnumberExtractor(new PDFToTextConverter()).extractFromMultipleFiles(files, out);
        System.out.println(out);
        System.out.println("================================================================================");
        System.out.println("ps2ascii");
        System.out.println("================================================================================");
        out.clear();
        new FaxnumberExtractor(new PSToAsciiConverter()).extractFromMultipleFiles(files, out);
        System.out.println(out);
        System.out.println("================================================================================");
        System.out.println("pstotext");
        System.out.println("================================================================================");
        out.clear();
        new FaxnumberExtractor(new PSToTextConverter()).extractFromMultipleFiles(files, out);
        System.out.println(out);
        System.out.println("================================================================================");
        System.out.println("iText");
        System.out.println("================================================================================");
        out.clear();
        new FaxnumberExtractor(new ITextPDFToTextConverter()).extractFromMultipleFiles(files, out);
        System.out.println(out);
    }
}
