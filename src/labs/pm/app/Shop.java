package labs.pm.app;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.logging.Logger;

import labs.pm.data.ProductManager;
import labs.pm.data.Rating;

public class Shop {
	public static final Logger LOGGER = Logger.getGlobal();
	public static void main(String[] args) {

		// final Logger logger = Logger.getGlobal();
		ProductManager pm = new ProductManager(Locale.getDefault());

		pm.createProduct(101, "Tea", BigDecimal.valueOf(1.99), Rating.NOT_RATED);
		pm.printProductReport(101);
		pm.reviewProduct(101, Rating.FOUR_STARS, "Nice hot cup of tea");
		pm.reviewProduct(101, Rating.TWO_STARS, "Bad hot cup of tea");
		pm.reviewProduct(101, Rating.ONE_STAR, "Cool hot cup of tea");
		pm.reviewProduct(101, Rating.ONE_STAR, "Great hot cup of tea");
		pm.reviewProduct(101, Rating.FIVE_STARS, "Excellent hot cup of tea");
		pm.reviewProduct(101, Rating.THREE_STARS, "Average hot cup of tea");
		pm.printProductReport(101);

		pm.createProduct(102, "Coffee", BigDecimal.valueOf(1.99), Rating.NOT_RATED);
		pm.reviewProduct(102, Rating.FOUR_STARS, "Nice hot cup of tea");
		pm.reviewProduct(102, Rating.FOUR_STARS, "Nice hot cup of tea");
		pm.reviewProduct(102, Rating.FOUR_STARS, "Nice hot cup of tea");
		pm.printProductReport(102);

		LOGGER.info(pm.findProduct(101).toString());
		
		pm.createProduct(103, "Coke", BigDecimal.valueOf(1.99), Rating.NOT_RATED);
		pm.reviewProduct(103, Rating.FOUR_STARS, "Nice hot cup of tea");
		pm.reviewProduct(103, Rating.FOUR_STARS, "Nice hot cup of tea");
		pm.reviewProduct(103, Rating.FOUR_STARS, "Nice hot cup of tea");
		pm.printProductReport(103);
		// Product 102 = pm.createProduct(102, "Coffee", BigDecimal.valueOf(1.99), Rating.FOUR_STARS);
		// Product p3 = pm.createProduct(103, "Cake", BigDecimal.valueOf(3.99), Rating.FIVE_STARS,
		// 		LocalDate.now().plusDays(2));
		// Product p4 = pm.createProduct(105, "Cookie", BigDecimal.valueOf(3.99), Rating.TWO_STARS, LocalDate.now());
		// Product p5 = p3.applyRating(Rating.THREE_STARS);

		// // p1.setId(101);
		// // p1.setName("Tea");
		// // p1.setPrice(BigDecimal.valueOf(1.99))

		// LOGGER.info(p1.toString());
		// System.out.println(102.getBestBefore());
		// System.out.println(p3.getBestBefore());
		// System.out.println(p1);
		// System.out.println(102);
		// System.out.println(p3);
		// System.out.println(p4);
		// System.out.println(p5);

	}

}
