package labs.pm.app.data;

import static java.math.RoundingMode.HALF_UP;
import static labs.pm.app.data.Rating.*;

import java.math.BigDecimal;

public class Product {

	public static final BigDecimal DISCOUNT_RATE = BigDecimal.valueOf(0.1);
	private final int id; // immutable class
	private final String name;
	private final BigDecimal price;
	private final Rating rating;

	public Product(int id, String name, BigDecimal price, Rating rating) {
		this.id = id;
		this.name = name;
		this.price = price;
		this.rating = rating;
	}

	public Product(int id, String name, BigDecimal price) {
		this(id, name, price, NOT_RATED);
	}

	public Product(){
		this(0, "no name", BigDecimal.ZERO);
	}

	public int getId() {
		return id;
	}

	// public void setId(final int id) {
	// 	this.id = id;
	// }

	public String getName() {
		return name;
	}

	// public void setName(final String name) {
	// 	this.name = name;
	// }

	public BigDecimal getPrice() {
		return price;
	}

	// public void setPrice(final BigDecimal price) {
	// 	this.price = price;
	// }

	public Rating getRating() {
		return rating;
	}

	public Product applyRating(Rating newRating){
		return new Product(id, name, price, newRating);
	}

	public BigDecimal getDiscount() {
		return price.multiply(DISCOUNT_RATE).setScale(2, HALF_UP);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Product [id=").append(id).append(", name=").append(name).append(", price=").append(price)
				.append(", rating=").append(rating).append(", discount= ").append(this.getDiscount()).append("]");
		return builder.toString();
	}
	
	

}
