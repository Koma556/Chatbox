package Communication;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

// used to parse language codes
public final class IsoUtil {
    private static final Set<String> ISO_LANGUAGES = new HashSet<String>
            (Arrays.asList(Locale.getISOLanguages()));
    
    private IsoUtil() {}

    public static boolean isValidISOLanguage(String s) {
        return ISO_LANGUAGES.contains(s);
    }
}