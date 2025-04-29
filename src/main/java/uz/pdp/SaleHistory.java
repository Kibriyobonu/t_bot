package uz.pdp;

import java.time.LocalDateTime;

public class SaleHistory {
    private final Long userId;
    private final String userName;
    private final String bookTitle;
    private final int quantity;
    private final LocalDateTime purchasedAt;

    public SaleHistory(Long userId, String userName, String bookTitle, int quantity, LocalDateTime purchasedAt) {
        this.userId = userId;
        this.userName = userName;
        this.bookTitle = bookTitle;
        this.quantity = quantity;
        this.purchasedAt = purchasedAt;
    }

    public Long getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getBookTitle() { return bookTitle; }
    public int getQuantity() { return quantity; }
    public LocalDateTime getPurchasedAt() { return purchasedAt; }
}

