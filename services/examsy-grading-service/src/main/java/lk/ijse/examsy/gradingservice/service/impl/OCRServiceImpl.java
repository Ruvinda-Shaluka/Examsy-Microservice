package lk.ijse.examsy.gradingservice.service.impl;

import lk.ijse.examsy.gradingservice.service.OCRService;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.URI;

@Service
@Slf4j
public class OCRServiceImpl implements OCRService {

    @Value("${tesseract.datapath:C:\\Program Files\\Tesseract-OCR\\tessdata}")
    private String tesseractDatapath;

    @Value("${tesseract.language:eng}")
    private String tesseractLanguage;

    @Override
    public String extractTextFromPdfUrl(String pdfUrl) {
        StringBuilder extractedText = new StringBuilder();

        try {
            log.info("Opening remote PDF stream from: {}", pdfUrl);
            InputStream in = URI.create(pdfUrl).toURL().openStream();
            PDDocument document = PDDocument.load(in);
            PDFRenderer pdfRenderer = new PDFRenderer(document);

            ITesseract tesseract = new Tesseract();
            tesseract.setDatapath(tesseractDatapath);
            tesseract.setLanguage(tesseractLanguage);

            int pageCount = document.getNumberOfPages();
            log.info("Processing {} page(s) for OCR extraction...", pageCount);

            for (int page = 0; page < pageCount; page++) {
                BufferedImage bim = pdfRenderer.renderImageWithDPI(page, 300, ImageType.RGB);
                String text = tesseract.doOCR(bim);
                extractedText.append(text).append("\n\n");
            }

            document.close();
            in.close();

            String finalCleanedText = cleanOcrText(extractedText.toString());
            log.info("Successfully extracted {} characters of text from PDF.", finalCleanedText.length());
            return finalCleanedText;

        } catch (TesseractException e) {
            log.error("OCR Engine failed to read handwriting or tessdata not found: {}", e.getMessage());
            return "ERROR: OCR Engine could not read handwriting. Details: " + e.getMessage();
        } catch (Exception e) {
            log.error("Failed to process the PDF document at {}: {}", pdfUrl, e.getMessage());
            return "ERROR: Could not process the PDF document. Details: " + e.getMessage();
        }
    }

    /**
     * OCR Pre-processing & Sanitization Pipeline.
     * Cleans up garbage characters, irregular spacing, and formatting artifacts.
     */
    private String cleanOcrText(String rawText) {
        if (rawText == null) return "";

        return rawText
                // Remove non-printable ASCII characters (preserve standard text, numbers, punctuation, and newlines)
                .replaceAll("[^\\x20-\\x7e\\x0A\\x0D]", "")
                // Replace multiple spaces with a single space
                .replaceAll(" +", " ")
                // Replace 3 or more consecutive newlines with just 2
                .replaceAll("\\n{3,}", "\n\n")
                // Trim leading/trailing whitespace
                .trim();
    }
}
