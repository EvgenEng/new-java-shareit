package ru.practicum.shareit.booking.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingStatus;

public interface BookingRepository extends JpaRepository<Booking, Long> {
        List<Booking> findByBookerIdOrderByStartDesc(Long bookerId, Pageable pageable);

        List<Booking> findByBookerIdAndStatusOrderByStartDesc(Long bookerId, BookingStatus status, Pageable pageable);

        @Query("SELECT b FROM Booking b " +
                        "WHERE b.booker.id = :bookerId " +
                        "AND :now BETWEEN b.start AND b.end " +
                        "ORDER BY b.start DESC")
        List<Booking> findByBookerIdAndCurrentOrderByStartDesc(@Param("bookerId") Long bookerId,
                        @Param("now") LocalDateTime now,
                        Pageable pageable);

        @Query("SELECT b FROM Booking b " +
                        "WHERE b.booker.id = :bookerId " +
                        "AND b.end < :now " +
                        "ORDER BY b.start DESC")
        List<Booking> findByBookerIdAndPastOrderByStartDesc(@Param("bookerId") Long bookerId,
                        @Param("now") LocalDateTime now,
                        Pageable pageable);

        @Query("SELECT b FROM Booking b " +
                        "WHERE b.booker.id = :bookerId " +
                        "AND b.start > :now " +
                        "ORDER BY b.start DESC")
        List<Booking> findByBookerIdAndFutureOrderByStartDesc(@Param("bookerId") Long bookerId,
                        @Param("now") LocalDateTime now,
                        Pageable pageable);

        List<Booking> findByItem_OwnerIdOrderByStartDesc(Long ownerId, Pageable pageable);

        List<Booking> findByItem_OwnerIdAndStatusOrderByStartDesc(Long ownerId, BookingStatus status,
                        Pageable pageable);

        @Query("SELECT b FROM Booking b " +
                        "WHERE b.item.owner.id = :ownerId " +
                        "AND :now BETWEEN b.start AND b.end " +
                        "ORDER BY b.start DESC")
        List<Booking> findByItem_OwnerIdAndCurrentOrderByStartDesc(@Param("ownerId") Long ownerId,
                        @Param("now") LocalDateTime now,
                        Pageable pageable);

        @Query("SELECT b FROM Booking b " +
                        "WHERE b.item.owner.id = :ownerId " +
                        "AND b.end < :now " +
                        "ORDER BY b.start DESC")
        List<Booking> findByItem_OwnerIdAndPastOrderByStartDesc(@Param("ownerId") Long ownerId,
                        @Param("now") LocalDateTime now,
                        Pageable pageable);

        @Query("SELECT b FROM Booking b " +
                        "WHERE b.item.owner.id = :ownerId " +
                        "AND b.start > :now " +
                        "ORDER BY b.start DESC")
        List<Booking> findByItem_OwnerIdAndFutureOrderByStartDesc(@Param("ownerId") Long ownerId,
                        @Param("now") LocalDateTime now,
                        Pageable pageable);

        @Query("SELECT b FROM Booking b " +
                        "WHERE b.item.id = :itemId " +
                        "AND b.booker.id = :bookerId " +
                        "AND b.status = :status " +
                        "AND b.end < :now")
        Optional<Booking> findCompletedBooking(@Param("itemId") Long itemId,
                        @Param("bookerId") Long bookerId,
                        @Param("now") LocalDateTime now,
                        @Param("status") BookingStatus status);

        @Query("SELECT b FROM Booking b " +
                        "WHERE b.item.id = :itemId " +
                        "AND b.status = :status " +
                        "ORDER BY b.start")
        List<Booking> findApprovedBookingsByItemId(@Param("itemId") Long itemId,
                        @Param("status") BookingStatus status);

        @Query("SELECT b FROM Booking b " +
                        "WHERE b.item.id = :itemId " +
                        "AND b.start < :now " +
                        "AND b.status = :status " +
                        "ORDER BY b.start DESC")
        List<Booking> findByItemIdAndStartBeforeOrderByStartDesc(@Param("itemId") Long itemId,
                        @Param("now") LocalDateTime now,
                        @Param("status") BookingStatus status,
                        Pageable pageable);

        @Query("SELECT b FROM Booking b " +
                        "WHERE b.item.id = :itemId " +
                        "AND b.end < :now " +
                        "AND b.status = :status " +
                        "ORDER BY b.end DESC")
        List<Booking> findByItemIdAndEndBeforeOrderByEndDesc(@Param("itemId") Long itemId,
                        @Param("now") LocalDateTime now,
                        @Param("status") BookingStatus status,
                        Pageable pageable);

        @Query("SELECT b FROM Booking b " +
                        "WHERE b.item.id = :itemId " +
                        "AND b.start > :now " +
                        "AND b.status = :status " +
                        "ORDER BY b.start ASC")
        List<Booking> findByItemIdAndStartAfterOrderByStartAsc(@Param("itemId") Long itemId,
                        @Param("now") LocalDateTime now,
                        @Param("status") BookingStatus status,
                        Pageable pageable);

        @Query("SELECT b FROM Booking b " +
                        "WHERE b.booker.id = :bookerId " +
                        "AND b.item.id = :itemId " +
                        "AND b.end < :now " +
                        "AND b.status = :status")
        List<Booking> findByBookerIdAndItemIdAndEndBeforeAndStatus(@Param("bookerId") Long bookerId,
                        @Param("itemId") Long itemId,
                        @Param("now") LocalDateTime now,
                        @Param("status") BookingStatus status);

        @Query("SELECT b FROM Booking b " +
                        "WHERE b.item.id = :itemId " +
                        "AND b.start < :now " +
                        "AND b.status = 'APPROVED' " +
                        "ORDER BY b.start DESC")
        List<Booking> findByItemIdAndEndBeforeOrderByEndDescAllStatuses(@Param("itemId") Long itemId,
                        @Param("now") LocalDateTime now,
                        Pageable pageable);

        @Query("SELECT b FROM Booking b " +
                        "WHERE b.item.id = :itemId " +
                        "AND b.start > :now " +
                        "AND b.status = 'APPROVED' " +
                        "ORDER BY b.start ASC")
        List<Booking> findByItemIdAndStartAfterOrderByStartAscAllStatuses(@Param("itemId") Long itemId,
                        @Param("now") LocalDateTime now,
                        Pageable pageable);
}
