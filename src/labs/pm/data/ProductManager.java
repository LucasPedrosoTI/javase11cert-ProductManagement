package labs.pm.data;

import static labs.pm.app.Shop.LOGGER;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
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
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.stream.Collectors;;

public class ProductManager {

	private Map<Product, List<Review>> products = new HashMap<>();
	private static final String REVIEW_FILE = "reviews.data.file";
	private static final String REPORT_FILE = "report.file";
	private static final String TEMP_FOLDER = "temp.folder";
	private static final String DATA_FOLDER = "data.folder";
	private static final String REPORTS_FOLDER = "reports.folder";
	private static final String CONFIG_RESOURCE = "labs.pm.data.config";
	private static final String REVIEW_DATA_FORMAT = "review.data.format";
	private static final String PRODUCT_DATA_FORMAT = "product.data.format";
	private static final String NEW_LINE = System.lineSeparator();

	private static final Map<String, ResourceFormatter> formatters = Map.of("en-GB", new ResourceFormatter(Locale.UK),
			"en-US", new ResourceFormatter(Locale.US),
			"pt-BR", new ResourceFormatter(new Locale("pt", "BR")),
			"es-EN", new ResourceFormatter(new Locale("es", "EN")));
	//	private ResourceFormatter formatter;

	private final ResourceBundle config = ResourceBundle.getBundle(CONFIG_RESOURCE);

	private final MessageFormat reviewFormat = new MessageFormat(config.getString(REVIEW_DATA_FORMAT));
	private final MessageFormat productFormat = new MessageFormat(config.getString(PRODUCT_DATA_FORMAT));

	//	private Path currentFolder = Path.of(".");
	private final Path reportsFolder = Path.of(config.getString(REPORTS_FOLDER));
	private final Path dataFolder = Path.of(config.getString(DATA_FOLDER));
	private final Path tempFolder = Path.of(config.getString(TEMP_FOLDER));

	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	private final Lock writeLock = lock.writeLock();
	private final Lock readLock = lock.readLock();

	private static final ProductManager pm = new ProductManager();
	//	public ProductManager(Locale locale) {
	//		this(locale.toLanguageTag());
	//	}

	private ProductManager() {
		//		changeLocale(languageTag);
		loadAllData();
	}

	//	public void changeLocale(String languageTag) {
	//		formatter  = formatters.getOrDefault(languageTag, formatters.get("pt-BR"));
	//	}

	public static ProductManager getInstance() {
		return pm;
	}

	public static Set<String> getSupportedLocales() {
		return formatters.keySet();
	}

	public Product createProduct(int id, String name, BigDecimal price, Rating rating, LocalDate bestBefore) {
		Product product = null;
		try {
			writeLock.lock();
			product = new Food(id, name, price, rating, bestBefore);
			products.putIfAbsent(product, new ArrayList<>());
		} catch (Exception e) {
			LOGGER.info(() -> "Error adding product " + e.getMessage());
		} finally {
			writeLock.unlock();
		}

		return product;
	}

	public Product createProduct(int id, String name, BigDecimal price, Rating rating) {
		Product product = null;
		try {
			writeLock.lock();
			product = new Drink(id, name, price, rating);
			products.putIfAbsent(product, new ArrayList<>());
		} catch (Exception e) {
			LOGGER.info(() -> "Error adding product " + e.getMessage());
		} finally {
			writeLock.unlock();
		}

		return product;
	}

	private Product reviewProduct(Product product, Rating rating, String comments) {
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

	private void printProductReport(Product product, String languageTag, String client) throws IOException {
		//		StringBuilder txt = new StringBuilder();
		ResourceFormatter formatter = formatters.getOrDefault(languageTag, formatters.get("pt-BR"));
		List<Review> reviews = products.get(product);
		Collections.sort(reviews);

		Path productFile = reportsFolder
				.resolve(MessageFormat.format(config.getString(REPORT_FILE), product.getId(), client));

		//		LOGGER.info(">>>> normalize " + reportsFolder.normalize().toString());
		//		LOGGER.info(">>>> absolute path " + reportsFolder.toAbsolutePath().toString());
		//		LOGGER.info(">>>> uri " + reportsFolder.toUri().toString());
		//		//		LOGGER.info(">>>> root " + reportsFolder.getRoot().toString());
		//		LOGGER.info(">>>> parent " + reportsFolder.getParent().toString());
		//		LOGGER.info(">>>>" + productFile.toString());

		try (PrintWriter out = new PrintWriter(
				new OutputStreamWriter(Files.newOutputStream(productFile, StandardOpenOption.CREATE),
						StandardCharsets.UTF_8))) {

			out.append(formatter.formatProduct(product)).append(NEW_LINE);
			if (reviews.isEmpty()) {
				out.append(formatter.getText("no.reviews")).append(NEW_LINE);
			} else {
				// reviews.forEach(review ->
				// out.append(formatter.formatReview(review)).append(NEW_LINE)); // bad when
				// using parallelism
				out.append(reviews
						.stream()
						.map(r -> formatter.formatReview(r) + NEW_LINE)
						.collect(Collectors.joining())); // better for pararellism, because the joining is done
				// altogether
				// after the conversions
			}
		}
		// LOGGER.info(txt::toString);
	}

	public void printProductReport(int id, String languageTag, String client) {
		try {
			readLock.lock();
			this.printProductReport(this.findProduct(id), languageTag, client);
		} catch (ProductManagerException e) {
			LOGGER.info(e.getMessage());
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "Error printing product report " + e.getMessage(), e);
		} finally {
			readLock.unlock();
		}
	}

