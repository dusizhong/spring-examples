package com.dusizhong.examples.file.util;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.html.HtmlWriter;

import java.io.FileOutputStream;
import java.io.IOException;

public class OpenPdfUtils {

    public static void main(String[] args) {

        System.out.println("Hello World");

        // step 1: creation of a document-object
        Document document = new Document();
        try {
            // step 2:
            // we create a writer that listens to the document
            // and directs a HTML-stream to a file
            HtmlWriter.getInstance(document, new FileOutputStream("/helloworld.html"));

            // step 3: we open the document
            document.open();
            // step 4: we add a paragraph to the document
            document.add(new Paragraph("程序添加内容：Hello World"));
        } catch (DocumentException | IOException de) {
            System.err.println(de.getMessage());
        }

        // step 5: we close the document
        document.close();
    }
}
