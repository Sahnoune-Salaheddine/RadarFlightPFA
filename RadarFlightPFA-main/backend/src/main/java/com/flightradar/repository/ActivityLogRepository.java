package com.flightradar.repository;

import com.flightradar.model.ActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    
    Page<ActivityLog> findByOrderByTimestampDesc(Pageable pageable);
    
    Page<ActivityLog> findByActivityTypeOrderByTimestampDesc(
        ActivityLog.ActivityType activityType, 
        Pageable pageable
    );
    
    Page<ActivityLog> findByUserIdOrderByTimestampDesc(Long userId, Pageable pageable);
    
    Page<ActivityLog> findBySeverityOrderByTimestampDesc(
        ActivityLog.LogSeverity severity, 
        Pageable pageable
    );
    
    @Query("SELECT l FROM ActivityLog l WHERE l.timestamp BETWEEN :start AND :end ORDER BY l.timestamp DESC")
    Page<ActivityLog> findByTimestampBetween(
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end,
        Pageable pageable
    );
    
    @Query("SELECT l FROM ActivityLog l WHERE " +
           "(:userId IS NULL OR l.userId = :userId) AND " +
           "(:activityType IS NULL OR l.activityType = :activityType) AND " +
           "(:severity IS NULL OR l.severity = :severity) AND " +
           "l.timestamp BETWEEN :start AND :end " +
           "ORDER BY l.timestamp DESC")
    Page<ActivityLog> findWithFilters(
        @Param("userId") Long userId,
        @Param("activityType") ActivityLog.ActivityType activityType,
        @Param("severity") ActivityLog.LogSeverity severity,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end,
        Pageable pageable
    );
    
    @Query("SELECT COUNT(l) FROM ActivityLog l WHERE l.timestamp BETWEEN :start AND :end")
    Long countByTimestampBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    @Query("SELECT l.activityType, COUNT(l) FROM ActivityLog l WHERE l.timestamp BETWEEN :start AND :end GROUP BY l.activityType")
    List<Object[]> countByActivityTypeBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}