	public void printProducts(Predicate<Product> filter, Comparator<Product> sorter, String languageTag) {
		try {
			readLock.lock();
			// List<Product> productList = new ArrayList<>(products.keySet());
			// productList.sort(sorter);
			// for (Product product : productList) {
			// txt.append(formatter.formatProduct(product)).append(NEW_LINE);
			// }
			ResourceFormatter formatter = formatters.getOrDefault(languageTag, formatters.get("pt-BR"));
			StringBuilder txt = new StringBuilder();
			txt.append(products.keySet()
					.stream()
					.sorted(sorter)
					.filter(filter)
					.map(p -> formatter.formatProduct(p) + NEW_LINE)
					.collect(Collectors.joining()));
			LOGGER.warning(txt::toString);
		} finally {
			readLock.unlock();
		}

	}

	public Product findProduct(int id) throws ProductManagerException {
		try {
			readLock.lock();
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
			// .get();
			// .orElse(null);
			// .collect(Collectors.toList())
			// .get(0);
		} finally {
			readLock.unlock();
		}
	}

	public Product reviewProduct(int id, Rating rating, String comments) {
		try {
			writeLock.lock();
			return reviewProduct(findProduct(id), rating, comments);
		} catch (Exception e) {
			LOGGER.info(e.getMessage());
		} finally {
			writeLock.unlock();
		}

		return null;
	}

	public Map<String, String> getDiscounts(String languageTag) {
		try {
			readLock.lock();
			ResourceFormatter formatter = formatters.getOrDefault(languageTag, formatters.get("pt-BR"));
			return products.keySet()
					.stream()
					.collect(
							Collectors.groupingBy(
									p -> p.getRating().getStars(),
									Collectors.collectingAndThen(
											Collectors.summingDouble(
													product -> product.getDiscount().doubleValue()),
											discount -> formatter.moneyFormat.format(discount))));
		} finally {
			readLock.unlock();
		}
	}

	private Review parseReview(String text) {
		Review review = null;
		try {
			Object[] values = reviewFormat.parse(text);
			review = new Review(Rateable.convert(Integer.parseInt((String) values[0])), (String) values[1]);
		} catch (ParseException | NumberFormatException e) {
			LOGGER.log(Level.WARNING, "Error parsing review {0}", text);
		}

		return review;
	}

	private Product parseProduct(String text) {
		Product product = null;
		try {
			Object[] values = productFormat.parse(text);
			int id = Integer.parseInt((String) values[1]);
			String name = (String) values[2];
			BigDecimal price = new BigDecimal((String) values[3]);
			Rating rating = Rateable.convert(Integer.parseInt((String) values[4]));

			switch ((String) values[0]) {
			case "D":
				product = new Drink(id, name, price, rating);
				break;
			case "F":
				LocalDate bestBefore = LocalDate.parse((CharSequence) values[5]);
				product = new Food(id, name, price, rating, bestBefore);
				break;
			default:
				throw new ParseException("Parsing Error", 0);
			}

		} catch (ParseException | NumberFormatException | DateTimeParseException e) {
			LOGGER.log(Level.WARNING, "Error parsing review {0} - {1}", new Object[] { text, e.getMessage() });
		}

		return product;
	}

	private Product loadProduct(Path file) {
		Product product = null;

		try {
			product = parseProduct(
					Files.lines(file, StandardCharsets.UTF_8).findFirst().orElseThrow());
		} catch (Exception e) {
			LOGGER.warning(() -> "Error loading product " + e.getMessage());
		}

		return product;
	}

	private List<Review> loadReviews(Product product) {
		List<Review> reviews = null;

		Path file = dataFolder.resolve(MessageFormat.format(config.getString(REVIEW_FILE), product.getId()));

		if (Files.notExists(file)) {
			reviews = new ArrayList<>();
		} else {
			try {
				reviews = Files.lines(file, StandardCharsets.UTF_8)
						.map(text -> parseReview(text))
						.filter(review -> review != null)
						.collect(Collectors.toList());
			} catch (IOException e) {
				LOGGER.warning(() -> "Error parsing review for file " + file + e.getMessage());
			}
		}

		return reviews;
	}

	private void loadAllData() {
		try {
			products = Files.list(dataFolder)
					.filter(file -> file.getFileName().toString().startsWith("product"))
					.map(this::loadProduct)
					.filter(Objects::nonNull)
					.collect(Collectors.toMap(product -> product, this::loadReviews));
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "Error loading data " + e.getMessage(), e);
		}
	}

	public void dumpData() {
		try {
			if (Files.notExists(tempFolder)) {
				Files.createDirectories(tempFolder);
			}

			Path tempFile = tempFolder
					.resolve(MessageFormat.format(config.getString("temp.file"), Math.round(Math.random() * 1000)));

			try (ObjectOutputStream out = new ObjectOutputStream(
					Files.newOutputStream(tempFile, StandardOpenOption.CREATE))) {
				out.writeObject(products);
				products = new HashMap<>();
			}
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "Error dumping data " + e.getMessage(), e);
		}
	}

	@SuppressWarnings("unchecked")
	public void restoreData() {
		try {
			Path tempFile = Files.list(tempFolder)
					.filter(path -> path.getFileName().toString().endsWith("tmp"))
					.findFirst().orElseThrow();
			try (ObjectInputStream in = new ObjectInputStream(
					Files.newInputStream(tempFile, StandardOpenOption.DELETE_ON_CLOSE))) {
				products = (HashMap<Product, List<Review>>) in.readObject();
			}
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error restoring data " + e.getMessage(), e);
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
