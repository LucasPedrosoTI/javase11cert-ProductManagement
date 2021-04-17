package labs.pm.app;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.function.Predicate;
import java.util.logging.Logger;

import labs.pm.data.Product;
import labs.pm.data.ProductManager;
import labs.pm.data.Rating;

public class Shop {
	public static final Logger LOGGER = Logger.getGlobal();

	public static void main(String[] args) {

		ProductManager pm = new ProductManager("pt-BR");

		pm.createProduct(101, "Tea", BigDecimal.valueOf(1.99), Rating.NOT_RATED);
		pm.printProductReport(101);
		pm.reviewProduct(101, Rating.FOUR_STARS, "Nice hot cup of tea");
		pm.reviewProduct(101, Rating.TWO_STARS, "Bad hot cup of tea");
		pm.reviewProduct(101, Rating.ONE_STAR, "Cool hot cup of tea");
		pm.reviewProduct(101, Rating.ONE_STAR, "Great hot cup of tea");
		pm.reviewProduct(101, Rating.FIVE_STARS, "Excellent hot cup of tea");
		pm.reviewProduct(101, Rating.THREE_STARS, "Average hot cup of tea");
		pm.printProductReport(101);

		pm.createProduct(103, "Coke", BigDecimal.valueOf(3.99), Rating.NOT_RATED);
		pm.reviewProduct(103, Rating.FIVE_STARS, "Nice hot cup of tea");
		pm.reviewProduct(103, Rating.FIVE_STARS, "Nice hot cup of tea");
		// pm.printProductReport(103);

		pm.createProduct(102, "Coffee", BigDecimal.valueOf(2.99), Rating.NOT_RATED);
		pm.reviewProduct(102, Rating.FOUR_STARS, "Nice hot cup of tea");
		pm.reviewProduct(102, Rating.FOUR_STARS, "Nice hot cup of tea");
		pm.reviewProduct(102, Rating.FOUR_STARS, "Nice hot cup of tea");
		// pm.printProductReport(102);

		pm.changeLocale("en-US");

		try {
			Product p1 = pm.findProduct(102);
			Product p2 = pm.findProduct(104);
			LOGGER.info(() -> "Found product: " + p1.toString());
			LOGGER.info(() -> "Found product: " + p2.toString());
		} catch (Exception e) {
			LOGGER.severe("Product not found");
			LOGGER.severe(e.getMessage());
		}

		// orderna por preco DESC
		final Comparator<Product> priceSorterDesc = (Product p1, Product p2) -> p2.getPrice().compareTo(p1.getPrice());
		final Predicate<Product> priceFilter = p -> p.getPrice().doubleValue() > 2;
		pm.printProducts(priceFilter, priceSorterDesc);
		// ORDENA POR NOME ASC e DESC
		// pm.printProducts((var p1, var p2) ->
		// p1.getName().compareToIgnoreCase(p2.getName()));
		// pm.printProducts((p1, p2) -> p2.getName().compareToIgnoreCase(p1.getName()));
		// ORDENA POR RATING ASC E DESC
		final Comparator<Product> ratingSorterAsc = (p1, p2) -> p1.getRating().compareTo(p2.getRating());
		// pm.printProducts(ratingSorterAsc);
		// pm.printProducts((p1, p2) -> p2.getRating().compareTo(p1.getRating()));

		// pm.printProducts(ratingSorterAsc.thenComparing(priceSorterDesc));
		// pm.printProducts(ratingSorterAsc.thenComparing(priceSorterDesc).reversed());

		pm.getDiscounts().forEach((rating, discount) -> LOGGER.info(rating + "\t" + discount));

	}

}
