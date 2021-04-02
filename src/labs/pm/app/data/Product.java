package labs.pm.app.data;

import static java.math.RoundingMode.HALF_UP;
import static labs.pm.app.data.Rating.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public abstract class Product {

	public static final BigDecimal DISCOUNT_RATE = BigDecimal.valueOf(0.1);
	private final int id; // immutable class
	private final String name;
	private final BigDecimal price;
	private final Rating rating;

	 Product(int id, String name, BigDecimal price, Rating rating) {
		this.id = id;
		this.name = name;
		this.price = price;
		this.rating = rating;
	}

	 Product(int id, String name, BigDecimal price) {
		this(id, name, price, NOT_RATED);
	}

	//  Product() {
	// 	this(0, "no name", BigDecimal.ZERO);
	// }

	public int getId() {
		return id;
	}

	// public void setId(final int id) {
	// this.id = id;
	// }

	public String getName() {
		return name;
	}

	// public void setName(final String name) {
	// this.name = name;
	// }

	public BigDecimal getPrice() {
		return price;
	}

	// public void setPrice(final BigDecimal price) {
	// this.price = price;
	// }

	public Rating getRating() {
		return rating;
	}

	public abstract Product applyRating(Rating newRating);

	public BigDecimal getDiscount() {
		return price.multiply(DISCOUNT_RATE).setScale(2, HALF_UP);
	}
	
	public LocalDate getBestBefore() {
		return LocalDate.now();
	}

	@Override
	public String toString() {
		return "Product [id=" + id + ", name=" + name + ", price=" + price + ", rating=" + rating + ", bestBefore =" + getBestBefore() + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name, price, rating);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		Product other = (Product) obj;
		return id == other.id && Objects.equals(name, other.name) && Objects.equals(price, other.price)
				&& rating == other.rating;
	}
	
	

}
