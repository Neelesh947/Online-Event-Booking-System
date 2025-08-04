package in.online.event.booking.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;

import in.online.event.booking.event.entity.Event;

@Repository
public interface EventRepository extends JpaRepository<Event, String>{

	// 🔍 Find upcoming events
    List<Event> findByStartTimeAfter(LocalDateTime now);

    // 🔍 Find events by category name
    List<Event> findByCategory_Name(String categoryName);

    // 🔍 Find events by organizerId
    List<Event> findByOrganizerId(String organizerId);

    // 🔍 Search by title or location
    List<Event> findByTitleContainingIgnoreCaseOrLocationContainingIgnoreCase(String title, String location);
    
    //Filter events by optional search text (title, description, Location)
    
    @Query("SELECT e FROM Event e WHERE " +
            "(:search IS NULL OR LOWER(e.title) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR e.description LIKE CONCAT('%', :search, '%') " +
            "OR LOWER(e.location) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (:categoryId IS NULL OR e.category.id = :categoryId) " +
            "AND (:from IS NULL OR e.startTime >= :from) " +
            "AND (:to IS NULL OR e.endTime <= :to)")
    List<Event> filterEvents(String search, String categoryId, LocalDateTime from, LocalDateTime to);
}
