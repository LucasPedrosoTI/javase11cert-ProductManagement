package labs.pm.data;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;

import labs.pm.app.Shop;

public class ProductManager {

  private final Locale locale;
  private ResourceBundle resources;
  private DateTimeFormatter dateFormat;
  private NumberFormat moneyFormat;

  private Product product;
  private Review[] reviews = new Review[5];

  public ProductManager(Locale locale) {
    this.locale = locale;
    resources = ResourceBundle.getBundle("labs.pm.data.resources", this.locale);
    dateFormat = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).localizedBy(this.locale);
    moneyFormat = NumberFormat.getCurrencyInstance(this.locale);
  }

  public Product createProduct(int id, String name, BigDecimal price, Rating rating, LocalDate bestBefore) {
    product = new Food(id, name, price, rating, bestBefore);
    return product;
  }

  public Product createProduct(int id, String name, BigDecimal price, Rating rating) {
    product = new Drink(id, name, price, rating);
    return product;
  }

  public Product reviewProduct(Product product, Rating rating, String comments) {
    // this.review = new Review(rating, comments);
    if (reviews[reviews.length - 1] != null) {
      reviews = Arrays.copyOf(reviews, reviews.length + 5);
    }
    int sum = 0, i = 0;
    boolean reviewed = false;
    while (i < reviews.length && !reviewed) {
      if (reviews[i] == null) {
        reviews[i] = new Review(rating, comments);
        reviewed = true;
      }
      sum += reviews[i].getRating().ordinal();
      i++;
    }

    // this.product = product.applyRating(rating);
    this.product = product.applyRating(Rateable.convert(Math.round((float) sum / i)));
    return this.product;
  }

  public void printProductReport() {
    StringBuilder txt = new StringBuilder();
    final char NEW_LINE = '\n';
    txt.append(
        MessageFormat
        .format(resources.getString("product"), 
          product.getName(), 
          moneyFormat.format(product.getPrice()),
          product.getRating().getStars(), 
          dateFormat.format(product.getBestBefore())))
        .append(NEW_LINE);

    for (Review review : reviews) {
      if (Objects.isNull(review)) {
        break;
      }
      txt.append(MessageFormat
          .format(resources.getString("review"), review.getRating().getStars(), review.getComments()))
          .append(NEW_LINE);
    }

    if (Objects.isNull(reviews[0])) {
      txt.append(resources.getString("no.reviews")).append(NEW_LINE);
    }

    Shop.LOGGER.info(txt.toString());
  }
}
