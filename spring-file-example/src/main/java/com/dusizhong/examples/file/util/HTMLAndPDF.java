package com.dusizhong.examples.file.util;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Utilities;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.XMLWorker;
import com.itextpdf.tool.xml.XMLWorkerFontProvider;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import com.itextpdf.tool.xml.css.CssFile;
import com.itextpdf.tool.xml.css.StyleAttrCSSResolver;
import com.itextpdf.tool.xml.html.CssAppliers;
import com.itextpdf.tool.xml.html.CssAppliersImpl;
import com.itextpdf.tool.xml.html.Tags;
import com.itextpdf.tool.xml.parser.XMLParser;
import com.itextpdf.tool.xml.pipeline.css.CSSResolver;
import com.itextpdf.tool.xml.pipeline.css.CssResolverPipeline;
import com.itextpdf.tool.xml.pipeline.end.PdfWriterPipeline;
import com.itextpdf.tool.xml.pipeline.html.HtmlPipeline;
import com.itextpdf.tool.xml.pipeline.html.HtmlPipelineContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * HTML转PDF
 */
public class HTMLAndPDF {
    public static final String PDF = "pdf/web.pdf";
    public static final String PDF2 = "pdf/web2.pdf";
    public static final String PDF3 = "pdf/web3.pdf";
    public static final String PDF4 = "pdf/web4.pdf";
    public static final String HTML = "pdf/web.html";
    public static final String CSS = "pdf/web.css";

    public static void main(String[] args) throws IOException, DocumentException {
        File file = new File(PDF);
        file.getParentFile().mkdirs();
        new HTMLAndPDF().createPdf(PDF);

        file = new File(PDF2);
        file.getParentFile().mkdirs();
        new HTMLAndPDF().createPdf2(PDF2);

        file = new File(PDF3);
        file.getParentFile().mkdirs();
        new HTMLAndPDF().createPdf3(PDF3);

        file = new File(PDF4);
        file.getParentFile().mkdirs();
        new HTMLAndPDF().createPdf4(PDF4);

    }

    /**
     * 原封不动转换
     * @param file
     * @throws IOException
     * @throws DocumentException
     */
    public void createPdf(String file) throws IOException, DocumentException {
        // step 1
        Document document = new Document();
        // step 2
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));
        writer.setInitialLeading(12);
        // step 3
        document.open();
        // step 4
        XMLWorkerHelper.getInstance().parseXHtml(writer, document,
                new FileInputStream(HTML), Charset.forName("UTF-8"));
        // step 5
        document.close();
    }

    /**
     * 引入额外的css
     * @param file
     * @throws IOException
     * @throws DocumentException
     */
    public void createPdf2(String file) throws IOException, DocumentException {
        // step 1
        Document document = new Document();
        // step 2
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));
        writer.setInitialLeading(12);
        // step 3
        document.open();
        // step 4
        XMLWorkerHelper.getInstance().parseXHtml(writer, document,
                new FileInputStream(HTML));

        String html = Utilities.readFileToString(HTML);
        String css = "ul { list-style: disc } li { padding: 10px }";
        PdfPTable table = new PdfPTable(1);
        table.setSpacingBefore(20);
        PdfPCell cell = new PdfPCell();
        for (Element e : XMLWorkerHelper.parseToElementList(html, css)) {
            cell.addElement(e);
        }
        table.addCell(cell);
        document.add(table);
        // step 5
        document.close();
    }

    /**
     * 引入外部css
     * @param file
     * @throws IOException
     * @throws DocumentException
     */
    public void createPdf3(String file) throws IOException, DocumentException {
        // step 1
        Document document = new Document();

        // step 2
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));
        writer.setInitialLeading(12.5f);

        // step 3
        document.open();

        // step 4

        // CSS
        CSSResolver cssResolver = new StyleAttrCSSResolver();
        CssFile cssFile = XMLWorkerHelper.getCSS(new FileInputStream(CSS));
        cssResolver.addCss(cssFile);

        // HTML
        HtmlPipelineContext htmlContext = new HtmlPipelineContext(null);
        htmlContext.setTagFactory(Tags.getHtmlTagProcessorFactory());

        // Pipelines
        PdfWriterPipeline pdf = new PdfWriterPipeline(document, writer);
        HtmlPipeline html = new HtmlPipeline(htmlContext, pdf);
        CssResolverPipeline css = new CssResolverPipeline(cssResolver, html);

        // XML Worker
        XMLWorker worker = new XMLWorker(css, true);
        XMLParser p = new XMLParser(worker);
        p.parse(new FileInputStream(HTML));

        // step 5
        document.close();
    }

    /**
     * 处理中文（引入外部字体文件）
     * @param file
     * @throws IOException
     * @throws DocumentException
     */
    public void createPdf4(String file) throws IOException, DocumentException {
        // step 1
        Document document = new Document();

        // step 2
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));
        writer.setInitialLeading(12.5f);

        // step 3
        document.open();

        // step 4

        // CSS
        CSSResolver cssResolver = new StyleAttrCSSResolver();
        CssFile cssFile = XMLWorkerHelper.getCSS(new FileInputStream(CSS));
        cssResolver.addCss(cssFile);

        // HTML
        XMLWorkerFontProvider fontProvider = new XMLWorkerFontProvider(XMLWorkerFontProvider.DONTLOOKFORFONTS);
        fontProvider.register("pdf/华庚少女字体.ttf", "girl");    // 字体别名，在web.html使用
        CssAppliers cssAppliers = new CssAppliersImpl(fontProvider);
        HtmlPipelineContext htmlContext = new HtmlPipelineContext(cssAppliers);
        htmlContext.setTagFactory(Tags.getHtmlTagProcessorFactory());

        // Pipelines
        PdfWriterPipeline pdf = new PdfWriterPipeline(document, writer);
        HtmlPipeline html = new HtmlPipeline(htmlContext, pdf);
        CssResolverPipeline css = new CssResolverPipeline(cssResolver, html);

        // XML Worker
        XMLWorker worker = new XMLWorker(css, true);
        XMLParser p = new XMLParser(worker);
        p.parse(new FileInputStream(HTML), Charset.forName("UTF-8"));
        // step 5
        document.close();
    }

}