/**
 * 
 */
package yajhfc.faxcover.pdf;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.Collections;
import java.util.logging.Logger;

import yajhfc.faxcover.Faxcover;
import yajhfc.faxcover.tag.ConditionState;
import yajhfc.faxcover.tag.ConditionalTag;
import yajhfc.faxcover.tag.Tag;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

/**
 * @author jonas
 *
 */
public class PDFFaxcover extends Faxcover {
    private static final Logger log = Logger.getLogger(PDFFaxcover.class.getName());
    
    protected PdfReader pdfReader;
    
    public PDFFaxcover(URL coverTemplate) {
        super(coverTemplate);
    }
    
    protected PdfReader getPdfReader() throws IOException {
        if (pdfReader == null) {
            pdfReader = new PdfReader(this.coverTemplate); // Cache the reader in case we make multiple cover pages from one template
        }
        return pdfReader;
    }

    /* (non-Javadoc)
     * @see yajhfc.faxcover.Faxcover#makeCoverSheet(java.io.OutputStream)
     */
    @Override
    public void makeCoverSheet(OutputStream out) throws IOException {
        try {
            log.fine("Opening " + this.coverTemplate);
            PdfStamper filledOutForm = new PdfStamper(getPdfReader(), out);

            AcroFields form = filledOutForm.getAcroFields();
            for (String field : form.getFields().keySet()) {
                Tag tag = Tag.availableTags.get(field.toLowerCase());
                if (tag != null) {
                    if (tag instanceof ConditionalTag) {
                        log.warning("Conditional tags not supported for PDF: " + field);
                    } else {
                        form.setField(field, tag.getValue(this, Collections.<ConditionState>emptyList(), null));
                    }
                } else {
                    log.info("Unknown form field \"" + field + "\" found, ignoring it.");
                }
            }

            filledOutForm.close();
        } catch (DocumentException e) {
            throw (IOException)new IOException("Error from itext").initCause(e);
        }
    }

}
