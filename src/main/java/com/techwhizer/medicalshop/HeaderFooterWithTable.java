package com.techwhizer.medicalshop;

import com.itextpdf.text.*;
import com.itextpdf.text.html.WebColors;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;
import com.techwhizer.medicalshop.report.ReportConfig;

import java.io.FileOutputStream;
import java.io.IOException;

public class HeaderFooterWithTable  {

    public static void main(String[] args) {

        Document document = ReportConfig.GetDocument("output.pdf");

        try {

            Paragraph title = new Paragraph("REPORT ON [17-02-2023]");
            title.setAlignment(Paragraph.ALIGN_CENTER);
            Font titleFont = new Font();
            titleFont.setStyle(Font.BOLD);
            titleFont.setColor(BaseColor.RED);
            title.setFont(titleFont);
            document.add(title);

            document.add(new Paragraph("\n"));

            PdfPTable table = new PdfPTable(6);

            table.setWidthPercentage(new float[]{40,215, 90, 70,80,80}, PageSize.A4);
            // Set border color to gray
            table.getDefaultCell().setBorderColor(BaseColor.GRAY);
            table.getDefaultCell().setBorderWidth(0.7f);

            // Add headers
            table.addCell(ReportConfig.getColumnCell("#",PdfPCell.ALIGN_MIDDLE));
            table.addCell(ReportConfig.getColumnCell("Item Name",PdfPCell.LEFT));
            table.addCell(ReportConfig.getColumnCell("Batch",PdfPCell.ALIGN_MIDDLE));
            table.addCell(ReportConfig.getColumnCell("Quantity",PdfPCell.ALIGN_MIDDLE));
            table.addCell(ReportConfig.getColumnCell("Total Items",PdfPCell.ALIGN_MIDDLE));
            table.addCell(ReportConfig.getColumnCell("Amount",PdfPCell.ALIGN_RIGHT));


            for (int i = 0; i < 100; i++) {
                // Add rows
                table.addCell(ReportConfig.getRowCell(String.valueOf(i),PdfPCell.ALIGN_MIDDLE));
                table.addCell(ReportConfig.getRowCell("2",PdfPCell.LEFT));
                table.addCell(ReportConfig.getRowCell("3",PdfPCell.ALIGN_MIDDLE));
                table.addCell(ReportConfig.getRowCell("4",PdfPCell.ALIGN_MIDDLE));
                table.addCell(ReportConfig.getRowCell("5",PdfPCell.ALIGN_MIDDLE));
                table.addCell(ReportConfig.getRowCell("6",PdfPCell.ALIGN_RIGHT));
            }

            // Add the table to the document
            document.add(table);

            PdfPTable footerTable = new PdfPTable(2);

            footerTable.setWidthPercentage(new float[]{345,230}, PageSize.A4);
            footerTable.getDefaultCell().setBorderColor(BaseColor.GRAY);
            footerTable.getDefaultCell().setBorderWidth(0.7f);

            footerTable.addCell(ReportConfig.getColumnCell("TOTAL NET AMOUNT : ",PdfPCell.ALIGN_RIGHT));
            footerTable.addCell(ReportConfig.getColumnCell("1000000000",PdfPCell.ALIGN_RIGHT));


            document.add(footerTable);
        } catch (DocumentException e) {
            throw new RuntimeException(e);
        }finally {
            document.close();
        }


    }




}
