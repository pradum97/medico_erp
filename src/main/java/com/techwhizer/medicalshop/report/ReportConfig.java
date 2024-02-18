package com.techwhizer.medicalshop.report;

import com.itextpdf.text.*;
import com.itextpdf.text.html.WebColors;
import com.itextpdf.text.pdf.*;
import com.techwhizer.medicalshop.HeaderFooterWithTable;

import java.io.FileOutputStream;
import java.io.IOException;

public class ReportConfig {

    public static Document GetDocument(String path){

        Document document = new Document(PageSize.A4, 15, 15, 100, 15);
        try {
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(path));

            ReportHeader event = new ReportHeader();
            writer.setPageEvent(event);
            document.open();

        } catch (DocumentException | IOException e) {
            e.printStackTrace();
        }

        return document;
    }

    private static class FooterEvent extends PdfPageEventHelper {
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfPTable footer = new PdfPTable(1);
            PdfPCell cell = new PdfPCell(new Paragraph("This is the footer"));
            cell.setBorder(0);
            footer.addCell(cell);

            footer.setTotalWidth(document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin());
            footer.writeSelectedRows(0, -1, document.leftMargin(), document.bottomMargin(), writer.getDirectContent());
        }
    }


    public void setPageBorder(PdfWriter writer, BaseColor color, float width) {
        PdfContentByte contentByte = writer.getDirectContent();
        contentByte.setColorStroke(color);
        contentByte.setLineWidth(width);

        // Set the coordinates of the rectangle (border) on the page
        float x1 = 15; // left
        float y1 = 15; // bottom
        float x2 = PageSize.A4.getWidth() - x1; // right
        float y2 = PageSize.A4.getHeight() - y1; // top

        contentByte.rectangle(x1, y1, x2 - x1, y2 - y1);
        contentByte.stroke();
    }


    public static PdfPCell getColumnCell(String text, int alignment) {

        BaseColor columnBaseColor = WebColors.getRGBColor("#cc0052");
        Font columnFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Font.BOLD, columnBaseColor);
        columnFont.setFamily("Arial");

        PdfPCell cell = new PdfPCell(new Phrase(text,columnFont));
        cell.setPaddingBottom(6);
        cell.setPaddingRight(6);
        cell.setPaddingLeft(4);
        cell.setPaddingTop(4);
        cell.setHorizontalAlignment(alignment);
        cell.setBorderColor(BaseColor.GRAY);
        cell.setBorderWidth(0.7f);
        return cell;
    }

    public static PdfPCell getRowCell(String text, int alignment) {
        BaseColor rowBaseColor = WebColors.getRGBColor("#0d0033");
        Font rowFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Font.NORMAL, rowBaseColor);
        rowFont.setFamily("Arial");

        PdfPCell cell = new PdfPCell(new Phrase(text,rowFont));
        cell.setPaddingBottom(6);
        cell.setPaddingRight(6);
        cell.setPaddingLeft(4);
        cell.setPaddingTop(4);
        cell.setHorizontalAlignment(alignment);
        cell.setBorderColor(BaseColor.GRAY);
        cell.setBorderWidth(0.7f);
        return cell;
    }
}
