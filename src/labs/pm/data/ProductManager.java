package labs.pm.data;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import labs.pm.app.Shop;

public class ProductManager {

	private Map<Product, List<Review>> products = new HashMap<>();
	private ResourceFormatter formatter;
	private static final char NEW_LINE = '\n';
	private static Map<String, ResourceFormatter> formatters = Map.of("en-GB", new ResourceFormatter(Locale.UK),
			"en-US", new ResourceFormatter(Locale.US),
			"pt-BR", new ResourceFormatter(new Locale("pt", "BR")),
			"es-EN", new ResourceFormatter(new Locale("es", "EN")));

	public ProductManager(Locale locale) {
		this(locale.toLanguageTag());
	}

	public ProductManager(String languageTag) {
		changeLocale(languageTag);
	}

	public void changeLocale(String languageTag) {
		formatter = formatters.getOrDefault(languageTag, formatters.get("pt-BR"));
	}

	public static Set<String> getSupportedLocales() {
		return formatters.keySet();
	}

	public Product createProduct(int id, String name, BigDecimal price, Rating rating, LocalDate bestBefore) {
		Product product = new Food(id, name, price, rating, bestBefore);
		products.putIfAbsent(product, new ArrayList<>());
		return product;
	}

	public Product createProduct(int id, String name, BigDecimal price, Rating rating) {
		Product product = new Drink(id, name, price, rating);
		products.putIfAbsent(product, new ArrayList<>());
		return product;
	}

	public Product reviewProduct(Product product, Rating rating, String comments) {
		// this.review = new Review(rating, comments);
		// if (reviews[reviews.length - 1] != null) {
		// reviews = Arrays.copyOf(reviews, reviews.length + 5);
		// }
		List<Review> reviews = products.get(product);
		products.remove(product, reviews);
		reviews.add(new Review(rating, comments));
		Long average = Math.round(reviews
				.stream()
				.mapToInt(r -> r.getRating().ordinal())
				.average()
				.orElse(0));
		//		for (Review review : reviews) {
		//			sum += review.getRating().ordinal();
		//		}
		// boolean reviewed = false;
		// while (i < reviews.length && !reviewed) {
		// if (reviews[i] == null) {
		// reviews[i] = new Review(rating, comments);
		// reviewed = true;
		// }
		// sum += reviews[i].getRating().ordinal();
		// i++;
		// }

		// this.product = product.applyRating(rating);
		//				Math.round((float) sum / reviews.size())
		product = product.applyRating(Rateable.convert(average.intValue()));
		products.put(product, reviews);
		return product;
	}

	public void printProductReport(Product product) {
		StringBuilder txt = new StringBuilder();
		List<Review> reviews = products.get(product);
		Collections.sort(reviews);

		txt.append(formatter.formatProduct(product)).append(NEW_LINE);

		if (reviews.isEmpty()) {
			txt.append(formatter.getText("no.reviews")).append(NEW_LINE);
		} else {
			// reviews.forEach(review ->
			// txt.append(formatter.formatReview(review)).append(NEW_LINE)); // bad when
			// using parallelism
			txt.append(reviews
					.stream()
					.map(r -> formatter.formatReview(r) + NEW_LINE)
					.collect(Collectors.joining())); // better for pararellism, because the joining is done altogether
			// after the conversions
		}


		Shop.LOGGER.info(txt::toString);
	}

	public void printProducts(Predicate<Product> filter, Comparator<Product> sorter) {
		//		List<Product> productList = new ArrayList<>(products.keySet());
		//		productList.sort(sorter);
		// for (Product product : productList) {
		// txt.append(formatter.formatProduct(product)).append(NEW_LINE);
		// }

		StringBuilder txt = new StringBuilder();
		txt.append(products.keySet()
				.stream()
				.sorted(sorter)
				.filter(filter)
				.map(p -> formatter.formatProduct(p) + NEW_LINE)
				.collect(Collectors.joining()));


		Shop.LOGGER.warning(txt::toString);

	}

	public Product findProduct(int id) {
		// Iterator<Product> it = products.keySet().iterator();
		// while (it.hasNext()) {
		// Product product = it.next();
		// if (product.getId() == id) {
		// return product;
		// }
		// }
		// return null;
		return products.keySet()
				.stream()
				.filter(p -> p.getId() == id)
				.findFirst()
				.orElse(null);
		// .orElseThrow();
		// .collect(Collectors.toList())
		// .get(0);
	}

	public void printProductReport(int id) {
		this.printProductReport(this.findProduct(id));
	}

	public Product reviewProduct(int id, Rating rating, String comments) {
		return reviewProduct(findProduct(id), rating, comments);
	}

	public Map<String, String> getDiscounts() {
		return products.keySet()
				.stream()
				.collect(
						Collectors.groupingBy(
								p -> p.getRating().getStars(),
								Collectors.collectingAndThen(
										Collectors.summingDouble(
												product -> product.getDiscount().doubleValue()),
										discount -> formatter.moneyFormat.format(discount))));
	}

	private static class ResourceFormatter {
		private final Locale locale;
		private ResourceBundle resources;
		private DateTimeFormatter dateFormat;
		private NumberFormat moneyFormat;

		private ResourceFormatter(Locale locale) {
			this.locale = locale;
			resources = ResourceBundle.getBundle("labs.pm.data.resources", this.locale);
			dateFormat = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).localizedBy(this.locale);
			moneyFormat = NumberFormat.getCurrencyInstance(this.locale);
		}

		private String formatProduct(Product product) {
			return MessageFormat.format(resources.getString("product"), product.getName(),
					moneyFormat.format(product.getPrice()), product.getRating().getStars(),
					dateFormat.format(product.getBestBefore()));
		}

		private String formatReview(Review review) {
			return MessageFormat.format(resources.getString("review"), review.getRating().getStars(),
					review.getComments());
		}

		private String getText(String key) {
			return resources.getString(key);
		}
	}
}
