package labs.pm.app;

import java.math.BigDecimal;

import labs.pm.app.data.Product;
import labs.pm.app.data.Rating;

public class Shop {

	public static void main(String[] args) {
		
		Product p1 = new Product(101, "Tear", BigDecimal.valueOf(1.99));
		Product p2 = new Product(102, "Coffee", BigDecimal.valueOf(1.99), Rating.FOUR_STARS);
		Product p3 = new Product(103, "Cake", BigDecimal.valueOf(3.99), Rating.FIVE_STARS);
		Product p4 = new Product();
		Product p5 = p3.applyRating(Rating.THREE_STARS);
		
//		p1.setId(101);
//		p1.setName("Tea");
//		p1.setPrice(BigDecimal.valueOf(1.99))
		
		System.out.println(p1);
		System.out.println(p2);
		System.out.println(p3);
		System.out.println(p4);
		System.out.println(p5);
		
	}

}
