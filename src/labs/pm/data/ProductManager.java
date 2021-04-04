package labs.pm.data;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import labs.pm.app.Shop;

public class ProductManager {

  private final Locale locale;
  private ResourceBundle resources;
  private DateTimeFormatter dateFormat;
  private NumberFormat moneyFormat;

  private Map<Product, List<Review>> products = new HashMap<>();
  // private Product product;
  // private Review[] reviews = new Review[5];

  public ProductManager(Locale locale) {
    this.locale = locale;
    resources = ResourceBundle.getBundle("labs.pm.data.resources", this.locale);
    dateFormat = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).localizedBy(this.locale);
    moneyFormat = NumberFormat.getCurrencyInstance(this.locale);
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
    int sum = 0;
    for (Review review : reviews) {
      sum += review.getRating().ordinal();
    }
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
    product = product.applyRating(Rateable.convert(Math.round((float) sum / reviews.size())));
    products.put(product, reviews);
    return product;
  }

  public void printProductReport(Product product) {
    StringBuilder txt = new StringBuilder();
    final char NEW_LINE = '\n';
    List<Review> reviews = products.get(product);
    Collections.sort(reviews);
    txt.append(
        MessageFormat.format(resources.getString("product"), product.getName(), moneyFormat.format(product.getPrice()),
            product.getRating().getStars(), dateFormat.format(product.getBestBefore())))
        .append(NEW_LINE);

    for (Review review : reviews) {
      txt.append(
          MessageFormat.format(resources.getString("review"), review.getRating().getStars(), review.getComments()))
          .append(NEW_LINE);
    }

    if (reviews.isEmpty()) {
      txt.append(resources.getString("no.reviews")).append(NEW_LINE);
    }

    Shop.LOGGER.info(txt.toString());
  }

  public Product findProduct(int id) {
    Iterator<Product> it = products.keySet().iterator();
    while (it.hasNext()) {
      Product product = it.next();
      if (product.getId() == id) {
        return product;
      }
    }
    return null;
  }

  public void printProductReport(int id){
    this.printProductReport(this.findProduct(id));
  }

  public Product reviewProduct(int id, Rating rating, String comments){
    return reviewProduct(findProduct(id), rating, comments);
  }
}
