README for YajHFC PDF plugin
=============================

The YajHFC PDF plugin uses the iText library to offer advanced PDF support.

This includes the following features:
- PDF cover pages (see Creating_PDF_coverpages.pdf for more information)
- Alternative Image to PDF converters
- Alternative TIFF to PDF converter:
  + Replaces tiff2pdf
  + Default behaviour should be more sensible for faxes than tiff2pdf's
  + Can chop "long faxes" (unlimited pagelength) into multiple pages
- PDF to text conversion option using iText


INSTALLATION
-------------

If you use the Windows setup:
Install the newest version using the setup program. This plugin is included 
in the default installation for YajHFC 0.5.2 and up.

If you use deb packages:
Install the yajhfc-pdfplugin package. 
If there is no libitext5-java package for your version of Debian/Ubuntu, using
a newer package (e.g. from Debian Testing) usually should work fine.

Else:
1. Unpack this ZIP file somewhere.
2. Start YajHFC, go to Options->Plugins&JDBC and click "Add plugin".
3. Select yajhfc-pdf-plugin.jar 
4. Restart YajHFC
5. If it worked, you should now have a new panel in the Options dialog called "Advanced PDF support (iText)"
   If you open that panel, you should be able to see the iText version (something like "iText® 5.1.3 ©2000-2011 1T3XT BVBA")
   
   
