package in.online.event.booking.event.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import in.online.event.booking.event.entity.Booking;

@Repository
public interface BookingRepository extends JpaRepository<Booking, String> {

	List<Booking> findByUserId(String userId);
}
