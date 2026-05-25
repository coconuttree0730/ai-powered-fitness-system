CREATE UNIQUE INDEX IF NOT EXISTS uk_fitness_booking_user_session_active
    ON fitness_booking (user_id, session_id)
    WHERE session_id IS NOT NULL
      AND deleted = false
      AND status IN (0, 1);
