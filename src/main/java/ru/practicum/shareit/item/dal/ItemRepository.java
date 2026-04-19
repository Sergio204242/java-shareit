package ru.practicum.shareit.item.dal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    boolean existsByItemIdAndOwnerId(Long id, Long userId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Item i SET i.name = :name")
    void updateName(@Param("name") String name);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Item i SET i.description = :description")
    void updateDescription(@Param("description") String description);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Item i SET i.available = :available")
    void updateAvailable(@Param("available") boolean available);

    List<Item> findItemsByOwnerId(long userId);

    @Query("SELECT i FROM Item i WHERE (LOWER(i.name) LIKE LOWER(CONCAT('%', :text, '%')) " +
            "OR LOWER(i.description) LIKE LOWER(CONCAT('%', :text, '%'))) AND i.available = TRUE")
    List<Item> searchByText(@Param("text") String text);
}
