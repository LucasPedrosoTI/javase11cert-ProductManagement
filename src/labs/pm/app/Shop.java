package labs.pm.app;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.logging.Logger;

import labs.pm.app.data.Product;
import labs.pm.app.data.ProductManager;
import labs.pm.app.data.Rating;

public class Shop {

	public static void main(String[] args) {
		
		final Logger logger = Logger.getGlobal();
		ProductManager pm = new ProductManager();
		
		Product p1 = pm.createProduct(101, "Tea", BigDecimal.valueOf(1.99), Rating.NOT_RATED);
		Product p2 = pm.createProduct(102, "Coffee", BigDecimal.valueOf(1.99), Rating.FOUR_STARS);
		Product p3 = pm.createProduct(103, "Cake", BigDecimal.valueOf(3.99), Rating.FIVE_STARS, LocalDate.now().plusDays(2));
		Product p4 = pm.createProduct(105, "Cookie", BigDecimal.valueOf(3.99), Rating.TWO_STARS, LocalDate.now());
		Product p5 = p3.applyRating(Rating.THREE_STARS);

//		p1.setId(101);
//		p1.setName("Tea");
//		p1.setPrice(BigDecimal.valueOf(1.99))
		
		

		logger.info(p1.toString());
		System.out.println(p2.getBestBefore());
		System.out.println(p3.getBestBefore());
		System.out.println(p1);
		System.out.println(p2);
		System.out.println(p3);
		System.out.println(p4);
		System.out.println(p5);

	}

}
