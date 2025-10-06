
package com.example.orders;

public class OrderEvent {
    private String orderId;
    private String symbol;
    private String side; // BUY / SELL
    private int qty;
    private double price;

    public OrderEvent() {}

    public OrderEvent(String orderId, String symbol, String side, int qty, double price) {
        this.orderId = orderId;
        this.symbol = symbol;
        this.side = side;
        this.qty = qty;
        this.price = price;
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public String getSide() { return side; }
    public void setSide(String side) { this.side = side; }
    public int getQty() { return qty; }
    public void setQty(int qty) { this.qty = qty; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
}
