package labs.pm.data;

import static labs.pm.app.Shop.LOGGER;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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
import java.util.logging.Level;
import java.util.stream.Collectors;;

public class ProductManager {

	private static final String CONFIG_RESOURCE = "labs.pm.data.config";
	private static final String REVIEW_DATA_FORMAT = "review.data.format";
	private static final String PRODUCT_DATA_FORMAT = "product.data.format";
	private static final char NEW_LINE = '\n';

	private static Map<String, ResourceFormatter> formatters = Map.of("en-GB", new ResourceFormatter(Locale.UK),
			"en-US", new ResourceFormatter(Locale.US),
			"pt-BR", new ResourceFormatter(new Locale("pt", "BR")),
			"es-EN", new ResourceFormatter(new Locale("es", "EN")));

	private Map<Product, List<Review>> products = new HashMap<>();
	private ResourceFormatter formatter;
	private ResourceBundle config = ResourceBundle.getBundle(CONFIG_RESOURCE);
	private MessageFormat reviewFormat = new MessageFormat(config.getString(REVIEW_DATA_FORMAT));
	private MessageFormat productFormat = new MessageFormat(config.getString(PRODUCT_DATA_FORMAT));

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
		// for (Review review : reviews) {
		// sum += review.getRating().ordinal();
		// }
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
		// Math.round((float) sum / reviews.size())
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

		LOGGER.info(txt::toString);
	}

	public void printProducts(Predicate<Product> filter, Comparator<Product> sorter) {
		// List<Product> productList = new ArrayList<>(products.keySet());
		// productList.sort(sorter);
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

		LOGGER.warning(txt::toString);

	}

	public Product findProduct(int id) throws ProductManagerException {
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
				.orElseThrow(() -> new ProductManagerException("Product with id " + id + " not found"));
		//				.get();
		//				.orElse(null);
		// .collect(Collectors.toList())
		// .get(0);
	}

	public void printProductReport(int id) {
		try {
			this.printProductReport(this.findProduct(id));
		} catch (ProductManagerException e) {
			LOGGER.info(e.getMessage());
		}
	}

	public Product reviewProduct(int id, Rating rating, String comments) {
		try {
			return reviewProduct(findProduct(id), rating, comments);
		} catch (Exception e) {
			LOGGER.info(e.getMessage());
		}

		return null;
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

	public void parseReview(String text) {
		try {
			Object[] values = reviewFormat.parse(text);
			reviewProduct(Integer.parseInt((String) values[0]), Rateable.convert(Integer.parseInt((String) values[1])),
					(String) values[2]);
		} catch (ParseException | NumberFormatException e) {
			LOGGER.log(Level.WARNING, "Error parsing review {0}", text);
		}
	}

	public void parseProduct(String text) {
		try {
			Object[] values = productFormat.parse(text);
			int id = Integer.parseInt((String) values[1]);
			String name = (String) values[2];
			BigDecimal price = new BigDecimal((String) values[3]);
			Rating rating = Rateable.convert(Integer.parseInt((String) values[4]));

			switch ((String) values[0]) {
			case "D":
				createProduct(id, name, price, rating);
				break;
			case "F":
				LocalDate bestBefore = LocalDate.parse((CharSequence) values[5]);
				createProduct(id, name, price, rating, bestBefore);
				break;
			default:
				throw new ParseException("Parsing Error", 0);
			}

		} catch (ParseException | NumberFormatException | DateTimeParseException e) {
			LOGGER.log(Level.WARNING, "Error parsing review {0} - {1}", new Object[] { text, e.getMessage() });
		}
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
