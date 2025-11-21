-- =====================================================
-- TABLE: activity_logs
-- Journal d'activité système pour le dashboard admin
-- =====================================================

CREATE TABLE IF NOT EXISTS activity_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    username VARCHAR(50),
    activity_type VARCHAR(50) NOT NULL CHECK (activity_type IN (
        'LOGIN', 'LOGOUT', 'FLIGHT_CREATED', 'FLIGHT_UPDATED', 'FLIGHT_CANCELLED',
        'AIRCRAFT_UPDATED', 'WEATHER_ALERT', 'RADAR_ALERT', 'USER_CREATED',
        'USER_UPDATED', 'USER_DELETED', 'SYSTEM_ERROR', 'DATA_EXPORT', 'REPORT_GENERATED'
    )),
    description TEXT NOT NULL,
    entity_type VARCHAR(50),
    entity_id BIGINT,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    severity VARCHAR(20) NOT NULL DEFAULT 'INFO' CHECK (severity IN ('INFO', 'WARNING', 'ERROR', 'CRITICAL')),
    ip_address VARCHAR(45)
);

-- Index pour améliorer les performances des requêtes
CREATE INDEX IF NOT EXISTS idx_activity_logs_timestamp ON activity_logs(timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_activity_logs_user_id ON activity_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_activity_logs_activity_type ON activity_logs(activity_type);
CREATE INDEX IF NOT EXISTS idx_activity_logs_severity ON activity_logs(severity);

COMMENT ON TABLE activity_logs IS 'Journal d''activité système pour le suivi des actions utilisateurs et événements système';

