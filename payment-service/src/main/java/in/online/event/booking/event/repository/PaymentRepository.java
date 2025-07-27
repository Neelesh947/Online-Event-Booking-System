package in.online.event.booking.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import in.online.event.booking.event.entity.Payment;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String>{

	Payment findByTransactionId(String transactionId);
}
