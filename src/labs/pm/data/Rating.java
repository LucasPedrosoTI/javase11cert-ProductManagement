package labs.pm.data;

public enum Rating {
	NOT_RATED("\u2606\u2606\u2606\u2606\u2606"), ONE_STAR("\u2605\u2606\u2606\u2606\u2606"),
	TWO_STARS("\u2605\u2605\u2606\u2606\u2606"), THREE_STARS("\u2605\u2605\u2605\u2606\u2606"),
	FOUR_STARS("\u2605\u2605\u2605\u2605\u2606"), FIVE_STARS("\u2605\u2605\u2605\u2605\u2605");

	private String stars;

	private Rating(String stars) {
		this.stars = stars;
	}

	public String getStars() {
		return stars;
	}

}
