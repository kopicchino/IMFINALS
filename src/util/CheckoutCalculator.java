package util;

import model.CartItem;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;


public class CheckoutCalculator {
    
    public static final BigDecimal DELIVERY_FEE = new BigDecimal("30.00");
    public static final BigDecimal TAX_RATE = new BigDecimal("0.12"); // std tax in the ph (12%)
    
    
    public static String validateCartItemPrices(List<CartItem> items) {
        if (items == null || items.isEmpty()) {
            return "Cart is empty";
        }
        
        for (CartItem item : items) {
            if (item.hasPriceError()) {
                return "Product '" + item.getProductName() + "' has no valid price.\n" +
                       "Please contact admin to fix this issue.";
            }
        }
        
        return null; // No errors
    }
    
    
    public static String validateStockAvailability(List<CartItem> items) {
        if (items == null || items.isEmpty()) {
            return "Cart is empty";
        }
       
        java.util.Map<Integer, Integer> requested = new java.util.HashMap<>();
        java.util.Map<Integer, CartItem> sampleItem = new java.util.HashMap<>();

        for (CartItem item : items) {
            requested.merge(item.getProductId(), item.getQuantity(), Integer::sum);
            // keep a sample item to read available stock/product name later
            sampleItem.putIfAbsent(item.getProductId(), item);
        }

        for (java.util.Map.Entry<Integer, Integer> entry : requested.entrySet()) {
            int productId = entry.getKey();
            int totalRequested = entry.getValue();
            CartItem item = sampleItem.get(productId);
            int available = item != null ? item.getAvailableStock() : 0;

            // normalize negative values
            if (available < 0) {
                available = 0;
            }

            if (totalRequested > available) {
                String name = item != null ? item.getProductName() : ("Product ID " + productId);
                String msg = "Insufficient stock for '" + name + "'.\n" +
                             "Available: " + available + ", Requested: " + totalRequested;
                // Helpful debug log for developers
                System.out.println("[Checkout] Stock check failed: productId=" + productId + ", requested=" + totalRequested + ", available=" + available);
                return msg;
            }
        }
        
        return null; 
    }
    
  
    public static BigDecimal calculateSubtotal(List<CartItem> items) {
        BigDecimal subtotal = BigDecimal.ZERO;
        
        if (items == null || items.isEmpty()) {
            return subtotal;
        }
        
        for (CartItem item : items) {
            // Get unit price safely, defaulting to ZERO if null
            BigDecimal unitPrice = Objects.requireNonNullElse(item.getUnitPrice(), BigDecimal.ZERO);
            
            // Skip items with zero or negative prices
            if (unitPrice.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal itemTotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
                subtotal = subtotal.add(itemTotal);
            }
        }
        
        return subtotal;
    }
    
  
    public static BigDecimal calculateTax(BigDecimal subtotal) {
        if (subtotal == null) {
            subtotal = BigDecimal.ZERO;
        }
        return subtotal.multiply(TAX_RATE);
    }
    
   
    public static BigDecimal calculateTotal(BigDecimal subtotal, BigDecimal tax, BigDecimal deliveryFee) {
        BigDecimal total = Objects.requireNonNullElse(subtotal, BigDecimal.ZERO);
        total = total.add(Objects.requireNonNullElse(tax, BigDecimal.ZERO));
        total = total.add(Objects.requireNonNullElse(deliveryFee, DELIVERY_FEE));
        return total;
    }
    
   
    public static CheckoutSummary calculateCheckout(List<CartItem> items) {
        CheckoutSummary summary = new CheckoutSummary();
        
        // Validate items
        String priceError = validateCartItemPrices(items);
        if (priceError != null) {
            summary.validationError = priceError;
            return summary;
        }
        
        String stockError = validateStockAvailability(items);
        if (stockError != null) {
            summary.validationError = stockError;
            return summary;
        }
        
        // Calculate totals
        summary.subtotal = calculateSubtotal(items);
        summary.tax = calculateTax(summary.subtotal);
        summary.deliveryFee = DELIVERY_FEE;
        summary.total = calculateTotal(summary.subtotal, summary.tax, summary.deliveryFee);
        summary.isValid = true;
        
        return summary;
    }
    
  
    public static class CheckoutSummary {
        public BigDecimal subtotal = BigDecimal.ZERO;
        public BigDecimal tax = BigDecimal.ZERO;
        public BigDecimal deliveryFee = DELIVERY_FEE;
        public BigDecimal total = BigDecimal.ZERO;
        public boolean isValid = false;
        public String validationError = null;
        
        @Override
        public String toString() {
            return "CheckoutSummary{" +
                    "subtotal=" + subtotal +
                    ", tax=" + tax +
                    ", deliveryFee=" + deliveryFee +
                    ", total=" + total +
                    ", isValid=" + isValid +
                    ", validationError='" + validationError + '\'' +
                    '}';
        }
    }
}
