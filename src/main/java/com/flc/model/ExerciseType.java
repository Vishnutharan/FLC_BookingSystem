package com.flc.model;

/**
 * Enum representing the types of exercises available at Furzefield Leisure Centre.
 * Each exercise type has a display name and a fixed price per session.
 *
 * @author FLC Development Team
 */
public enum ExerciseType {

    /** Yoga session - £8.00 */
    YOGA("Yoga", 8.00),
    /** Zumba session - £7.50 */
    ZUMBA("Zumba", 7.50),
    /** Aquacise session - £9.00 */
    AQUACISE("Aquacise", 9.00),
    /** Box Fit session - £10.00 */
    BOX_FIT("Box Fit", 10.00),
    /** Body Blitz session - £9.50 */
    BODY_BLITZ("Body Blitz", 9.50);

    private final String displayName;
    private final double price;

    /**
     * Constructs an ExerciseType with a display name and price.
     *
     * @param displayName the human-readable name of the exercise
     * @param price       the price per session in GBP
     */
    ExerciseType(String displayName, double price) {
        this.displayName = displayName;
        this.price = price;
    }

    /**
     * Gets the human-readable display name of this exercise type.
     *
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets the price per session for this exercise type.
     *
     * @return the price in GBP
     */
    public double getPrice() {
        return price;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
