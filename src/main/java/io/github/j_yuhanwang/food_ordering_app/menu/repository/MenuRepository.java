package io.github.j_yuhanwang.food_ordering_app.menu.repository;

import io.github.j_yuhanwang.food_ordering_app.menu.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing Menu entities.
 * Handles canteen-specific food retrieval and global search.
 *
 * @author YuhanWang
 * @Date 28/01/2026 2:56 pm
 */
public interface MenuRepository extends JpaRepository<Menu,Long> {

    /**
     * Finds a specific dish in a specific canteen.
     * Usage:
     * Useful for checking duplicates (e.g., preventing two "Lattes" in the same canteen)
     * or finding a specific item when both Canteen ID and Name are known.
     *
     * @param canteenId The ID of the canteen.
     * @param name The exact name of the dish.
     * @return Optional containing the menu item if found.
     */
    Optional<Menu> findByCanteenIdAndName(Long canteenId, String name);

    /**
     * Global search for dishes by name.
     * Features:
     * - Partial match (LIKE %name%)
     * - Case insensitive (ignores upper/lower case)
     *
     * @param name The keyword to search for.
     * @return List of matching menu items.
     */
    List<Menu> findByNameContainingIgnoreCase(String name);

    /**
     * Retrieves the full menu list for a specific canteen.
     * Used for the Canteen Detail page.
     *
     * @param id The ID of the canteen.
     * @return List of all menu items in that canteen.
     */
    List<Menu> findByCanteenId(Long id);
}
