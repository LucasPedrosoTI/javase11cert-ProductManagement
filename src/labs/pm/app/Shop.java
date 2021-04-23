package labs.pm.app;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import labs.pm.data.Product;
import labs.pm.data.ProductManager;
import labs.pm.data.Rating;

public class Shop {
	public static final Logger LOGGER = Logger.getGlobal();

	public static void main(String[] args) {

		ProductManager pm = ProductManager.getInstance();
		AtomicInteger clientCount = new AtomicInteger(0);

		Callable<String> client = () -> {
			String clientId = "Client " + clientCount.incrementAndGet();
			String threadName = Thread.currentThread().getName();
			int productId = ThreadLocalRandom.current().nextInt(2) + 101;
			String languageTag = ProductManager.getSupportedLocales()
					.stream()
					.skip(ThreadLocalRandom.current().nextInt(3))
					.findFirst().get();
			StringBuilder log = new StringBuilder();

			log.append(clientId + threadName + "\n-\tstart of log\t-\n");

			log.append(pm.getDiscounts(languageTag).entrySet().stream()
					.map(entry -> entry.getKey() + "\t" + entry.getValue()).collect(Collectors.joining("\n")));

			Product product = pm.reviewProduct(productId, Rating.FOUR_STARS, "Yet another review");

			log.append((product != null) ? "\nProduct: " + productId + " reviewed\n"
					: "\nProduct " + productId + " not reviewed\n");

			pm.printProductReport(productId, languageTag, clientId);

			log.append(clientId + " generated report for " + productId + " product");

			log.append("\n-\tend of the log\t-\n");

			return log.toString();
		};

		List<Callable<String>> clients = Stream.generate(() -> client).limit(5).collect(Collectors.toList());

		ExecutorService eService = Executors.newFixedThreadPool(3);

		try {
			List<Future<String>> results = eService.invokeAll(clients);
			eService.shutdown();

			results.stream().forEach(result -> {
				try {
					System.out.println(result.get());
				} catch (InterruptedException | ExecutionException e) {
					LOGGER.severe(() -> "Error retrieving client log " + e.getMessage());
					Thread.currentThread().interrupt();
				}
			});
		} catch (InterruptedException e) {
			LOGGER.severe(() -> "Error invoking clients " + e.getMessage());
			Thread.currentThread().interrupt();
		}
	}

}
