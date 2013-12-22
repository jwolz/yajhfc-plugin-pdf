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
package yajhfc.pdf;


import java.net.URL;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import yajhfc.pdf.i18n.Msgs;
import yajhfc.plugin.PluginManager;
import yajhfc.plugin.PluginUI;

import com.itextpdf.text.Version;

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
     * Flag if native libtiff support is available
     */
    public static boolean haveNativeLibTIFF=false;
    
    /**
     * Version of native TIFF library
     */
    public static Future<String> nativeTIFFVersion=null;
    
	/**
	 * Plugin initialization method.
	 * The name and signature of this method must be exactly as follows 
	 * (i.e. it must always be "public static boolean init(int)" )
	 * @param startupMode the mode YajHFC is starting up in. The possible
	 *    values are one of the STARTUP_MODE_* constants defined in yajhfc.plugin.PluginManager
	 * @return true if the initialization was successful, false otherwise.
	 */
	public static boolean init(int startupMode) {
	    if (Utils.debugMode) {
	        Logger.getLogger(EntryPoint.class.getName()).info("iText version: " + Version.getInstance().getVersion());
	    }
	    
	    Faxcover.supportedCoverFormats.put(FileFormat.PDF, PDFFaxcover.class);
	    
	    FileConverters.addFileConverterSource(new FileConverterSource() {
	        @Override
	        public void addFileConvertersTo(Map<FileFormat, FileConverter> converters) {	            
	            try {
                    Version.getInstance().getVersion();
                } catch (Throwable e) {
                    Logger.getLogger(EntryPoint.class.getName()).log(Level.SEVERE, "Could not initialize iText", e);
                    return;
                }
	            
	            PDFOptions options = getOptions();
	            if (options.useITextForTIFF)
	                converters.put(FileFormat.TIFF, new ITextTIFFFileConverter(options));
	            
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
                        Msgs._("PDF support (iText)"), // The text displayed in the tree view for this options page
                        loadIcon("pdf.png"));            // The icon displayed in the tree view for this options page
	        }
	        
	        @Override
	        public void saveOptions(Properties p) {
                getOptions().storeToProperties(p);
	        }
        });
	    
	    FileConverters.invalidateFileConverters();
		return true;
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
