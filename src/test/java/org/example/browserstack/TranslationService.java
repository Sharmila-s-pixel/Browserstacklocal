package org.example.browserstack;

import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import org.apache.commons.text.StringEscapeUtils;

import java.io.FileInputStream;
import java.io.IOException;

public class TranslationService {

    private static final String CREDENTIALS_PATH = "C:/Users/Sharmila/Downloads/browserstack2-f535977a9b9c.json";
    public static String translateToEnglish(String text) {
        try {
            Translate translate = TranslateOptions.newBuilder()
                    .setCredentials(ServiceAccountCredentials.fromStream(new FileInputStream(CREDENTIALS_PATH)))
                    .build()
                    .getService();

            Translation translation = translate.translate(
                    text,
                    Translate.TranslateOption.sourceLanguage("es"),
                    Translate.TranslateOption.targetLanguage("en")
            );

            // Decode any HTML entities (e.g., &#39; => ')
            return StringEscapeUtils.unescapeHtml4(translation.getTranslatedText());

        } catch (IOException e) {
            System.out.println("Failed to load credentials: " + e.getMessage());
            return text;
        } catch (Exception e) {
            System.out.println("Translation failed: " + e.getMessage());
            return text;
        }
    }
}
