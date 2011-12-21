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
package yajhfc.pdf.i18n;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import yajhfc.Utils;

/**
 * Class holding the translations
 * 
 * @author jonas
 *
 */
public class Msgs {
    private static final String MESSAGE_BUNDLE_NAME = "yajhfc.pdf.i18n.Messages";
    
    private static ResourceBundle msgs = null;
    private static boolean triedMsgLoad = false;
    
    /**
     * Returns the translation of key. If no translation is found, the
     * key is returned.
     * @param key
     * @return
     */
    public static String _(String key) {
        return _(key, key);
    }
    
    /**
     * Returns the translation of key. If no translation is found, the
     * defaultValue is returned.
     * @param key
     * @param defaultValue
     * @return
     */
    public static String _(String key, String defaultValue) {
        if (msgs == null)
            if (triedMsgLoad)
                return defaultValue;
            else {
                loadMessages();
                return _(key, defaultValue);
            }                
        else
            try {
                return msgs.getString(key);
            } catch (Exception e) {
                return defaultValue;
            }
    }
    
    private static void loadMessages() {
        triedMsgLoad = true;
        
        // Use special handling for english locale as we don't use
        // a ResourceBundle for it
        final Locale myLocale = Utils.getLocale();
        if (myLocale.getLanguage().equals(Locale.ENGLISH.getLanguage())) {
            if (Utils.debugMode) {
                Logger.getLogger(Msgs.class.getName()).fine("Not loading messages for language " + myLocale);
            }
            msgs = null;
        } else {
            try {
                if (Utils.debugMode) {
                    Logger.getLogger(Msgs.class.getName()).fine("Trying to load messages for language " + myLocale);
                }
                msgs = ResourceBundle.getBundle(MESSAGE_BUNDLE_NAME, myLocale);
            } catch (Exception e) {
                Logger.getLogger(Msgs.class.getName()).log(Level.INFO, "Error loading messages for " + myLocale, e);
                msgs = null;
            }
        }
    }
    
    private Msgs() {};
}
