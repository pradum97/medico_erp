package com.techwhizer.medicalshop.report;

import com.itextpdf.text.*;
import com.itextpdf.text.html.WebColors;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;
import com.techwhizer.medicalshop.HeaderFooterWithTable;

import java.io.FileOutputStream;
import java.io.IOException;

public class ReportHeader extends PdfPageEventHelper {

    public static void REPORT_HEADER(PdfWriter writer) throws IOException, DocumentException {
        PdfPTable header = new PdfPTable(3);

        header.setWidths(new int[]{2, 12, 2});

        header.setTotalWidth(527);
        header.setLockedWidth(true);
        header.getDefaultCell().setFixedHeight(40);
        header.getDefaultCell().setBorder(Rectangle.BOTTOM);
        header.getDefaultCell().setBorderColor(BaseColor.LIGHT_GRAY);
        header.setWidthPercentage(100);
        float size = 45;
        String path = "D:\\TechWhizer\\Project\\medico_erp\\src\\main\\resources\\com\\techwhizer\\medicalshop\\img\\company\\gangotri_company_logo.png";

        BaseColor topRowColor = WebColors.getRGBColor("#cc0052");
        Font topRowFont = FontFactory.getFont(FontFactory.HELVETICA, 18, Font.BOLD, topRowColor);
        topRowFont.setFamily("Arial");

        PdfPCell topCell = new PdfPCell(new Phrase("GANGOTRI MEMORIAL HOSPITAL PVT.LTD", topRowFont));
        topCell.setColspan(3);
        topCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        topCell.setBorder(Rectangle.NO_BORDER);
        topCell.setSpaceCharRatio(2f);
        header.addCell(topCell);

        PdfPCell leftCell = new PdfPCell();
        leftCell.setBorder(Rectangle.NO_BORDER);
        Image leftImage = Image.getInstance(path);
        leftImage.scaleToFit(size, size); // Set the height here
        leftCell.addElement(leftImage);
        leftCell.setPaddingLeft(17);
        header.addCell(leftCell);
        String adress = "Madhepura Ward No-01,Pathraha, 1.3 KM. South From Madhepura Medical College Madhepura, Bihar-852113, Call :- 9341420655, 7755973085";

        PdfPTable verticalContainer = new PdfPTable(1);
        verticalContainer.setWidthPercentage(100);
        verticalContainer.setHorizontalAlignment(Element.ALIGN_CENTER);
        Font addressFont = FontFactory.getFont(FontFactory.TIMES, 12);
        addressFont.setFamily("Arial");

        PdfPCell addressCell = new PdfPCell(new Phrase(adress, addressFont));
        addressCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        addressCell.setBorder(Rectangle.NO_BORDER);
        verticalContainer.addCell(addressCell);

        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER); // Set horizontal alignment to center
        cell.setVerticalAlignment(PdfPCell.TOP); // Set vertical alignment to middle
        Image image = Image.getInstance("D:\\TechWhizer\\Project\\medico_erp\\src\\main\\resources\\com\\techwhizer\\medicalshop\\img\\company\\subtitle.png");
        image.scaleToFit(120,120);
        image.setAlignment(Image.MIDDLE); // Set alignment
        cell.addElement(image);

        verticalContainer.addCell(cell);

        PdfPCell containerCell = new PdfPCell(verticalContainer);

        containerCell.setBorder(Rectangle.NO_BORDER);
        header.addCell(containerCell);

        PdfPCell rightCell = new PdfPCell();
        rightCell.setBorder(Rectangle.NO_BORDER);
        Image rightImage = Image.getInstance(path);
        rightImage.scaleToFit(size, size);
        rightCell.addElement(rightImage);
        rightCell.setPaddingRight(30);
        header.addCell(rightCell);

        PdfPCell borderCell = new PdfPCell();
        borderCell.setColspan(20);
        borderCell.setBorder(Rectangle.BOTTOM);
        borderCell.setBorderColor(BaseColor.GRAY);
        borderCell.setFixedHeight(5);
        header.addCell(borderCell);

        header.writeSelectedRows(0, -1, 34, 825, writer.getDirectContent());
    }

    @Override
    public void onEndPage(PdfWriter writer, Document document) {

        try {
            REPORT_HEADER(writer);
          //  document.add(new Paragraph("\n\n\n\n\n"));
            new ReportConfig().setPageBorder(writer, BaseColor.GRAY, 1f);
        } catch (IOException | DocumentException e) {
            throw new RuntimeException(e);
        }
    }



}
