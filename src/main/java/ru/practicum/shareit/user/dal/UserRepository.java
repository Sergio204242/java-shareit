package ru.practicum.shareit.user.dal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.user.model.User;


public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE User u SET u.name = :name WHERE u.id = :id")
    void updateName(@Param("name") String name, @Param("id") Long id);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE User u SET u.email = :email WHERE u.id = :id")
    void updateEmail(@Param("email") String email, @Param("id") Long id);
}