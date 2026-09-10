package com.freddieapp.notification.repository;

import com.freddieapp.notification.entity.FreddieEventTracker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FreddieEventTrackerRepository extends JpaRepository<FreddieEventTracker, Long> {
}
