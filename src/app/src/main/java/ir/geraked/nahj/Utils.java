package ir.geraked.nahj;

import java.text.Normalizer;

public final class Utils {
    private Utils() {}

    public static String normalize(String input) {
        if (input == null) return "";
        // Unicode normalize
        String s = Normalizer.normalize(input, Normalizer.Form.NFKC);
        // Remove Arabic diacritics (tashkeel)
        s = s.replaceAll("[\u0610-\u061A\u064B-\u065F\u0670\u06D6-\u06ED]", "");
        // Normalize Arabic/Persian variants
        s = s
                .replace('\u064A', '\u06CC') // ي -> ی
                .replace('\u0643', '\u06A9') // ك -> ک
                .replace('\u0622', '\u0627') // آ -> ا
                .replace('\u0623', '\u0627') // أ -> ا
                .replace('\u0625', '\u0627') // إ -> ا
                .replace('\u0649', '\u06CC'); // ى -> ی
        // Trim and lowercase (safe for Latin; Persian/Arabic case-insensitive inherently)
        return s.trim().toLowerCase();
    }
}

