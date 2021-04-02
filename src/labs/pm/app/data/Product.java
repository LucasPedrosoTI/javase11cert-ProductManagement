package labs.pm.app.data;

import static java.math.RoundingMode.HALF_UP;

import java.math.BigDecimal;

public class Product {
	
	private int id;
	private String name;
	private BigDecimal price;
	public static final BigDecimal DISCOUNT_RATE = BigDecimal.valueOf(0.1);  
	
	public int getId() {
		return id;
	}
	public void setId(final int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(final String name) {
		this.name = name;
	}
	public BigDecimal getPrice() {
		return price;
	}
	public void setPrice(final BigDecimal price) {
		this.price = price;
	}
	
	public BigDecimal getDiscount() {
		return price.multiply(DISCOUNT_RATE).setScale(2, HALF_UP);
	}
}
