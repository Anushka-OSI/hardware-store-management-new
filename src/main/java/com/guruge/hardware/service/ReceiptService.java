package com.guruge.hardware.service;

import com.guruge.hardware.config.AppProperties;
import com.guruge.hardware.dto.response.ReceiptResponse;
import com.guruge.hardware.entity.Payment;
import com.guruge.hardware.entity.Sale;
import com.guruge.hardware.entity.SaleItem;
import com.guruge.hardware.entity.User;
import com.guruge.hardware.exception.ResourceNotFoundException;
import com.guruge.hardware.repository.SaleRepository;
import com.guruge.hardware.repository.UserRepository;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ReceiptService {

    private final SaleRepository saleRepository;
    private final UserRepository userRepository;
    private final AppProperties appProperties;

    @Transactional(readOnly = true)
    public ReceiptResponse receiptData(Long saleId) {
        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new ResourceNotFoundException("Sale", "id", saleId));
        String cashierName = userRepository.findById(sale.getCashierId())
                .map(User::getFullName).orElse("Cashier #" + sale.getCashierId());
        BigDecimal paid = sale.getPayments() != null
                ? sale.getPayments().stream().map(Payment::getAmountPaid)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                : BigDecimal.ZERO;
        BigDecimal balance = sale.getPayments() != null && !sale.getPayments().isEmpty()
                ? sale.getPayments().get(0).getBalance() : BigDecimal.ZERO;
        String method = sale.getPayments() != null && !sale.getPayments().isEmpty()
                ? sale.getPayments().get(0).getPaymentMethod() : "CASH";
        return ReceiptResponse.builder()
                .receiptNumber(sale.getReceiptNumber())
                .storeName(appProperties.getBusiness().getStoreName())
                .customerName(sale.getCustomerName())
                .customerPhone(sale.getCustomerPhone())
                .cashierName(cashierName)
                .subtotal(sale.getSubtotal())
                .discountAmount(sale.getDiscountAmount())
                .totalAmount(sale.getTotalAmount())
                .amountPaid(paid)
                .balance(balance)
                .paymentMethod(method)
                .createdAt(sale.getCreatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public byte[] receiptPdf(Long saleId) {
        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new ResourceNotFoundException("Sale", "id", saleId));
        ReceiptResponse data = receiptData(saleId);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A5, 24, 24, 24, 24);
            PdfWriter.getInstance(doc, out);
            doc.open();

            Font title = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font normal = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Font bold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);

            Paragraph store = new Paragraph(data.getStoreName(), title);
            store.setAlignment(Element.ALIGN_CENTER);
            doc.add(store);
            Paragraph meta = new Paragraph(
                    "Receipt: " + data.getReceiptNumber() + "\nDate: " + data.getCreatedAt()
                            + "\nCashier: " + data.getCashierName()
                            + "\nCustomer: " + (data.getCustomerName() != null ? data.getCustomerName() : "-"),
                    normal);
            meta.setAlignment(Element.ALIGN_LEFT);
            meta.setSpacingBefore(8);
            meta.setSpacingAfter(8);
            doc.add(meta);

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{4, 1, 2, 2});
            for (String h : new String[]{"Item", "Qty", "Price", "Total"}) {
                PdfPCell cell = new PdfPCell(new Phrase(h, bold));
                cell.setBackgroundColor(new Color(230, 230, 230));
                table.addCell(cell);
            }
            List<SaleItem> items = sale.getItems();
            if (items != null) {
                for (SaleItem i : items) {
                    table.addCell(new Phrase(i.getProductName(), normal));
                    table.addCell(new Phrase(String.valueOf(i.getQuantity()), normal));
                    table.addCell(new Phrase(String.valueOf(i.getUnitPrice()), normal));
                    table.addCell(new Phrase(String.valueOf(i.getLineTotal()), normal));
                }
            }
            doc.add(table);

            Paragraph totals = new Paragraph(
                    "\nSubtotal: " + data.getSubtotal()
                            + "\nDiscount: " + data.getDiscountAmount()
                            + "\nTotal: " + data.getTotalAmount()
                            + "\nPaid (" + data.getPaymentMethod() + "): " + data.getAmountPaid()
                            + "\nBalance: " + data.getBalance()
                            + "\n\nThank you for shopping with us!",
                    normal);
            totals.setAlignment(Element.ALIGN_RIGHT);
            doc.add(totals);

            doc.close();
            return out.toByteArray();
        } catch (Exception ex) {
            throw new RuntimeException("Failed to generate receipt PDF: " + ex.getMessage(), ex);
        }
    }

    private void unused(Stream<String> s) {
        // keeps import used if tooling flags it
    }
}
