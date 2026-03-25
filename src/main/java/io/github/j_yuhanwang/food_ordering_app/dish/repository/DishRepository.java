package io.github.j_yuhanwang.food_ordering_app.dish.repository;

import io.github.j_yuhanwang.food_ordering_app.dish.entity.Dish;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing Dish entities.
 * Handles canteen-specific food retrieval and global search.
 *
 * @author YuhanWang
 * @Date 28/01/2026 2:56 pm
 */
public interface DishRepository extends JpaRepository<Dish,Long> {

    /**
     * Finds a specific dish in a specific canteen.
     * Usage:
     * Useful for checking duplicates (e.g., preventing two "Lattes" in the same canteen)
     * or finding a specific item when both Canteen ID and Name are known.
     *
     * @param canteenId The ID of the canteen.
     * @param name The exact name of the dish.
     * @return Optional containing the dish item if found.
     */
    Optional<Dish> findByCanteenIdAndName(Long canteenId, String name);

    /**
     * Global search for dishes by name.
     * Features:
     * - Partial match (LIKE %name%)
     * - Case insensitive (ignores upper/lower case)
     *
     * @param name The keyword to search for.
     * @return List of matching dish items.
     */
    List<Dish> findByNameContainingIgnoreCase(String name);

    /**
     * Retrieves the full dish list for a specific canteen.
     * Used for the Canteen Detail page.
     *
     * @param id The ID of the canteen.
     * @return List of all dish items in that canteen.
     */
    List<Dish> findByCanteenId(Long id);
}
