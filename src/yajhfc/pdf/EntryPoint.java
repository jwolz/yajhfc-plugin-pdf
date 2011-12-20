package yajhfc.pdf;


import java.net.URL;
import java.util.Map;
import java.util.Properties;

import javax.swing.ImageIcon;

import yajhfc.Utils;
import yajhfc.faxcover.Faxcover;
import yajhfc.faxcover.pdf.PDFFaxcover;
import yajhfc.file.FileConverter;
import yajhfc.file.FileConverterSource;
import yajhfc.file.FileConverters;
import yajhfc.file.FileFormat;
import yajhfc.file.pdf.ITextImageFileConverter;
import yajhfc.file.pdf.ITextTIFFFileConverter;
import yajhfc.file.textextract.HylaToTextConverter;
import yajhfc.file.textextract.pdf.ITextPDFToTextConverter;
import yajhfc.launch.Launcher2;
import yajhfc.options.PanelTreeNode;
import yajhfc.plugin.PluginManager;
import yajhfc.plugin.PluginUI;

/**
 * Example initialization class for a YajHFC plugin.
 * 
 * The name of this class can be chosen freely, but must match the name
 * set in the YajHFC-Plugin-InitClass entry in the jar file.
 * @author jonas
 *
 */
public class EntryPoint {

	/**
	 * Plugin initialization method.
	 * The name and signature of this method must be exactly as follows 
	 * (i.e. it must always be "public static boolean init(int)" )
	 * @param startupMode the mode YajHFC is starting up in. The possible
	 *    values are one of the STARTUP_MODE_* constants defined in yajhfc.plugin.PluginManager
	 * @return true if the initialization was successful, false otherwise.
	 */
	public static boolean init(int startupMode) {
	    Faxcover.supportedCoverFormats.put(FileFormat.PDF, PDFFaxcover.class);
	    
	    FileConverters.addFileConverterSource(new FileConverterSource() {
	        @Override
	        public void addFileConvertersTo(Map<FileFormat, FileConverter> converters) {
	            PDFOptions options = getOptions();
	            
	            if (options.useITextForTIFF)
	                converters.put(FileFormat.TIFF, new ITextTIFFFileConverter());
	            
	            ITextImageFileConverter ifc = new ITextImageFileConverter();
	            if (options.useITextForPNG)
	                converters.put(FileFormat.PNG, ifc);
	            if (options.useITextForJPEG)
	                converters.put(FileFormat.JPEG, ifc);
	            if (options.useITextForGIF)
	                converters.put(FileFormat.GIF, ifc);
	        }
        });
	    
	    HylaToTextConverter.availableConverters.add(new ITextPDFToTextConverter());

	    PluginManager.pluginUIs.add(new PluginUI() {
	        @Override
	        public int getOptionsPanelParent() {
	            return OPTION_PANEL_PATHS_VIEWERS;
	        }
	        
	        @Override
	        public PanelTreeNode createOptionsPanel(PanelTreeNode parent) {
                /*
                 * This method must return a PanelTreeNode as shown below
                 * or null to not create an options page
                 */
                return new PanelTreeNode(
                        parent, // Always pass the parent as first parameter
                        new PDFOptionsPanel(), // The actual UI component that implements the options panel. 
                                                // This object *must* implement the OptionsPage interface.
                        _("PDF support (iText)"), // The text displayed in the tree view for this options page
                        loadIcon("pdf.png"));            // The icon displayed in the tree view for this options page
	        }
	        
	        @Override
	        public void saveOptions(Properties p) {
                getOptions().storeToProperties(p);
	        }
        });
	    
		return true;
	}
	
	public static String _(String key) {
	    return key;
	}
	
    public static ImageIcon loadIcon(String fileName) {
        URL imgURL = EntryPoint.class.getResource("/yajhfc/pdf/images/" + fileName);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            return null;
        }
    }
	
	private static PDFOptions options;
	/**
	 * Lazily load some options (optional, only if you want to save settings)
	 * @return
	 */
	public static PDFOptions getOptions() {
	    if (options == null) {
	        options = new PDFOptions();
	        options.loadFromProperties(Utils.getSettingsProperties());
	    }
	    return options;
	}
    
    /**
     * Launches YajHFC including this plugin (for debugging purposes)
     * @param args
     */
    public static void main(String[] args) {
		PluginManager.internalPlugins.add(EntryPoint.class);
		Launcher2.main(args);
	}
}
